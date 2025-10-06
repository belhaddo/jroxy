package com.belhaddou.jroxy.service.cache;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

public interface JRoxyCacheService {
    ResponseEntity<byte[]> getCachedResponse(HttpServletRequest request);

    void putResponse(HttpServletRequest request, ResponseEntity<byte[]> response);
}
