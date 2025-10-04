package com.belhaddou.jroxy.model;

import lombok.Data;

@Data
public class Listen {
    private String address;
    private int port;
}