package com.belhaddou.jroxy.service.loadbalancer.impl;

import com.belhaddou.jroxy.model.ServiceConfig;
import com.belhaddou.jroxy.service.loadbalancer.LoadBalancerContext;
import com.belhaddou.jroxy.service.loadbalancer.LoadBalancerStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LoadBalancerContextImpl implements LoadBalancerContext {

    @Value("${proxy.load-balancing.strategy}")
    private String configuredStrategy;

    private Map<String, LoadBalancerStrategy> strategyMap;

    public ServiceConfig chooseInstance(List<ServiceConfig> instances) {
        LoadBalancerStrategy strategy = strategyMap.get(configuredStrategy);
        return strategy.select(instances);
    }

}
