package com.example.democloudstreamwebfux;

import java.util.function.Supplier;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Configuration
@Slf4j
public class EventProducerConfiguration {
    
    @Bean
    public Sinks.Many<Message<String>> many() {
        return Sinks.many().unicast().onBackpressureBuffer();
    }

    
    @Bean
    public Supplier<Flux<Message<String>>> supply(Sinks.Many<Message<String>> many)  {

        return () -> many
                        .asFlux()
                        .doOnNext(m -> log.info("Manually sending message {}",m))
                        .doOnError(t -> log.error("Error encountered {} ",t))
                        ;

    }
}
