package com.belhaddou.jroxy.service.proxy;

import org.springframework.http.ResponseEntity;

public interface ReverseProxyService<T> {
    public ResponseEntity<byte[]> proxyForward(String hostHeader, String path, String query,
                                               byte[] body,
                                               String method);
}
