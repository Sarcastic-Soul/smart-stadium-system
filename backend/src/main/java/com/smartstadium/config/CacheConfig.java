package com.smartstadium.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cache configuration using in-memory ConcurrentMapCacheManager.
 * This avoids any external dependency (like Redis) and works
 * seamlessly on Cloud Run without VPC connectors.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String CROWD_DENSITY_CACHE = "crowdDensity";
    public static final String QUEUE_WAIT_TIME_CACHE = "queueWaitTime";

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(CROWD_DENSITY_CACHE, QUEUE_WAIT_TIME_CACHE);
    }
}
