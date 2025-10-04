package com.belhaddou.jroxy.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.net.http.HttpHeaders;

@Data
@AllArgsConstructor
public class JRoxyHttpResponse {
    private byte[] body;
    private HttpHeaders headers;
    private HttpStatus status;
    private String etag;
}