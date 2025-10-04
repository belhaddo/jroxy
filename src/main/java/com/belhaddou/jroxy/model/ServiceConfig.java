package com.belhaddou.jroxy.model;

import lombok.Data;

import java.util.List;

@Data
public class ServiceConfig {
    private String name;
    private String domain;
    private List<Host> hosts;
}