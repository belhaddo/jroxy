package com.belhaddou.jroxy.service.loadbalancer.strategy.impl;

import com.belhaddou.jroxy.configuration.JRoxyConfig;
import com.belhaddou.jroxy.service.loadbalancer.strategy.LoadBalancerStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service("roundRobinStrategy")
public class RoundRobinStrategyImpl implements LoadBalancerStrategy {
    private final AtomicInteger counter;

    @Autowired
    public RoundRobinStrategyImpl() {
        this.counter = new AtomicInteger(0);
    }

    //AI Generated
    @Override
    public JRoxyConfig.Host select(List<JRoxyConfig.Host> instances) {
        int index = Math.abs(counter.getAndIncrement() % instances.size());
        return instances.get(index);
    }
}
