package com.example.springbootmultiredis.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class Redis2Configuration {
    
    @Autowired
    private Redis2Properties redis2Properties;

    @Bean(name = "redis2ConnectionFactory")
    public LettuceConnectionFactory redisConnectionFactory() {

        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(
                redis2Properties.getHost(),
                redis2Properties.getPort()
        );
        config.setDatabase(redis2Properties.getDatabase());                    
        ;
        LettuceConnectionFactory factory = new LettuceConnectionFactory(config)   ;
        return factory; 
    }

    @Bean(name = "redis2RedisTemplate")
    public RedisTemplate<?,?> redisTemplate(@Qualifier("redis2ConnectionFactory") RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<?,?> redisTemplate = new RedisTemplate<>() ;
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

    @Bean(name = "redis2StringRedisTemplate")
    public StringRedisTemplate stringRedisTemplate(@Qualifier("redis2ConnectionFactory") RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate redisTemplate = new StringRedisTemplate() ;
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }


}
