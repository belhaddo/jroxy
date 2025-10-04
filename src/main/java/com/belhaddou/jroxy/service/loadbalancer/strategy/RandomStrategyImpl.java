package com.belhaddou.jroxy.service.loadbalancer.strategy;

import com.belhaddou.jroxy.model.ServiceConfig;
import com.belhaddou.jroxy.service.loadbalancer.LoadBalancerStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service("randomStrategy")
@RequiredArgsConstructor
public class RandomStrategyImpl implements LoadBalancerStrategy {
    private final Random random;

    @Override
    public ServiceConfig select(List<ServiceConfig> instances) {
        return instances.get(random.nextInt(instances.size()));
    }
}
