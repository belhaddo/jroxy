package com.belhaddou.jroxy.controller;

import com.belhaddou.jroxy.service.proxy.ReverseProxyService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class JRoxyController {
    private final ReverseProxyService<byte[]> reverseProxyService;

    @GetMapping("/**")
    public ResponseEntity<byte[]> forward(HttpServletRequest request,
                                          @RequestBody(required = false) byte[] body) {
        String path = request.getRequestURI();
        log.debug("Going to proxy request to : {}", path);
        return reverseProxyService.proxyGet(path);

    }

}
