package com.example.demotestspringautoconfig;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;


@SpringBootTest
public class RedisTest {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Test
    public void testSetGet() {
        String key = "key1"  ;
        String value = "value1";
        String expected = value;
        stringRedisTemplate.opsForValue().set(key, value);

        String actual = stringRedisTemplate.opsForValue().get(key);

        assertEquals(expected, actual);
    }

    
}
