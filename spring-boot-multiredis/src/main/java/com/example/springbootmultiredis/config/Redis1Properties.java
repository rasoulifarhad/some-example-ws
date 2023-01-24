package com.example.springbootmultiredis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.redis")
public class Redis1Properties extends RedisCommonProperties {
    
}
