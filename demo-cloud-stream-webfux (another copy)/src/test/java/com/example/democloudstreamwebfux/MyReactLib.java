package com.example.democloudstreamwebfux;

import java.time.Duration;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class MyReactLib {
    
    public Flux<String> alphabet5(char from) {
        return Flux
                    .range((int) from, 5)
                    .map(i -> "" + (char) i.intValue() )
                    ;
    }

    public Flux<String> correctAlphabet5(char from) {
        return Flux
                    .range((int) from, 5)
                    .take(Math.min(5, 'z' - from + 1 ))
                    .map(i -> "" + (char) i.intValue() )
                    ;
    }

    public Mono<String> withDelay(String value , int delaySeconds) {

        return
                Mono
                    .just(value)
                    .delaySubscription(Duration.ofSeconds(delaySeconds))
                    ;
        
    }
}
