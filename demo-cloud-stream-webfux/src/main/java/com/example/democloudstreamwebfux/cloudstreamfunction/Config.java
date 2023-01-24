package com.example.democloudstreamwebfux.cloudstreamfunction;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.springframework.cloud.function.context.PollableBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Configuration
@Slf4j
public class Config {
    
    @Bean
    public Function<String,String>  uppercase() {
            
            return t ->  t.toUpperCase();

    } 

    // @PollableBean
    // public Supplier<Flux<String>> stringSupplier() {
    //     return () -> Flux.just("One" , "Two");
    // }

    // @Bean
    // public Supplier<Flux<String>> stringSupplierSingleCall() {

    //     return () -> Flux.fromStream(Stream.generate(new Supplier<String>() {

    //         @Override
    //         public String get() {
    //             try {
    //                 Thread.sleep(1000);
    //                 return "Hello from supplier";
                    
    //             } catch (Exception e) {
    //                 throw new RuntimeException();
    //             }
                
    //         }
            
    //     })).subscribeOn(Schedulers.boundedElastic()).share()
    //     ;
    // }

    // @Bean
    // public Function<StringPayload,StringPayload>  uppercase() {
            
    //         return payload -> StringPayload.of(payload.getPayload().toUpperCase() );

    // } 
    // @Bean
    // public Consumer<StringPayload> log() {
    //     return payload -> log.info("Received: {}",payload);
    // }

}
