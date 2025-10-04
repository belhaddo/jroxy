package com.belhaddou.jroxy.service.loadbalancer;

import com.belhaddou.jroxy.model.ServiceConfig;

import java.util.List;

public interface LoadBalancerContext {
    ServiceConfig chooseInstance(List<ServiceConfig> instances);

}
