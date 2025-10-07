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

@Service
@RequiredArgsConstructor
@Slf4j
// This service contains caching logic of the reverse-proxy
public class JRoxyCacheServiceImpl implements JRoxyCacheService {
    private final EhCacheService<CachedResponse> cache;

    public ResponseEntity<byte[]> getCachedResponse(HttpServletRequest request) {
        String key = buildCacheKey(request);
        log.debug("Getting cached response from cache with key {}", key);
        CachedResponse cached = cache.get(key);

        if (cached == null) return null;
        // check if the cached entity is expired then it should be evicted from the cache
        if (isExpired(cached)) {
            log.debug("Evicting cache for key {}", key);
            cache.evict(key);
            return null;
        }

        return cached.toResponseEntity();
    }

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
        // Only cache successful GET responses
        if (!response.getStatusCode().is2xxSuccessful()) return false;

        String cacheControl = response.getHeaders().getCacheControl();
        if (cacheControl != null) {
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


    //AI Generated
    private long computeTtlFromHeaders(HttpHeaders headers) {
        // Priority order:
        // 1. Cache-Control: max-age
        // 2. Expires header
        String cacheControl = headers.getCacheControl();
        if (cacheControl != null && cacheControl.contains("max-age")) {
            try {
                String[] parts = cacheControl.split("=");
                if (parts.length == 2) {
                    long seconds = Long.parseLong(parts[1].trim());
                    return seconds;
                }
            } catch (NumberFormatException ignored) {
            }
        }

        String expires = headers.getFirst(HttpHeaders.EXPIRES);
        if (expires != null) {
            try {
                Instant expiryInstant = ZonedDateTime.parse(expires, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant();
                long diff = expiryInstant.getEpochSecond() - Instant.now().getEpochSecond();
                return Math.max(diff, 0);
            } catch (Exception ignored) {
            }
        }

        // Default TTL if no cache headers specified
        return 60; // 1 minute
    }

    // AI Generated
    // --- Inner Model Class ---
    private static class CachedResponse {
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


