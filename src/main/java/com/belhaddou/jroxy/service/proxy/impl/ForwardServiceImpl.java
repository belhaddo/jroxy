package com.belhaddou.jroxy.service.proxy.impl;

import com.belhaddou.jroxy.configuration.JRoxyConfig;
import com.belhaddou.jroxy.exception.JRoxyIllegalArgumentException;
import com.belhaddou.jroxy.exception.JRoxyRuntimeException;
import com.belhaddou.jroxy.service.loadbalancer.context.LoadBalancerContext;
import com.belhaddou.jroxy.service.proxy.ForwardsService;
import com.belhaddou.jroxy.service.registry.ServiceRegistry;
import com.belhaddou.jroxy.util.UrlUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ForwardServiceImpl implements ForwardsService {

    private final LoadBalancerContext loadBalancerContext;
    private final RestTemplate restTemplate;
    private final ServiceRegistry serviceRegistry;
    private final JRoxyConfig jRoxyConfig;

    public ResponseEntity<byte[]> forward(HttpServletRequest request, byte[] body) throws IOException {
        // Extracting Subdomain to determine to which upstream service it will be sent
        String subdomain = UrlUtils.extractSubdomain(request);
        log.debug("extracting subdomain from url : {}", subdomain);

        if (subdomain.isEmpty()) {
            log.debug("No subdomain found, hence returning BAD_GATEWAY !");
            return new ResponseEntity<>(HttpStatus.BAD_GATEWAY);
        }

        // Getting maxAttempts number from configuration, otherwise its 3
        Integer maxAttempts = jRoxyConfig.getRetries() == null ? 3 : Integer.valueOf(jRoxyConfig.getRetries());

        Integer attempt = 0;

        log.debug("Going to proxy request to service: {}", subdomain);
        // Retry policy if one host is not reachable
        while (attempt < maxAttempts) {
            try {
                // Executing the Http call
                return getResponseEntity(request, subdomain, body);
            } catch (ResourceAccessException ex) {
                log.warn("Attempt {} failed for subdomain {}: {}", attempt + 1, subdomain, ex.getMessage());
                attempt++;
                // if max attempts retries is reached, an exception is raised
                if (attempt == maxAttempts) {
                    log.error("Max {} attempts reached, cannot reach any host for subdomain '{}'", maxAttempts, subdomain);
                    throw new JRoxyRuntimeException("Could not reach destination after " + maxAttempts + " attempts", ex);
                }
            }
        }
        throw new JRoxyRuntimeException("Could not forward request !");
    }

    private ResponseEntity<byte[]> getResponseEntity(HttpServletRequest request, String subdomain, byte[] body) throws IOException {
        // Getting request path
        String path = request.getRequestURI();
        // Getting Query parameters
        String query = request.getQueryString();
        // Select an instance with load balancer
        JRoxyConfig.Host targetHost = loadBalancerContext.chooseInstance(subdomain);
        log.debug("Load balancer have selected the host : {}:{}", targetHost.getAddress(), targetHost.getPort());
        // if no instance is found an exception is raised
        if (targetHost == null) {
            throw new JRoxyIllegalArgumentException("Load balancer could not select a host !");
        }
        // Building the target url
        String targetUrl = String.format("http://%s:%d%s%s",
                targetHost.getAddress(),
                targetHost.getPort(),
                path,
                (query != null ? "?" + query : ""));
        log.debug("Going to call target url: {}", targetUrl);
        // Extracting Header from downstream client request
        HttpHeaders headers = UrlUtils.extractHeaders(request);
        HttpEntity<byte[]> requestHttpEntity = new HttpEntity<>(body, headers);

        // Sending the request to upstream service with the same headers and body and method received from downstream client
        ResponseEntity<byte[]> response = restTemplate.exchange(
                targetUrl,
                HttpMethod.valueOf(request.getMethod()),
                requestHttpEntity,
                byte[].class
        );
        return response;
    }
}
