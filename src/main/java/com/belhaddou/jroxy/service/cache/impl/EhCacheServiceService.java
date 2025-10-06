package com.belhaddou.jroxy.service.cache.impl;

import lombok.RequiredArgsConstructor;
import org.ehcache.Cache;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EhCacheServiceService<Object> implements com.belhaddou.jroxy.service.cache.EhCacheService<Object> {

    @Qualifier("ehCache")
    private final Cache<String, Object> cache;

    @Override
    public Object get(String key) {
        return cache.get(key);
    }

    @Override
    public void put(String key, Object value) {
        cache.put(key, value);

    }

    @Override
    public void evict(String key) {
        cache.remove(key);
    }
}
