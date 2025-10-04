package com.belhaddou.jroxy.configuration;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.expiry.Duration;
import org.ehcache.expiry.Expirations;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class EhCacheConfig {
    private final String HTTP_CACHE = "httpCache";


    @Bean
    public CacheManager ehCacheManager() {
        CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .withCache(HTTP_CACHE,
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(
                                        String.class,
                                        Object.class,
                                        ResourcePoolsBuilder.heap(1000))
                                .withExpiry(Expirations.timeToLiveExpiration(Duration.of(10, TimeUnit.SECONDS)))
                )
                .build(true);

        return cacheManager;
    }

    @Bean
    public Cache<String, Object> ehCache(CacheManager ehCacheManager) {
        return ehCacheManager.getCache(HTTP_CACHE, String.class, Object.class);

    }
}