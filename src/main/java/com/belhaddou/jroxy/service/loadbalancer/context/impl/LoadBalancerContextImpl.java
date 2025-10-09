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
    // This map is built by Spring boot at runtime to load all Load balancing strategies and its implementation
    private final Map<String, LoadBalancerStrategy> strategyMap;
    private final ServiceRegistry serviceRegistry;

    public JRoxyConfig.Host chooseInstance(String subdomain) {
        // Retrieving Hosts for the specific subdomain
        List<InstanceWithHealth> instanceWithHealth = serviceRegistry.getRegistry()
                .get(subdomain);
        //Making sure that instances are present
        if (instanceWithHealth == null) {
            throw new IllegalArgumentException("Could not load from registry instances for service : " + subdomain);
        }
        // Filtering the Instances which are UP
        List<JRoxyConfig.Host> hosts = instanceWithHealth.stream()
                .filter(instance -> instance.getHealthy() == true)
                .map(InstanceWithHealth::getHost)
                .toList();

        // Getting Service Configuration to get Load balancing strategy
        JRoxyConfig.Services service = jRoxyConfig.getServices()
                .stream().filter(s -> subdomain.equals(s.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("subdomain : " + subdomain + " is not part of the configuration !"));
        log.debug("Load balancer found {} number of host are available for service {}", hosts.size(), service.getName());

        // Checking if the default or service specific load balancing strategy will be used
        String strategy = service.getLoadBalancer() == null ? jRoxyConfig.getDefaultLoadBalancing()
                : service.getLoadBalancer();
        log.debug("Going to use Load balancing strategy : {}", strategy);

        // Retrieving the load balancing strategy implementation
        LoadBalancerStrategy selectedStrategy = strategyMap.get(strategy);
        // If the strategy is not found an exception is raised
        if (selectedStrategy == null) {
            throw new JRoxyIllegalArgumentException("Strategy " + strategy + "is not yet supported !");
        }
        // Select the Host
        return selectedStrategy.select(hosts);
    }

}
