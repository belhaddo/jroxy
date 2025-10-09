package com.belhaddou.jroxy.service.proxy.impl;

import com.belhaddou.jroxy.service.cache.JRoxyCacheService;
import com.belhaddou.jroxy.service.proxy.ForwardGetService;
import com.belhaddou.jroxy.service.proxy.ForwardsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetProxyServiceImpl implements ForwardGetService<byte[]> {

    private final ForwardsService forwardsService;
    private final JRoxyCacheService cacheService;

    @Override
    public ResponseEntity<byte[]> forwardGET(HttpServletRequest request, byte[] body) throws IOException {
        // measuring request time to debug caching performance
        long start = System.nanoTime();
        // if a response associated to the request is found then it will be returned from the cache
        ResponseEntity<byte[]> cached = cacheService.getCachedResponse(request);
        if (cached != null) {
            log.debug("Cache Hit: Execution time of GET is {} ms", (System.nanoTime() - start) / 1_000_000);
            return cached;
        }
        // if the response was't cached then a new request is sent to upstream service
        ResponseEntity<byte[]> response = forwardsService.forward(request, body);

        // the cache is updated with the response for future use
        if (response != null) {
            cacheService.putResponse(request, response);
        }

        log.debug("Cache Miss: Execution time of GET is {} ms", (System.nanoTime() - start) / 1_000_000);
        return response;
    }

}
