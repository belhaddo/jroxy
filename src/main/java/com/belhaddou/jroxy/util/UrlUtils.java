package com.belhaddou.jroxy.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;

import java.util.Collections;
import java.util.Enumeration;

@Slf4j
public class UrlUtils {
    /***
     *
     * @param request
     * @param baseHost
     * @return subdomain
     */

    public static String extractSubdomain(HttpServletRequest request, String baseHost) {
        String host = request.getHeader("Host");
        log.debug("extracting host from url : {}", host);
        if (host == null) return "";
        String fullDomain = host.split(":")[0];
        String[] parts = fullDomain.split("\\.");
        if (parts.length >= 3) {
            return parts[0];
        }
        return "";
    }

    /**
     *
     * @param request
     * @return
     */
    public static HttpHeaders extractHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            Enumeration<String> values = request.getHeaders(name);
            headers.put(name, Collections.list(values));
        }
        return headers;
    }
}
