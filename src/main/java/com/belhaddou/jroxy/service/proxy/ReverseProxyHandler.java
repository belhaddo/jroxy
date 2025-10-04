package com.belhaddou.jroxy.service.proxy;

import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServerHttpRequest;
import reactor.core.publisher.Mono;

public interface ReverseProxyHandler<T> {

    public Mono<ResponseEntity<T>> dispatch(ServerHttpRequest request);
}
