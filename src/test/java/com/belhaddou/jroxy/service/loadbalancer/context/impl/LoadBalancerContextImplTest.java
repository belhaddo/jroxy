package com.belhaddou.jroxy.service.loadbalancer.context.impl;

import com.belhaddou.jroxy.configuration.JRoxyConfig;
import com.belhaddou.jroxy.exception.JRoxyIllegalArgumentException;
import com.belhaddou.jroxy.model.InstanceWithHealth;
import com.belhaddou.jroxy.service.loadbalancer.strategy.LoadBalancerStrategy;
import com.belhaddou.jroxy.service.registry.ServiceRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// AI Generated
class LoadBalancerContextImplTest {

    @Mock
    private JRoxyConfig jRoxyConfig;

    @Mock
    private ServiceRegistry serviceRegistry;

    @Mock
    private LoadBalancerStrategy loadBalancerStrategy;

    @InjectMocks
    private LoadBalancerContextImpl loadBalancerContext;

    private final Map<String, LoadBalancerStrategy> strategyMap = new HashMap<>();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        strategyMap.put("round-robin", loadBalancerStrategy);
        loadBalancerContext = new LoadBalancerContextImpl(jRoxyConfig, strategyMap, serviceRegistry);
    }

    @Test
    void testChooseInstanceSuccessful() {
        String subdomain = "orders";

        // Mock Host
        JRoxyConfig.Host host1 = new JRoxyConfig.Host();
        ReflectionTestUtils.setField(host1, "address", "10.0.0.1");


        JRoxyConfig.Host host2 = new JRoxyConfig.Host();
        ReflectionTestUtils.setField(host2, "address", "10.0.0.1");

        // Mock healthy/unhealthy instances
        InstanceWithHealth healthyInstance = mock(InstanceWithHealth.class);
        when(healthyInstance.getHealthy()).thenReturn(true);
        when(healthyInstance.getHost()).thenReturn(host1);

        InstanceWithHealth unhealthyInstance = mock(InstanceWithHealth.class);
        when(unhealthyInstance.getHealthy()).thenReturn(false);
        when(unhealthyInstance.getHost()).thenReturn(host2);

        List<InstanceWithHealth> instances = List.of(healthyInstance, unhealthyInstance);
        Map<String, List<InstanceWithHealth>> registry = Map.of(subdomain, instances);
        when(serviceRegistry.getRegistry()).thenReturn(registry);

        // Mock config service
        JRoxyConfig.Services service = new JRoxyConfig.Services();
        service.setName(subdomain);
        service.setLoadBalancer("round-robin");

        when(jRoxyConfig.getServices()).thenReturn(List.of(service));
        when(jRoxyConfig.getDefaultLoadBalancing()).thenReturn("round-robin");

        // Mock strategy selection
        when(loadBalancerStrategy.select(anyList())).thenReturn(host1);

        JRoxyConfig.Host chosenHost = loadBalancerContext.chooseInstance(subdomain);

        assertEquals("10.0.0.1", chosenHost.getAddress());
        verify(loadBalancerStrategy, times(1)).select(anyList());
    }

    @Test
    void testChooseInstanceUnsupportedStrategyThrowsException() {
        String subdomain = "payments";

        JRoxyConfig.Host host = new JRoxyConfig.Host();
        ReflectionTestUtils.setField(host, "address", "10.0.0.3");

        InstanceWithHealth instance = mock(InstanceWithHealth.class);
        when(instance.getHealthy()).thenReturn(true);
        when(instance.getHost()).thenReturn(host);

        Map<String, List<InstanceWithHealth>> registry = Map.of(subdomain, List.of(instance));
        when(serviceRegistry.getRegistry()).thenReturn(registry);

        JRoxyConfig.Services service = new JRoxyConfig.Services();
        service.setName(subdomain);
        service.setLoadBalancer("unsupported-strategy");

        when(jRoxyConfig.getServices()).thenReturn(List.of(service));

        JRoxyIllegalArgumentException exception = assertThrows(
                JRoxyIllegalArgumentException.class,
                () -> loadBalancerContext.chooseInstance(subdomain)
        );

        assertTrue(exception.getMessage().contains("unsupported-strategy"));
    }

    @Test
    void testChooseInstanceSubdomainNotInConfigThrowsException() {
        String subdomain = "inventory";

        Map<String, List<InstanceWithHealth>> registry = Map.of(subdomain, List.of());
        when(serviceRegistry.getRegistry()).thenReturn(registry);
        when(jRoxyConfig.getServices()).thenReturn(List.of()); // No matching service

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> loadBalancerContext.chooseInstance(subdomain)
        );

        assertTrue(exception.getMessage().contains("not part of the configuration"));
    }

}