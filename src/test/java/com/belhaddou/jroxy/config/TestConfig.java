package com.belhaddou.jroxy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

//@Configuration
public class TestConfig {
    @Bean
    public RestTemplate testTestTemplate() {
        return new RestTemplate();
    }
}
