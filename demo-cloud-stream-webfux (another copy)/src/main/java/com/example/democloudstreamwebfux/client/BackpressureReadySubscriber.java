package com.example.democloudstreamwebfux.client;

import org.reactivestreams.Subscription;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.BaseSubscriber;

@Slf4j
public class BackpressureReadySubscriber<T> extends BaseSubscriber<T> {

    public void hookOnSubscribe(Subscription subscription) {

        // request the first item on subscribe
        request(1);

    }

    public void hookOnNext(T value) {

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        log.info("========>>>>>>>>>>> {}",value);
        request(1);

    }
    
}
