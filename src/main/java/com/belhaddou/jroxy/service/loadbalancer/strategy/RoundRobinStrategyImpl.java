package com.belhaddou.jroxy.service.loadbalancer.strategy;

import com.belhaddou.jroxy.model.ServiceConfig;
import com.belhaddou.jroxy.service.loadbalancer.LoadBalancerStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service("roundRobinStrategy")
@RequiredArgsConstructor
public class RoundRobinStrategyImpl implements LoadBalancerStrategy {
    private final AtomicInteger counter;

    @Override
    public ServiceConfig select(List<ServiceConfig> instances) {
        int index = Math.abs(counter.getAndIncrement() % instances.size());
        return instances.get(index);
    }
}
