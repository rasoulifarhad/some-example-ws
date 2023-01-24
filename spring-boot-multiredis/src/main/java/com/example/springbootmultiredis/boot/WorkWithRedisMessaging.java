package com.example.springbootmultiredis.boot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@Order(3)
public class WorkWithRedisMessaging implements CommandLineRunner {


    @Autowired
    @Qualifier("redis1StringRedisTemplate")
    private StringRedisTemplate redis1StringRedisTemplate ;

    @Autowired
    @Qualifier("redis2StringRedisTemplate")
    private StringRedisTemplate redis2StringRedisTemplate ;

    @Autowired
    PatternTopic  allTask1 ;

    @Autowired
    PatternTopic  allTask2 ;

    /**
     * 
     */
    public void run(String... args) throws Exception {

        log.debug("Start Sending messages");
        for (int i = 0; i < 10; i++) {

            String message = "message:" + i ;

            redis1StringRedisTemplate.convertAndSend(allTask1.getTopic(), "redis1:" + message );
            log.debug("Publishing : {} ", "redis1:" + message);

            Thread.sleep(100);

            redis2StringRedisTemplate.convertAndSend(allTask2.getTopic(), "redis2:" + message );
            log.debug("Publishing : {} ", "redis2:" + message);

            Thread.sleep(50);
        }

        log.debug("Sending messages ended");
        
    }
    
}
