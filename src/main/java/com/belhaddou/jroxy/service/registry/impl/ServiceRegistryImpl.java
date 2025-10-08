package com.belhaddou.jroxy.service.registry.impl;

import com.belhaddou.jroxy.configuration.JRoxyConfig;
import com.belhaddou.jroxy.model.InstanceWithHealth;
import com.belhaddou.jroxy.service.registry.ServiceRegistry;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class ServiceRegistryImpl implements ServiceRegistry {
    private final JRoxyConfig jRoxyConfig;
    private final Map<String, List<InstanceWithHealth>> registry;

    @Autowired
    public ServiceRegistryImpl(JRoxyConfig jRoxyConfig) {
        this.jRoxyConfig = jRoxyConfig;
        registry = new ConcurrentHashMap<>();
    }

    @PostConstruct
    public void registerServices() {
        log.info("From config: {}", jRoxyConfig);
        List<JRoxyConfig.Services> services = jRoxyConfig.getServices();
        services.forEach(service -> {
            //TODO: add logic to verify if the instance is healthy, considering it true for the MVP
            List<InstanceWithHealth> instanceWithHealths = service.getHosts()
                    .stream()
                    .map(host -> new InstanceWithHealth(host, true))
                    .toList();
            registry.put(service.getName(), instanceWithHealths);
        });
        log.info("Service registry - loaded {} service : [{}]", registry.size(), String.join(",", registry.keySet()));

    }

    public Map<String, List<InstanceWithHealth>> getRegistry() {
        return registry;
    }
}
