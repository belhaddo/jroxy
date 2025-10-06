package com.belhaddou.jroxy.service.proxy;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

public interface ForwardsService {

    ResponseEntity<byte[]> forward(HttpServletRequest request, byte[] body) throws IOException;
}
