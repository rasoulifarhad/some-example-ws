package com.example.democloudstreamwebfux.cloudFunctionStreamIntegration;

import java.util.function.Consumer;
import java.util.function.Function;

import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Configuration
@Slf4j
public class Config {

    public static void main(String[] args) {
        Function<Flux<String>,Flux<Integer>>  gg;
        Consumer<Flux<IntegerPayload>> hhh ;
        

        
    }

    @Bean
    public Consumer<Flux<StringPayload>> process(StreamBridge streamBridge) {
        return payloads -> payloads
                                .map(StringPayload::getString)
                                .doOnNext(str -> log.info("process===> {}",str))
                                .subscribe(str -> streamBridge.send("process-out-0", str/*
                                                        (Object) str, org.springframework.util.MimeType.valueOf("application/json")*/) )  
                                // .subscribe(str -> streamBridge.send("idestination", str/*
                                //                         (Object) str, org.springframework.util.MimeType.valueOf("application/json")*/) )  
                                ;
    }
    
    @Bean
    public Function<Flux<String>,Flux<Integer>>  doubleIt() {
        return ints -> ints
                            .map(Integer::valueOf)
                            .map(i -> i * 2)
                            .doOnNext(t -> log.info("doubleIt ==> {}",t))
                            ;
    }

    @Bean
    public Function<Flux<Integer> , Flux<IntegerPayload>> produceIt() {
        return integers -> integers 
                                .flatMap(integer -> Flux.range(0, integer) ) 
                                .doOnNext(o -> log.info("produceIt====> {}",o))
                                .map(IntegerPayload::of)
                                .window(100)
                                .flatMap(flux -> flux )
                                ;
    }


    @Bean
    public Consumer<Flux<IntegerPayload>> logIt() {
        return payloads -> payloads
                                .subscribe(payload -> log.info("logIt====> {}",payload))
                                ; 
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


    
}
