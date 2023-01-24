package com.example.democloudstreamwebfux.cloudstreambridge;

import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class config {
    
    @Bean
    public Function<String,String>  uppercase() {
            
            return t -> {
                log.info("Received: {}",t);
                return t.toUpperCase();
            };

    } 

    @RestController
    @Slf4j
    public static class WebResource {

        @Autowired
        private StreamBridge streamBridge ; 

        @PostMapping("/delegateToSupplier")
        public void delegateToSupplier(@RequestBody String body ) {
            
            log.info("Sending: {}",body);
            streamBridge.send("toStream-out-0", body);

        }

        /**
         * Dynamic routing
         * no need to spring.cloud.stream.source
         * @param body
         */
        @PostMapping("/delegateToDestination")
        public void delegateToDestination(@RequestBody String body ) {
            
            log.info("Sending: {}",body);
            streamBridge.send("inp-topic", body);

        }
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
