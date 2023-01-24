package com.example.springbootmultiredis.messaging.redis;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.scheduling.annotation.Scheduled;

public class Sender {

    private final RedisTemplate<String,String> redis1RedisTemplate ;
    private final RedisTemplate<String,String> redis2RedisTemplate ;
    private final ChannelTopic topic1 ;
    private final ChannelTopic topic2 ;


    private final AtomicLong counter = new AtomicLong(0);

    public Sender(RedisTemplate<String, String> redis1RedisTemplate,RedisTemplate<String, String> redis2RedisTemplate, ChannelTopic topic1, ChannelTopic topic2) {
        this.redis1RedisTemplate = redis1RedisTemplate;
        this.redis2RedisTemplate = redis1RedisTemplate;
        this.topic1 = topic1;
        this.topic2 = topic2;
    }

    
    @Scheduled(fixedDelay = 100)
    public void publish() {

        redis1RedisTemplate.convertAndSend(topic1.getTopic(), "Message " + counter.incrementAndGet() + ",  "  + Thread.currentThread().getName() );
        redis2RedisTemplate.convertAndSend(topic2.getTopic(), "Message " + counter.incrementAndGet() + ",  "  + Thread.currentThread().getName() );


    }
}
