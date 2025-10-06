package com.belhaddou.jroxy.controller;

import com.belhaddou.jroxy.service.proxy.ReverseProxyService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@Slf4j
public class JRoxyGetController {

    private final ReverseProxyService<byte[]> proxyGetService;

    // Get controller Separated because it has caching capability
    @RequestMapping(
            value = "/**",
            method = RequestMethod.GET
    )
    public ResponseEntity<byte[]> forwardGet(HttpServletRequest request,
                                             @RequestBody(required = false) byte[] body) throws IOException {
        log.debug("Proxying {} request to {}", request.getMethod(), request.getRequestURI());
        return proxyGetService.forwardGET(request, body);
    }
}
