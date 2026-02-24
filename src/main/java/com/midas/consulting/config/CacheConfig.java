package com.midas.consulting.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import com.midas.consulting.model.Tenant;
import com.midas.consulting.security.SecurityConstants;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    private static final Logger logger = LoggerFactory.getLogger(CacheConfig.class);

    @Value("${tenant.cache.max-size:100}")
    private int maxSize;

    @Value("${tenant.cache.expire-after-access-minutes:60}")
    private int expireAfterAccessMinutes;

    @Value("${tenant.cache.expire-after-write-minutes:120}")
    private int expireAfterWriteMinutes;

    @Value("${tenant.cache.refresh-after-write-minutes:30}")
    private int refreshAfterWriteMinutes;

    @Bean
    public com.github.benmanes.caffeine.cache.Cache<String, Tenant> tenantCache() {
        return Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfter(new TenantCacheExpiry())
                .removalListener(new TenantCacheRemovalListener())
                .recordStats()
                .build();
    }

    // ADD THIS BEAN FOR IP WHITELIST CACHE
    @Bean
    public com.github.benmanes.caffeine.cache.Cache<String, Boolean> ipCache() {
        return Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(Duration.ofSeconds(SecurityConstants.IP_VALIDATION_CACHE_TTL))
                .recordStats()
                .build();
    }

    private static class TenantCacheExpiry implements Expiry<String, Tenant> {

        private final long defaultExpireTime = TimeUnit.HOURS.toNanos(1);

        @Override
        public long expireAfterCreate(String key, Tenant value, long currentTime) {
            return defaultExpireTime;
        }

        @Override
        public long expireAfterUpdate(String key, Tenant value, long currentTime, @NonNegative long currentDuration) {
            return defaultExpireTime;
        }

        @Override
        public long expireAfterRead(String key, Tenant value, long currentTime, @NonNegative long currentDuration) {
            return currentDuration;
        }
    }

    private static class TenantCacheRemovalListener implements RemovalListener<String, Tenant> {
        @Override
        public void onRemoval(@Nullable String key, @Nullable Tenant value, RemovalCause cause) {
            if (key != null) {
                logger.info("Tenant cache entry removed: tenantId={}, cause={}", key, cause);
            }
        }
    }

    private static class IpCacheRemovalListener implements RemovalListener<String, Boolean> {
        @Override
        public void onRemoval(@Nullable String key, @Nullable Boolean value, RemovalCause cause) {
            if (key != null) {
                logger.debug("IP cache entry removed: cacheKey={}, allowed={}, cause={}", key, value, cause);
            }
        }
    }
}