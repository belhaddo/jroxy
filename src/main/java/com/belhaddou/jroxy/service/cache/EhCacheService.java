package com.belhaddou.jroxy.service.cache;

public interface EhCacheService<T> {
    T get(String key);

    void put(String key, T value);

    void evict(String key);
}
