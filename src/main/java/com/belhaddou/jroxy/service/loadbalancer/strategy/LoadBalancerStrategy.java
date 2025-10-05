package com.belhaddou.jroxy.service.loadbalancer.strategy;

import com.belhaddou.jroxy.configuration.JRoxyConfig;

import java.util.List;

public interface LoadBalancerStrategy {

    JRoxyConfig.Host select(List<JRoxyConfig.Host> instances);
}
