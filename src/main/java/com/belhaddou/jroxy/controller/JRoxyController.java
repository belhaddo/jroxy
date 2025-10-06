package com.belhaddou.jroxy.controller;

import com.belhaddou.jroxy.service.proxy.ForwardsService;
import com.belhaddou.jroxy.service.proxy.ReverseProxyService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@Slf4j
public class JRoxyController {
    private final ReverseProxyService<byte[]> reverseProxyService;
    private final ForwardsService forwardsService;
    @RequestMapping(
            value = "/**",
            method = {
                    RequestMethod.GET,
                    RequestMethod.POST,
                    RequestMethod.PUT,
                    RequestMethod.PATCH,
                    RequestMethod.DELETE,
                    RequestMethod.OPTIONS,
                    RequestMethod.HEAD
            }
    )
    public ResponseEntity<byte[]> forwardOtherMethods(HttpServletRequest request) throws IOException {
        String method = request.getMethod();
        log.debug("Proxying {} request to {}", method, request.getRequestURI());

        switch (method) {
            case "GET":
                return reverseProxyService.forwardGET(request);
            case "POST", "PATCH", "PUT", "DELETE", "OPTIONS", "HEAD":
                return forwardsService.proxyForward(request);
            default:
                log.warn("Unsupported method: {}", method);
                return ResponseEntity.status(405).build();
        }
    }
}
