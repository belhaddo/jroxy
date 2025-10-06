package com.belhaddou.jroxy.service.proxy.impl;

import com.belhaddou.jroxy.service.cache.JRoxyCacheService;
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
    private final JRoxyCacheService cacheService;

    @Override
    public ResponseEntity<byte[]> forwardGET(HttpServletRequest request, byte[] body) throws IOException {
        long start = System.nanoTime();
        ResponseEntity<byte[]> cached = cacheService.getCachedResponse(request);
        if (cached != null) {
            log.debug("⏱️ Cache Hit: Execution time of GET is {} ms", (System.nanoTime() - start) / 1_000_000);
            return cached;
        }

        ResponseEntity<byte[]> response = forwardsService.forward(request, body);
        cacheService.putResponse(request, response);

        log.debug("⏱️ Cache Miss: Execution time of GET is {} ms", (System.nanoTime() - start) / 1_000_000);
        return response;
    }

}
