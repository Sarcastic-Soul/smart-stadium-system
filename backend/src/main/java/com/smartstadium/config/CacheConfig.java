package com.smartstadium.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Cache configuration using Caffeine for high-performance in-memory caching.
 *
 * <p>Crowd density and queue data are cached with a short TTL (30 seconds)
 * to reduce redundant computations while keeping data reasonably fresh.</p>
 */
@Configuration
public class CacheConfig {

    public static final String CROWD_DENSITY_CACHE = "crowdDensity";
    public static final String QUEUE_WAIT_TIME_CACHE = "queueWaitTime";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                CROWD_DENSITY_CACHE, QUEUE_WAIT_TIME_CACHE);
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.SECONDS)
                .maximumSize(100));
        return cacheManager;
    }
}
