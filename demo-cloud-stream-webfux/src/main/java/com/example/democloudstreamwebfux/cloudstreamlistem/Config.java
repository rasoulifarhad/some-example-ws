package com.example.democloudstreamwebfux.cloudstreamlistem;

import java.util.function.Consumer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class Config {

    /**
     * - We are using functional programming model (see Spring Cloud Function support) to define a single message handler as Consumer.
     * - We are relying on framework conventions to bind such handler to the input destination binding exposed by the binder.
     *
     * Doing so also lets you see one of the core features of the framework: It tries to automatically convert incoming message payloads to type Person.
     * You now have a fully functional Spring Cloud Stream application that does listens for messages. 
     * 
     * Go to the RabbitMQ management console or any other RabbitMQ client and send a message to 'log-in-0'
     * 
     * The contents of the message should be a JSON representation of the Person class, as follows:
     * {"name":"Sam Spade"}
     * 
     * From the broker, the message arrives in a form of a byte[]. It is then transformed to a Message<byte[]> by the binders where as you can see 
     * the payload of the message maintains its raw form. 
    *  The headers of the message are <String, Object>, where values are typically another primitive or a collection/array of primitives, hence Object.  
     * @return
     */

    @Bean
    public Consumer<Person> log() {
        return person -> log.info("Received: {}",person);
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
