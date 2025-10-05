package com.belhaddou.jroxy.service.proxy.impl;

import com.belhaddou.jroxy.configuration.JRoxyConfig;
import com.belhaddou.jroxy.service.loadbalancer.context.LoadBalancerContext;
import com.belhaddou.jroxy.service.proxy.ReverseProxyService;
import com.belhaddou.jroxy.util.UrlUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReverseProxyServiceImpl implements ReverseProxyService {
    private final LoadBalancerContext loadBalancerContext;

    @Override
    public ResponseEntity<byte[]> proxyGet(String path) {

        JRoxyConfig.Host selectedInstance = loadBalancerContext.chooseInstance(path);
        String url = UrlUtils.getUrl(selectedInstance);

        return null;
    }
}
