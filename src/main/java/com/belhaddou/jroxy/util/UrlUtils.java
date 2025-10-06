package com.belhaddou.jroxy.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;

import java.util.Collections;
import java.util.Enumeration;

public class UrlUtils {

    /**
     *
     * @param host
     * @return
     */

    public static String extractSubdomain(String host, String baseHost) {
        if (host == null) return "";
        // select only host part without port
        String cleanHost = host.split(":")[0];
        if (cleanHost.contains("." + baseHost)) {
            return cleanHost.substring(0, cleanHost.indexOf("." + baseHost));
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
