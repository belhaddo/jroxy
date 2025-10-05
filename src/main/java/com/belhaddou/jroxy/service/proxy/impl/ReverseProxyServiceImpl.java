package com.belhaddou.jroxy.service.proxy.impl;

import com.belhaddou.jroxy.configuration.JRoxyConfig;
import com.belhaddou.jroxy.service.loadbalancer.context.LoadBalancerContext;
import com.belhaddou.jroxy.service.proxy.ReverseProxyService;
import com.belhaddou.jroxy.service.registry.ServiceRegistry;
import com.belhaddou.jroxy.util.UrlUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReverseProxyServiceImpl implements ReverseProxyService<byte[]> {
    private final LoadBalancerContext loadBalancerContext;
    private final RestTemplate restTemplate;
    private final ServiceRegistry serviceRegistry;
    @Value("${server.host:127.0.0.1}")
    private String baseHost = "127.0.0.1";

    @Override
    public ResponseEntity<byte[]> proxyForward(String hostHeader, String path, String query,
                                               byte[] body,
                                               String method) {
        String subdomain = UrlUtils.extractSubdomain(hostHeader, baseHost);
        //max attempts equals to the number of availalbe instances
        Integer maxAttempts = serviceRegistry.getRegistry().get(subdomain).size();
        Integer attempts = 1;
        ResponseEntity<byte[]> response = null;

        while (attempts < maxAttempts) {
            try {
                return getResponseEntity(path, query, body, method, subdomain);
            } catch (RestClientException ex) {
                log.debug("Attempt {} failed for subdomain {}: {}", attempts, subdomain, ex.getMessage());
                attempts++;
                if (attempts == maxAttempts) {
                    throw new RuntimeException("Could not reach destination after " + maxAttempts + " attempts", ex);
                }
            }
        }
        throw new IllegalStateException("Could not forward request !");
    }

    private ResponseEntity<byte[]> getResponseEntity(String path, String query, byte[] body, String method, String subdomain) {
        JRoxyConfig.Host targetHost = loadBalancerContext.chooseInstance(subdomain);

        String targetUrl = String.format("http://%s:%d/%s%s%s",
                targetHost.getAddress(),
                targetHost.getPort(),
                subdomain,
                path,
                (query != null ? "?" + query : ""));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<byte[]> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<byte[]> response = restTemplate.exchange(
                targetUrl,
                HttpMethod.valueOf(method),
                requestEntity,
                byte[].class
        );
        return response;
    }
}
