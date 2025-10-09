package com.belhaddou.jroxy.service.cache.impl;

import com.belhaddou.jroxy.service.cache.EhCacheService;
import com.belhaddou.jroxy.util.UrlUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JRoxyCacheServiceImplTest {

    private EhCacheService<JRoxyCacheServiceImpl.CachedResponse> cache;
    private JRoxyCacheServiceImpl service;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        cache = mock(EhCacheService.class);
        service = new JRoxyCacheServiceImpl(cache);
        request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/test");
        when(request.getServerName()).thenReturn("sub.example.com");
    }

    @Test
    void testPutAndGetCachedResponse() {
        try (MockedStatic<UrlUtils> utilsMock = mockStatic(UrlUtils.class)) {
            utilsMock.when(() -> UrlUtils.extractSubdomain(request)).thenReturn("sub");

            HttpHeaders headers = new HttpHeaders();
            headers.setCacheControl("max-age=5");
            ResponseEntity<byte[]> response = ResponseEntity.ok().headers(headers).body("Hello".getBytes());

            service.putResponse(request, response);

            ArgumentCaptor<JRoxyCacheServiceImpl.CachedResponse> captor = ArgumentCaptor.forClass(JRoxyCacheServiceImpl.CachedResponse.class);
            verify(cache).put(eq("GET:sub:/test"), captor.capture());

            JRoxyCacheServiceImpl.CachedResponse cached = captor.getValue();
            assertNotNull(cached.getExpiryTime());
            assertArrayEquals("Hello".getBytes(), cached.toResponseEntity().getBody());
        }
    }

    @Test
    void testGetCachedResponseReturnsNullWhenExpired() {
        try (MockedStatic<UrlUtils> utilsMock = mockStatic(UrlUtils.class)) {
            utilsMock.when(() -> UrlUtils.extractSubdomain(request)).thenReturn("sub");

            JRoxyCacheServiceImpl.CachedResponse expired = new JRoxyCacheServiceImpl.CachedResponse(ResponseEntity.ok().body("Hello".getBytes()), 1); // TTL = 1 second
            // Manually set expiry in the past
            Instant past = Instant.now().minusSeconds(10);
            ReflectionTestUtils.setField(expired, "expiryTime", past);

            when(cache.get("GET:sub:/test")).thenReturn(expired);

            assertNull(service.getCachedResponse(request));
            verify(cache).evict("GET:sub:/test");
        }
    }

    @Test
    void testShouldNotCacheNoStore() {
        try (MockedStatic<UrlUtils> utilsMock = mockStatic(UrlUtils.class)) {
            utilsMock.when(() -> UrlUtils.extractSubdomain(request)).thenReturn("sub");

            HttpHeaders headers = new HttpHeaders();
            headers.setCacheControl("no-store");
            ResponseEntity<byte[]> response = ResponseEntity.ok().headers(headers).body("Hello".getBytes());

            service.putResponse(request, response);
            verify(cache, never()).put(anyString(), any());
        }
    }

    @Test
    void testShouldNotCachePrivate() {
        try (MockedStatic<UrlUtils> utilsMock = mockStatic(UrlUtils.class)) {
            utilsMock.when(() -> UrlUtils.extractSubdomain(request)).thenReturn("sub");

            HttpHeaders headers = new HttpHeaders();
            headers.setCacheControl("private, max-age=100");
            ResponseEntity<byte[]> response = ResponseEntity.ok().headers(headers).body("Hello".getBytes());

            service.putResponse(request, response);
            verify(cache, never()).put(anyString(), any());
        }
    }

    @Test
    void testImmutableCache() {
        try (MockedStatic<UrlUtils> utilsMock = mockStatic(UrlUtils.class)) {
            utilsMock.when(() -> UrlUtils.extractSubdomain(request)).thenReturn("sub");

            HttpHeaders headers = new HttpHeaders();
            headers.setCacheControl("immutable");
            ResponseEntity<byte[]> response = ResponseEntity.ok().headers(headers).body("Hello".getBytes());

            service.putResponse(request, response);

            ArgumentCaptor<JRoxyCacheServiceImpl.CachedResponse> captor = ArgumentCaptor.forClass(JRoxyCacheServiceImpl.CachedResponse.class);
            verify(cache).put(eq("GET:sub:/test"), captor.capture());

            JRoxyCacheServiceImpl.CachedResponse cached = captor.getValue();
            assertTrue(cached.getExpiryTime().isAfter(Instant.now().plusSeconds(365 * 24 * 3600 - 10))); // roughly 1 year
        }
    }

}