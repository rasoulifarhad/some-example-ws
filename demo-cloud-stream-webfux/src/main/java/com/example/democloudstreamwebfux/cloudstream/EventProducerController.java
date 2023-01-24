package com.example.democloudstreamwebfux.cloudstream;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.webflux.dsl.WebFlux;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import kafka.server.BlockingSend;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.Many;

@RestController
@Slf4j
@Configuration
public class EventProducerController {

    @Autowired
    private SendMessageWebClient sendMessageWebClient ;

    private final Many<Message<StringPayload>> senderReact ;
    // private Many<Message<StringPayload>> processor = Sinks.many().multicast().directBestEffort();

    private final Many<StringPayload> rerceiverReact;


    public  EventProducerController() {
        senderReact = Sinks.many().multicast().onBackpressureBuffer(1,false);
        rerceiverReact = Sinks.many().multicast().onBackpressureBuffer(1,false);
        // EmitterProcessor pp =  EmitterProcessor.create(1,false);
        // FluxSink ss = pp.sink(FluxSink.OverflowStrategy.BUFFER);
    }

    @Bean 
    public Supplier<Flux<Message<StringPayload>>> supply() {
        // return () -> processor.asFlux();
        return () -> senderReact.asFlux()
                    .doOnNext(m -> log.info("Manually sending message {}",m))
                    .doOnError(t -> log.error("Error encountered {} ",t))
                
                    ;
    }

   
    @Bean
	public Consumer<Flux<Message<StringPayload>>>  consume() {

		return flux ->  {
			flux
				.log()
				// .map(Message::getPayload)
				.doOnNext(sb -> rerceiverReact.tryEmitNext(sb.getPayload()))
				// .doOnNext(stt2::onNext)
				// .doOnNext(msg ->  stt.onNext(msg.getPayload()))
				// .doOnNext(msg -> consumeOne.emitValue(msg.getPayload(),Sinks.EmitFailureHandler.FAIL_FAST) )
				.doOnNext(value -> log.info("consumeFlux: New message received: '{}'",value))
				.subscribe();
				;
		};
	
	}

    @PostMapping("/messagesNoReactive")
    public StringPayload sendMessageNoReactive(@RequestBody StringPayload message) {
        log.info("Going to add message {} to sendMessage." ,message);
        // getProcessor().tryEmitNext(MessageBuilder.withPayload(new StringPayload(message)).build());
        senderReact.emitNext(MessageBuilder.withPayload(message).build(),
                                        (signalType, emitResult) -> emitResult.equals(Sinks.EmitResult.FAIL_NON_SERIALIZED) ? true : false );

        StringPayload sp =  rerceiverReact.asFlux().log().next().block();
        log.info("sendMessageNoReactive: {}", sp);
        return sp;
                                
    }


    @PostMapping("/messages" )
    public Mono<StringPayload>  sendMessage(@RequestBody StringPayload message) {
        
        log.info("Going to add message {} to app-topic." ,message);
        // getProcessor().tryEmitNext(MessageBuilder.withPayload(new StringPayload(message)).build());
        senderReact.emitNext(MessageBuilder.withPayload(message).build(),
                                        (signalType, emitResult) -> emitResult.equals(Sinks.EmitResult.FAIL_NON_SERIALIZED) ? true : false );
        log.info("Sending Result Mono ");

        return  rerceiverReact.asFlux().checkpoint("start receive").log().next().checkpoint("end receive");


    }

    @PostMapping("/do" )
    public void  doReq() throws Exception {
        
        sendMessageWebClient.sendMessage("doReq");
        // return consumeMany.asFlux();
    }

    // @PostMapping("messages")
    // public Mono<String>  sendMessage(@RequestParam (name = "message") String message) {
        
    //     log.info("Going to add message {} to app-topic." ,message);
    //     supplyMany.emitNext(MessageBuilder.withPayload(message).build(),Sinks.EmitFailureHandler.FAIL_FAST);
    //     log.info("Sending Result Mono ");

    //     return consumeOne.asMono();
                                        
    // }


    // @Bean
    // public Publisher<Message<String>> httpReactiveSource() {
    //     return IntegrationFlows.
    //             from(Http.inboundChannelAdapter("/message/{id}")
    //                     .requestMapping(r -> r
    //                             .methods(HttpMethod.POST)
    //                     )
    //                     .payloadExpression("#pathVariables.id")
    //             )
    //             .channel(MessageChannels.queue())
    //             .toReactivePublisher();
    // }

    // @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    // public Flux<String> eventMessages() {
    //     return Flux.from(httpReactiveSource())
    //             .map(Message::getPayload);
    // }


    /**
    * 
    * #run the app 
    * #
    * #- to listen to SSEs.
    * #   
    * #  curl http://localhost:8080/events
    * #
    * #- in the second terminal:
    * #
    * #  curl -X POST http://localhost:8080/message/foo
    * #  curl -X POST http://localhost:8080/message/bar
    * #  curl -X POST http://localhost:8080/message/666
    *
    */
    @Bean
    public Publisher<Message<String>> reactiveSource() {
        return IntegrationFlows.
                            from(WebFlux.inboundChannelAdapter("/message/{id}")
                                    .requestMapping(r -> r
                                            .methods(HttpMethod.POST)
                                    )
                                    .payloadExpression("#pathVariables.id")
                            )
                            .log()
                            .channel(MessageChannels.flux())
                            .toReactivePublisher();
    }

    @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> eventMessages() {
    
        return Flux
                    .from(reactiveSource())
                    .map(Message::getPayload);
    }

//     @Bean
//     public IntegrationFlow eventMessages() {
//       return IntegrationFlows
//               .from(WebFlux.inboundGateway("/sse")
//                       .requestMapping(m -> m.produces(MediaType.TEXT_EVENT_STREAM_VALUE)))
//               .handle((p, h) -> reactiveSource())                
//               .get();
//   }   
    
}
