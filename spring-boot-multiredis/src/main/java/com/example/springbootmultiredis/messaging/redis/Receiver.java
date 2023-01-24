package com.example.springbootmultiredis.messaging.redis;

import java.util.concurrent.atomic.AtomicInteger;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Receiver {
    
    private AtomicInteger counter = new AtomicInteger();

    public void receiveMessageFromRedis1(String message) {
        log.info("Received <{}> from redis {} ", message,"redis1");
        counter.incrementAndGet();

    }

    public void receiveMessageFromRedis2(String message) {
        log.info("Received <{}> from redis {} ", message,"redis2");
        counter.incrementAndGet();

    }

    public void receiveMessageFromAllRedises(String message) {
        log.info("Received <{}> {} ", message);
        counter.incrementAndGet();

    }

    public void receiveMessageFromTopic1And2(String message) {
        log.info("Received <{}> {} ", message);
        counter.incrementAndGet();

    }

    public int getCount() {
        return counter.get();
    }
}
