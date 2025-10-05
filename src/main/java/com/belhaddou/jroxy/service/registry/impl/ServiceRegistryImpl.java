package com.belhaddou.jroxy.service.registry.impl;

import com.belhaddou.jroxy.configuration.JRoxyConfig;
import com.belhaddou.jroxy.model.InstanceWithHealth;
import com.belhaddou.jroxy.service.registry.ServiceRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ServiceRegistryImpl implements ServiceRegistry {
    private final JRoxyConfig jRoxyConfig;
    private Map<String, List<InstanceWithHealth>> registry;

    @Autowired
    public ServiceRegistryImpl(JRoxyConfig jRoxyConfig) {
        this.jRoxyConfig = jRoxyConfig;
        registry = new ConcurrentHashMap<>();
    }

    @PostConstruct
    public void registerServices() {
        jRoxyConfig.getServices()
                .forEach(service -> {
                    //TODO: add logic to verify if the instance is healthy, considering it true for the MVP
                    List<InstanceWithHealth> instanceWithHealths = service.getHosts()
                            .stream()
                            .map(host -> new InstanceWithHealth(host, true))
                            .toList();
                    //TODO: resolve the service name from the domain
                    registry.put(service.getName(), instanceWithHealths);
                });
    }

    public Map<String, List<InstanceWithHealth>> getRegistry() {
        return registry;
    }
}
