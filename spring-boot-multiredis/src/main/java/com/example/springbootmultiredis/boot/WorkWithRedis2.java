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
@Order(2)
public class WorkWithRedis2 implements CommandLineRunner {

    @Autowired
    @Qualifier("redis2RedisTemplate")
    private RedisTemplate<String,String> redisTemplate ;

    @Autowired
    @Qualifier("redis2StringRedisTemplate")
    private StringRedisTemplate stringRedisTemplate ;


    @Override
    public void run(String... args) throws Exception {
        for (int i = 0; i < 10; i++) {
            String key = "redis2:key:" + i ;
            String value = "redis2:value:" + i ;
            stringRedisTemplate.opsForValue().set(key, value);
            
            String redis2Value = stringRedisTemplate.opsForValue().get(key);

            log.info("Read from redis2: key {} value is {}",key,redis2Value);
        }
    }
    
}
