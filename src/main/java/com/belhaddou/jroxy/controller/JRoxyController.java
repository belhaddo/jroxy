package com.belhaddou.jroxy.controller;

import com.belhaddou.jroxy.service.proxy.ForwardsService;
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
public class JRoxyController {
    private final ForwardsService forwardsService;

    // Ideally each method should be in separate controller class
    @RequestMapping(
            value = "/**",
            method = {
                    RequestMethod.POST,
                    RequestMethod.PUT,
                    RequestMethod.PATCH,
                    RequestMethod.DELETE,
                    RequestMethod.OPTIONS,
                    RequestMethod.HEAD
            }
    )
    public ResponseEntity<byte[]> forward(HttpServletRequest request,
                                          @RequestBody(required = false) byte[] body) throws IOException {
        log.debug("Proxying {} request to {}", request.getMethod(), request.getRequestURI());
        return forwardsService.forward(request, body);


    }
}
