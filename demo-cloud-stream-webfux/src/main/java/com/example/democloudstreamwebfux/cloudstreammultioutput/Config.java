package com.example.democloudstreamwebfux.cloudstreammultioutput;

import java.time.Duration;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.reactivestreams.Publisher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.webflux.dsl.WebFlux;
import org.springframework.messaging.Message;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.UnicastProcessor;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Configuration
@Slf4j
public class Config {
    
    // @Bean
    // public CommandLineRunner subscribe(Supplier<Flux<Integer>>  intSupplier) {
    //     return args ->  intSupplier.get()
    //                                     .subscribeOn(Schedulers.boundedElastic())
    //                                     .subscribe(t -> log.info("number: {} generated",t)); 

    // }

    // @Bean
    // public Consumer<Flux<String>> evenLog() {
    //     return str -> str   
    //                    .subscribeOn(Schedulers.boundedElastic())
    //                    .subscribe(t -> log.info("Even consume: {}",str) );
                       
    // }

    // @Bean
    // public Consumer<Flux<String>> oddLog() {
    //     return str -> str   
    //                    .subscribeOn(Schedulers.boundedElastic())
    //                    .subscribe(t -> log.info("Odd consume: {}",str) );
                       
    // }

    @Bean
    public Supplier<Flux<Integer>>  intSupplier() {
        final Random rand = new Random() ;
        return () -> Flux
                        .interval(Duration.ofSeconds(2))
                        .map(t -> t.intValue())
                        // .fromStream(() -> Stream.generate(() -> rand.nextInt(Integer.MAX_VALUE)))
                        .take(1)
                        .subscribeOn(Schedulers.boundedElastic())
                        .log()
                        .share()
                        
                        ;
    }

    @Bean
    public Function<Flux<Integer> , Tuple2<Flux<String>,Flux<String>>> scatter() {

    return intFlux -> {
        
        log.info("Apply caled");
        Flux<Integer> connectedFlux =  intFlux.publish().autoConnect(2);
        UnicastProcessor<String> even =  UnicastProcessor.create();
        UnicastProcessor<String> odd = UnicastProcessor.create();

        Flux<Integer> evenFlux = connectedFlux
                                            .filter(number -> number % 2 == 0 )
                                            .doOnNext(number -> even.onNext("Even: " + number) )
                                            ;


        Flux<Integer> oddFlux = connectedFlux
                                            .filter(number -> number % 2 != 0 )
                                            .doOnNext(number -> odd.onNext("Odd: " + number) )
                                            ;

        return Tuples.of(Flux.from(even).doOnSubscribe(x ->  evenFlux.subscribe(t -> log.info("Even scatter: {}",t) )), 
                        Flux.from(odd).doOnSubscribe(x -> oddFlux.subscribe(t -> log.info("odd scatter: {}",t) )));

    };

    }


    @Bean
    public Publisher<Message<byte[]>> httpSupplierFlow() {
        return  IntegrationFlows
                            .from(WebFlux.inboundChannelAdapter("/requests"))
                            .toReactivePublisher();
    }

    @Bean
    public  Supplier<Flux<Message<byte[]>>> httpSupplier(Publisher<Message<byte[]>> httpRequPublisher) {
        return () -> Flux.from(httpRequPublisher).log();
    }

   
    // @Bean
    // public Consumer<Flux<Message<byte[]>>> fluxConsumer() {
    //     return flux ->  flux.subscribeOn(Schedulers.boundedElastic()).subscribe(message ->  log.info("==========> Received: {} ",new String( message.getPayload())));
    // }

    @Bean
    public Function<Flux<Message<byte[]>>,Mono<Void>> fluxConsumer() {
        return flux ->  flux
                            .log()
                            .subscribeOn(Schedulers.boundedElastic())
                            .doOnSubscribe(t -> log.info("=====================> subscrib"))
                            .doOnNext(message -> log.info("======================> fluxConsumer Received {}",new String( message.getPayload())))
                            .doOnEach(t -> log.info("==================================> {} ",t.getType()) )
                            .share()
                            .then();
                            // .subscribe(message ->  log.info("==========> Received: {} ",new String( message.getPayload())))
                            
                            
    }
}
