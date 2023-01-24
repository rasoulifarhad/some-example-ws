package com.example.democloudstreamwebfux.cloudstream.deleted;

import org.springframework.context.annotation.Configuration;


import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.function.Consumer;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;



@Configuration
@Slf4j
public class EventConsumerConfiguration {
    

	// @Bean
	// public EmitterProcessor<StringPayload> emitterProcessor(){
	// 	EmitterProcessor<StringPayload> e =  EmitterProcessor.create();
	// 	return e;
	// }

	// @Bean
	// public Consumer<Flux<StringPayload>>  consume(EmitterProcessor<StringPayload> emitterProcessor) {

	// 	return flux ->  {
	// 		flux
	// 			.log()
	// 			// .map(Message::getPayload)
	// 			.doOnNext(emitterProcessor()::onNext)
	// 			// .doOnNext(stt2::onNext)
	// 			// .doOnNext(msg ->  stt.onNext(msg.getPayload()))
	// 			// .doOnNext(msg -> consumeOne.emitValue(msg.getPayload(),Sinks.EmitFailureHandler.FAIL_FAST) )
	// 			.doOnNext(value -> log.info("consumeFlux: New message received: '{}'",value))
	// 			.subscribe();
	// 			;
	// 	};
	
	// }

// 	@Bean
// 	public Function<Flux<String>,Mono<Void>>  consume2(EmitterProcessor<String> emitterProcessor) {

// 		return flux ->  {
// 			flux
// 				// .log()
// 				// .map(Message::getPayload)
// 				// .doOnNext(emitterProcessor()::onNext)
// 				// .doOnNext(stt2::onNext)
// 				// .doOnNext(msg ->  stt.onNext(msg.getPayload()))
// 				// .doOnNext(msg -> consumeOne.emitValue(msg.getPayload(),Sinks.EmitFailureHandler.FAIL_FAST) )
// 				// .doOnNext(value -> log.info("consumeFlux: New message received: '{}'",value))
// 				.then()
// 				;
// 		};
	

//    }


	// @Bean
	// public Consumer<Message<String>>  consume() {
	// 	return message ->  {
	// 		log.info("consume: New message received: '{}',  enqueued time: {}",
	// 			message.getPayload(),
	// 			message.getHeaders().get(MessageHeaders.TIMESTAMP)
	// 			// message.getHeaders().get(KafkaMessageHeaders.)
	// 		);

	// 	};
	// }

	// @Bean("consumeMany")
    // public Sinks.Many<StringPayload> consumeMany() {
    //     return Sinks.many().multicast().onBackpressureBuffer();
    // }

	// @Bean("consumeOne")
    // public Sinks.One<StringPayload> consumeOne() {
    //     return Sinks.one();
    // }


	// @Bean
	// public Consumer<Flux<Message<String>>>  consumeFlux(@Qualifier("consumeMany") Sinks.Many<String> consumeMany,@Qualifier("consumeOne") Sinks.One<String> consumeOne) {

	// 	return flux ->  {
	// 		flux
	// 			.log()
	// 			.doOnNext(msg -> consumeMany.emitNext(msg.getPayload(),Sinks.EmitFailureHandler.FAIL_FAST) )
	// 			// .doOnNext(msg -> consumeOne.emitValue(msg.getPayload(),Sinks.EmitFailureHandler.FAIL_FAST) )
	// 			.doOnNext(value -> log.info("consumeFlux: New message received: '{}',  enqueued time: {}",value.getPayload(),value.getHeaders().get(MessageHeaders.TIMESTAMP)))
	// 			// .subscribe();
	// 			;
    //     };
	// }

	// @Bean
	// public Consumer<Flux<String>>  consumeFlux(@Qualifier("consumeMany") Sinks.Many<String> consumeMany,@Qualifier("consumeOne") Sinks.One<String> consumeOne) {

	// 	return flux ->  {
	// 		flux
	// 			.log()
	// 			.doOnNext(consumeMany::tryEmitNext )
	// 			// .doOnNext(msg -> consumeOne.emitValue(msg.getPayload(),Sinks.EmitFailureHandler.FAIL_FAST) )
	// 			.doOnNext(value -> log.info("consumeFlux: New message received: '{}'",value))
	// 			.subscribe();
	// 			;
    //     };
	// }


}




