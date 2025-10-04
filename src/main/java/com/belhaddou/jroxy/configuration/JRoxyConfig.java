package com.belhaddou.jroxy.configuration;

import com.belhaddou.jroxy.model.Listen;
import com.belhaddou.jroxy.model.RateLimiterConfig;
import com.belhaddou.jroxy.model.ServiceConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "proxy")
@Data
public class JRoxyConfig {
    public class ProxyConfig {
        private Listen listen;
        private List<ServiceConfig> services;
        private RateLimiterConfig rateLimiter;
    }
}
