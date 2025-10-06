package com.belhaddou.jroxy.service.proxy.impl;

import com.belhaddou.jroxy.service.proxy.ForwardsService;
import com.belhaddou.jroxy.service.proxy.ReverseProxyService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetProxyServiceImpl implements ReverseProxyService<byte[]> {

    private final ForwardsService forwardsService;

    @Override
    public ResponseEntity<byte[]> forwardGET(HttpServletRequest request) throws IOException {
        //TODO: add caching capability
        return forwardsService.forward(request);
    }

}
