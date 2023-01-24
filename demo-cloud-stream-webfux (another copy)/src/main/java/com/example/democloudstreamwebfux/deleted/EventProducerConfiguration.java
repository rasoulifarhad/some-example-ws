package com.example.democloudstreamwebfux.deleted;

import java.util.function.Supplier;

import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.webflux.dsl.WebFlux;
import org.springframework.messaging.Message;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Configuration
@Slf4j
public class EventProducerConfiguration {
    
    // @Bean("supplyMany")
    // public Sinks.Many<Message<StringPayload>> supplyMany() {
    //     return Sinks.many().unicast().onBackpressureBuffer();
    // }

    
    // @Bean
    // public Supplier<Flux<Message<StringPayload>>> supply(@Qualifier("supplyMany") Sinks.Many<Message<StringPayload>> supplyMany)  {

    //     return () -> supplyMany
    //                     .asFlux()
    //                     .doOnNext(m -> log.info("Manually sending message {}",m))
    //                     .doOnError(t -> log.error("Error encountered {} ",t))
    //                     ;

    // }

    // @Bean
	// public Publisher<Message<String>> httpSupplierFlow() {
	// 	return IntegrationFlows.from(
	// 								WebFlux.inboundChannelAdapter("/messages2")
	// 								)
	// 								.toReactivePublisher()
	// 								;
	// }

	// @Bean
	// public Supplier<Flux<Message<String>>> httpMessageSuplier(Publisher<Message<String>> httpRequestPublisher) {

	// 	return () -> Flux.from(httpRequestPublisher) ;

	// }

}
