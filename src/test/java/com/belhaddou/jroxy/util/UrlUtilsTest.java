package com.belhaddou.jroxy.util;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.util.Collections;
import java.util.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UrlUtilsTest {

    @Test
    void extractSubdomain() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Host")).thenReturn("my-service.my-company.com");
        String subdomain = UrlUtils.extractSubdomain(request);
        assertEquals("my-service", subdomain);
    }

    @Test
    void testExtractSubdomainWithSubdomain() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Host")).thenReturn("my-service.my-company.com");

        String subdomain = UrlUtils.extractSubdomain(request);

        assertEquals("my-service", subdomain);
    }

    @Test
    void testExtractSubdomainWithoutSubdomain() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Host")).thenReturn("my-company.com");

        String subdomain = UrlUtils.extractSubdomain(request);

        assertEquals("", subdomain);
    }

    @Test
    void testExtractSubdomainWithPort() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Host")).thenReturn("my-service.my-company.com:8080");

        String subdomain = UrlUtils.extractSubdomain(request);

        assertEquals("my-service", subdomain);
    }

    @Test
    void testExtractSubdomainNullHost() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Host")).thenReturn(null);

        String subdomain = UrlUtils.extractSubdomain(request);

        assertEquals("", subdomain);
    }

    @Test
    void testExtractHeadersSingleHeader() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Vector<String> headerNames = new Vector<>(Collections.singletonList("Content-Type"));
        Vector<String> headerValues = new Vector<>(Collections.singletonList("application/json"));

        when(request.getHeaderNames()).thenReturn(headerNames.elements());
        when(request.getHeaders("Content-Type")).thenReturn(headerValues.elements());

        HttpHeaders headers = UrlUtils.extractHeaders(request);

        assertTrue(headers.containsKey("Content-Type"));
        assertEquals("application/json", headers.getFirst("Content-Type"));
    }

    @Test
    void testExtractHeadersMultipleHeaders() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Vector<String> headerNames = new Vector<>();
        headerNames.add("Accept");
        headerNames.add("Authorization");

        Vector<String> acceptValues = new Vector<>(Collections.singletonList("application/json"));
        Vector<String> authValues = new Vector<>(Collections.singletonList("Bearer token"));

        when(request.getHeaderNames()).thenReturn(headerNames.elements());
        when(request.getHeaders("Accept")).thenReturn(acceptValues.elements());
        when(request.getHeaders("Authorization")).thenReturn(authValues.elements());

        HttpHeaders headers = UrlUtils.extractHeaders(request);

        assertEquals("application/json", headers.getFirst("Accept"));
        assertEquals("Bearer token", headers.getFirst("Authorization"));
        assertEquals(2, headers.size());
    }
}