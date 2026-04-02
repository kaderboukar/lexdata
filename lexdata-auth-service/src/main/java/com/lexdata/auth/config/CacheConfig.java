package com.lexdata.auth.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuration du cache Caffeine pour la protection anti-brute-force.
 * Le cache "loginAttempts" stocke le nombre de tentatives échouées par IP.
 * Chaque entrée expire automatiquement après 15 minutes d'inactivité.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    private final BruteForceProtectionProperties properties;

    public CacheConfig(BruteForceProtectionProperties properties) {
        this.properties = properties;
    }

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("loginAttempts");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(properties.getLockDurationMinutes(), TimeUnit.MINUTES)
                .recordStats()); // utile pour le monitoring via Actuator
        return cacheManager;
    }
}
