package com.example.demomultispdredis.Config;

import java.util.Map;

import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@ConfigurationProperties(prefix = "spring.redis")
public class MultiRedisProperties {

    public static final String DEFAULT= "default";
    private boolean enableMulti= false ;
    @NestedConfigurationProperty
    private Map<String,RedisProperties> multi ;
    
}
