package com.belhaddou.jroxy.service.cache.impl;

import com.belhaddou.jroxy.service.cache.EhCacheService;
import com.belhaddou.jroxy.service.cache.JRoxyCacheService;
import com.belhaddou.jroxy.util.UrlUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class JRoxyCacheServiceImpl implements JRoxyCacheService {

    private final EhCacheService<CachedResponse> cache;

    @Override
    public ResponseEntity<byte[]> getCachedResponse(HttpServletRequest request) {
        String key = buildCacheKey(request);
        log.debug("Getting cached response from cache with key {}", key);
        CachedResponse cached = cache.get(key);

        if (cached == null) return null;
        //TODO: Implement ETAG and LastModified for revalidation

        // Check expiry based on TTL
        if (isExpired(cached)) {
            log.debug("Evicting expired cache for key {}", key);
            cache.evict(key);
            return null;
        }

        return cached.toResponseEntity();
    }

    @Override
    public void putResponse(HttpServletRequest request, ResponseEntity<byte[]> response) {
        if (!shouldCache(response)) {
            return;
        }

        String key = buildCacheKey(request);
        log.debug("Putting response to cache with key {}", key);

        long ttl = computeTtlFromHeaders(response.getHeaders());
        CachedResponse cached = new CachedResponse(response, ttl);
        cache.put(key, cached);
    }

    private boolean shouldCache(ResponseEntity<byte[]> response) {
        if (!response.getStatusCode().is2xxSuccessful()) return false; // only successful GET responses

        String cacheControl = response.getHeaders().getCacheControl();
        if (cacheControl != null) {
            cacheControl = cacheControl.toLowerCase();
            if (cacheControl.contains("no-store")) return false;
            return !cacheControl.contains("private");
        }

        return true;
    }

    private boolean isExpired(CachedResponse cachedResponse) {
        if (cachedResponse.getExpiryTime() == null) return false;
        return Instant.now().isAfter(cachedResponse.getExpiryTime());
    }

    private String buildCacheKey(HttpServletRequest request) {
        String subdomain = UrlUtils.extractSubdomain(request);
        return request.getMethod() + ":" + subdomain + ":" + request.getRequestURI();
    }

    private long computeTtlFromHeaders(HttpHeaders headers) {
        String cacheControl = Optional.ofNullable(headers.getCacheControl()).orElse("").toLowerCase();

        // no-store or private should never cache
        if (cacheControl.contains("no-store") || cacheControl.contains("private")) {
            return 0;
        }

        // Immutable content can be cached for a long time (e.g., 1 year)
        if (cacheControl.contains("immutable")) {
            return 365 * 24 * 3600L;
        }

        // Try s-maxage (shared cache) first, then max-age
        long ttl = parseCacheControlMaxAge(cacheControl, "s-maxage");
        if (ttl == -1) ttl = parseCacheControlMaxAge(cacheControl, "max-age");

        // Fallback to Expires header if no max-age
        if (ttl == -1) {
            String expires = headers.getFirst(HttpHeaders.EXPIRES);
            if (expires != null) {
                try {
                    Instant expiryInstant = ZonedDateTime.parse(expires, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant();
                    ttl = Math.max(expiryInstant.getEpochSecond() - Instant.now().getEpochSecond(), 0);
                } catch (Exception e) {
                    ttl = -1;
                }
            }
        }

        return ttl;
    }

    private long parseCacheControlMaxAge(String cacheControl, String directive) {
        try {
            String[] directives = cacheControl.split(",");
            for (String dir : directives) {
                dir = dir.trim();
                if (dir.startsWith(directive + "=")) {
                    return Long.parseLong(dir.substring(directive.length() + 1).trim());
                }
            }
        } catch (NumberFormatException ignored) {
        }
        return -1;
    }

    // --- Inner Model Class ---
    static class CachedResponse {
        private final byte[] body;
        private final HttpHeaders headers;
        private final int statusCode;
        private final Instant expiryTime;

        CachedResponse(ResponseEntity<byte[]> response, long ttlSeconds) {
            this.body = response.getBody();
            this.headers = response.getHeaders();
            this.statusCode = response.getStatusCodeValue();
            this.expiryTime = ttlSeconds > 0 ? Instant.now().plusSeconds(ttlSeconds) : null;
        }

        Instant getExpiryTime() {
            return expiryTime;
        }

        ResponseEntity<byte[]> toResponseEntity() {
            return ResponseEntity.status(statusCode).headers(headers).body(body);
        }
    }
}
