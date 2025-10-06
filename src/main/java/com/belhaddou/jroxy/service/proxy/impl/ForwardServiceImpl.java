package com.belhaddou.jroxy.service.proxy.impl;

import com.belhaddou.jroxy.configuration.JRoxyConfig;
import com.belhaddou.jroxy.model.InstanceWithHealth;
import com.belhaddou.jroxy.service.loadbalancer.context.LoadBalancerContext;
import com.belhaddou.jroxy.service.proxy.ForwardsService;
import com.belhaddou.jroxy.service.registry.ServiceRegistry;
import com.belhaddou.jroxy.util.UrlUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ForwardServiceImpl implements ForwardsService {

    private final LoadBalancerContext loadBalancerContext;
    private final RestTemplate restTemplate;
    private final ServiceRegistry serviceRegistry;
    private final JRoxyConfig jRoxyConfig;

    public ResponseEntity<byte[]> forward(HttpServletRequest request, byte[] body) throws IOException {
        // get host

        // extract the subdomain
        String subdomain = UrlUtils.extractSubdomain(request, jRoxyConfig.getListen().getAddress());
        log.debug("extracting subdomain from url : {}", subdomain);

        if (subdomain.isEmpty()) {
            log.debug("No subdomain found, hence returning BAD_GATEWAY !");
            return new ResponseEntity<>(HttpStatus.BAD_GATEWAY);
        }

        List<InstanceWithHealth> totalHosts = serviceRegistry.getRegistry().get(subdomain);
        //max attempts equals to the number of availalbe instances
        Integer maxAttempts = totalHosts != null ? totalHosts.size() : 0;
        Integer attempt = 0;

        log.debug("Going to proxy request to service: {}", subdomain);

        while (attempt < maxAttempts) {
            try {
                return getResponseEntity(request, subdomain, body);
            } catch (RestClientException ex) {
                log.warn("Attempt {} failed for subdomain {}: {}", attempt + 1, subdomain, ex.getMessage());
                attempt++;
                if (attempt == maxAttempts) {
                    log.error("Max {} attempts reached, cannot reach any host for subdomain '{}'", maxAttempts, subdomain);
                    throw new RuntimeException("Could not reach destination after " + maxAttempts + " attempts", ex);
                }
            }
        }
        throw new IllegalStateException("Could not forward request !");
    }

    private ResponseEntity<byte[]> getResponseEntity(HttpServletRequest request, String subdomain, byte[] body) throws IOException {
        String path = request.getRequestURI();
        String query = request.getQueryString();
        JRoxyConfig.Host targetHost = loadBalancerContext.chooseInstance(subdomain);
        log.debug("Load balancer have selected the host : {}:{}", targetHost.getAddress(), targetHost.getPort());

        String targetUrl = String.format("http://%s:%d%s%s",
                targetHost.getAddress(),
                targetHost.getPort(),
                path,
                (query != null ? "?" + query : ""));
        log.debug("Going to call target url: {}", targetUrl);

        HttpHeaders headers = UrlUtils.extractHeaders(request);
        HttpEntity<byte[]> requestHttpEntity = new HttpEntity<>(body, headers);

        ResponseEntity<byte[]> response = restTemplate.exchange(
                targetUrl,
                HttpMethod.valueOf(request.getMethod()),
                requestHttpEntity,
                byte[].class
        );
        return response;
    }
}
