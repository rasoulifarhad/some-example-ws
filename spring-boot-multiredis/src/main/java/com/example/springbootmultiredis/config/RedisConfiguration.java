package com.example.springbootmultiredis.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class RedisConfiguration {
    
    @Autowired
    private Redis1Properties redis1Properties;

    @Primary
    @Bean(name = "redis1ConnectionFactory")
    public LettuceConnectionFactory redisConnectionFactory() {

        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(
                redis1Properties.getHost(),
                redis1Properties.getPort()
        );
        config.setDatabase(redis1Properties.getDatabase());                    
        ;
        LettuceConnectionFactory factory = new LettuceConnectionFactory(config)   ;
        return factory; 
    }

    @Bean(name = "redis1RedisTemplate")
    @Primary
    public RedisTemplate<?,?> redisTemplate(@Qualifier("redis1ConnectionFactory") RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<?,?> redisTemplate = new RedisTemplate<>() ;
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

    @Bean(name = "redis1StringRedisTemplate")
    @Primary
    public StringRedisTemplate stringRedisTemplate(@Qualifier("redis1ConnectionFactory") RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate redisTemplate = new StringRedisTemplate() ;
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }
}
