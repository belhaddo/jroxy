package com.belhaddou.jroxy.service.loadbalancer.strategy.impl;

import com.belhaddou.jroxy.configuration.JRoxyConfig;
import com.belhaddou.jroxy.service.loadbalancer.strategy.LoadBalancerStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service("randomStrategy")
public class RandomStrategyImpl implements LoadBalancerStrategy {
    private final Random random;

    @Autowired
    public RandomStrategyImpl() {
        this.random = new Random();
    }

    @Override
    public JRoxyConfig.Host select(List<JRoxyConfig.Host> instances) {
        return instances.get(random.nextInt(instances.size()));
    }
}
