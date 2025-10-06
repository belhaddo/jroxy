package com.belhaddou.jroxy.service.loadbalancer.context.impl;

import com.belhaddou.jroxy.configuration.JRoxyConfig;
import com.belhaddou.jroxy.exception.JRoxyIllegalArgumentException;
import com.belhaddou.jroxy.model.InstanceWithHealth;
import com.belhaddou.jroxy.service.loadbalancer.context.LoadBalancerContext;
import com.belhaddou.jroxy.service.loadbalancer.strategy.LoadBalancerStrategy;
import com.belhaddou.jroxy.service.registry.ServiceRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
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
        log.debug("Load balancer found {} number of host are available for service {}", hosts.size(), service.getName());

        String strategy = service.getLoadBalancer() == null ? jRoxyConfig.getDefaultLoadBalancing()
                : service.getLoadBalancer();
        log.debug("Going to use Load balancing strategy : {}", strategy);

        LoadBalancerStrategy selectedStrategy = strategyMap.get(strategy);

        if (selectedStrategy == null) {
            throw new JRoxyIllegalArgumentException("Strategy " + strategy + "is not yet supported !");
        }

        return selectedStrategy.select(hosts);
    }

}
