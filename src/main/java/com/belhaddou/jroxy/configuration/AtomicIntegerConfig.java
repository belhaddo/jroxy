package com.belhaddou.jroxy.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class AtomicIntegerConfig {

    @Bean
    public AtomicInteger counter() {
        return new AtomicInteger();
    }
}
