package com.belhaddou.jroxy.service.proxy.impl;

import com.belhaddou.jroxy.configuration.JRoxyConfig;
import com.belhaddou.jroxy.exception.JRoxyRuntimeException;
import com.belhaddou.jroxy.model.InstanceWithHealth;
import com.belhaddou.jroxy.service.loadbalancer.context.LoadBalancerContext;
import com.belhaddou.jroxy.service.registry.ServiceRegistry;
import com.belhaddou.jroxy.util.UrlUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ForwardServiceImplTest {

    @Mock
    private LoadBalancerContext loadBalancerContext;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private ServiceRegistry serviceRegistry;
    @Mock
    private JRoxyConfig jRoxyConfig;
    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private ForwardServiceImpl forwardService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        when(jRoxyConfig.getRetries()).thenReturn("3");
    }

    @Test
    void shouldReturnBadGatewayWhenSubdomainMissing() throws IOException {
        when(UrlUtils.extractSubdomain(request)).thenReturn("");
        ResponseEntity<byte[]> response = forwardService.forward(request, new byte[0]);
        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
    }

    @Test
    void shouldRetryExactNumberOfTimesBeforeFailing() throws IOException {
        // --- Mock inputs ---
        try (MockedStatic<UrlUtils> utility = Mockito.mockStatic(UrlUtils.class)) {
            utility.when(() -> UrlUtils.extractHeaders(any(HttpServletRequest.class))).thenReturn(new HttpHeaders());
            utility.when(() -> UrlUtils.extractSubdomain(any(HttpServletRequest.class))).thenReturn("test");
            when(UrlUtils.extractSubdomain(request)).thenReturn("test");
            when(request.getRequestURI()).thenReturn("/");
            when(request.getQueryString()).thenReturn(null);
            when(request.getMethod()).thenReturn("POST");


            JRoxyConfig.Host host = new JRoxyConfig.Host();
            host.setAddress("localhost");
            host.setPort(9090);
            when(loadBalancerContext.chooseInstance("test")).thenReturn(host);

            // registry
            Map<String, List<InstanceWithHealth>> registry = new HashMap<>();
            registry.put("test", List.of(mock(InstanceWithHealth.class)));
            when(serviceRegistry.getRegistry()).thenReturn(registry);

            // Always fail with ResourceAccessException'
            doThrow(new ResourceAccessException("Connection refused"))
                    .when(restTemplate).exchange(
                            anyString(),
                            eq(HttpMethod.POST),
                            any(HttpEntity.class),
                            eq(byte[].class)
                    );

            // --- Execute & Assert ---
            JRoxyRuntimeException exception =
                    assertThrows(JRoxyRuntimeException.class, () -> forwardService.forward(request, new byte[0]));

            assertTrue(exception.getMessage().contains("Could not reach destination after 3 attempts"));

            // Verify retry count = 3
            verify(restTemplate, times(3)).exchange(anyString(), eq(HttpMethod.POST), any(), eq(byte[].class));
        }
    }
}
