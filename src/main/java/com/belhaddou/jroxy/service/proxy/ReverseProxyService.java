package com.belhaddou.jroxy.service.proxy;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

public interface ReverseProxyService<T> {
    ResponseEntity<byte[]> forwardGET(HttpServletRequest request);
}
