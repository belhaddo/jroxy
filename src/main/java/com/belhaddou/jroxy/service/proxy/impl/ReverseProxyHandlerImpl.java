package com.belhaddou.jroxy.service.proxy.impl;

import com.belhaddou.jroxy.service.proxy.ReverseProxyHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ReverseProxyHandlerImpl<T> implements ReverseProxyHandler<T> {

    @Override
    public Mono<ResponseEntity<T>> dispatch(ServerHttpRequest request) {
        return null;
    }
}
