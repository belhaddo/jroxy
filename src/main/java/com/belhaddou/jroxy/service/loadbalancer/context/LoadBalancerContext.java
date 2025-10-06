package com.belhaddou.jroxy.service.loadbalancer.context;

import com.belhaddou.jroxy.configuration.JRoxyConfig;

public interface LoadBalancerContext {
    JRoxyConfig.Host chooseInstance(String name);
}
