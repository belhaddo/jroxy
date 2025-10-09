package com.belhaddou.jroxy.service.proxy;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

public interface ForwardGetService<T> {
    ResponseEntity<byte[]> forwardGET(HttpServletRequest request, byte[] body) throws IOException;
}
