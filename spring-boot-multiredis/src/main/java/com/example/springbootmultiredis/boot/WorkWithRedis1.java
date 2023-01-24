package com.example.springbootmultiredis.boot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@Order(1)
public class WorkWithRedis1 implements CommandLineRunner {

    @Autowired
    @Qualifier("redis1RedisTemplate")
    private RedisTemplate<String,String> redisTemplate ;

    @Autowired
    @Qualifier("redis1StringRedisTemplate")
    private StringRedisTemplate stringRedisTemplate ;

    @Override
    public void run(String... args) throws Exception {
        for (int i = 0; i < 10; i++) {
            String key = "redis1:key:" + i ;
            String value = "redis1:value:" + i ;
            stringRedisTemplate.opsForValue().set(key, value);
            
            String redis1Value = stringRedisTemplate.opsForValue().get(key);

            log.info("Read from redis1: key {} value is {}",key,redis1Value);
        }
    }

    
}
