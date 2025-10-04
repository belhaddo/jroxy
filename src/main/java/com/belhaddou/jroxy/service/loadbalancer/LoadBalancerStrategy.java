package com.belhaddou.jroxy.service.loadbalancer;

import com.belhaddou.jroxy.model.ServiceConfig;

import java.util.List;

public interface LoadBalancerStrategy {

    ServiceConfig select(List<ServiceConfig> instances);
}
