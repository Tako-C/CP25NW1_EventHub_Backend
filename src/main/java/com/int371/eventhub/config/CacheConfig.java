package com.int371.eventhub.config;

import java.util.concurrent.TimeUnit;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        cacheManager.registerCustomCache("registrationOtp",
                Caffeine.newBuilder()
                        .expireAfterWrite(5, TimeUnit.MINUTES)
                        .maximumSize(10000)
                        .build());

        cacheManager.registerCustomCache("registrationCooldown",
                Caffeine.newBuilder()
                        .expireAfterWrite(1, TimeUnit.MINUTES)
                        .maximumSize(10000)
                        .build());

        cacheManager.registerCustomCache("loginOtp",
                Caffeine.newBuilder()
                        .expireAfterWrite(5, TimeUnit.MINUTES)
                        .maximumSize(10000)
                        .build());
                        
        cacheManager.registerCustomCache("loginCooldown",
                Caffeine.newBuilder()
                        .expireAfterWrite(1, TimeUnit.MINUTES)
                        .maximumSize(10000)
                        .build());
        
        cacheManager.registerCustomCache("forgotPasswordOtp",
                Caffeine.newBuilder()
                        .expireAfterWrite(5, TimeUnit.MINUTES)
                        .maximumSize(10000)
                        .build());

        cacheManager.registerCustomCache("forgotPasswordCooldown",
                Caffeine.newBuilder()
                        .expireAfterWrite(5, TimeUnit.MINUTES)
                        .maximumSize(10000)
                        .build());

        return cacheManager;
    }
}