package com.belhaddou.jroxy.service.loadbalancer.context.impl;

import com.belhaddou.jroxy.configuration.JRoxyConfig;
import com.belhaddou.jroxy.model.InstanceWithHealth;
import com.belhaddou.jroxy.service.loadbalancer.context.LoadBalancerContext;
import com.belhaddou.jroxy.service.loadbalancer.strategy.LoadBalancerStrategy;
import com.belhaddou.jroxy.service.registry.ServiceRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LoadBalancerContextImpl implements LoadBalancerContext {

    private final JRoxyConfig jRoxyConfig;
    private final Map<String, LoadBalancerStrategy> strategyMap;
    private final ServiceRegistry serviceRegistry;

    public JRoxyConfig.Host chooseInstance(String subdomain) {
        List<InstanceWithHealth> instanceWithHealth = serviceRegistry.getRegistry()
                .get(subdomain);

        JRoxyConfig.Services service = jRoxyConfig.getServices()
                .stream().filter(s -> subdomain.equals(s.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("subdomain : " + subdomain + " is not part of the configuration !"));

        List<JRoxyConfig.Host> hosts = instanceWithHealth.stream()
                .filter(instance -> instance.getHealthy() == true)
                .map(InstanceWithHealth::getHost)
                .toList();

        String defaultStrategy = service.getLoadBalancer() == null ? jRoxyConfig.getDefaultLoadBalancing()
                : service.getLoadBalancer();

        LoadBalancerStrategy strategy = strategyMap.get(defaultStrategy);

        if (strategy == null) {
            throw new IllegalArgumentException("Strategy " + strategy + "is not yet supported !");
        }

        return strategy.select(hosts);
    }

}
