package com.example.democloudstreamwebfux.route;

import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.democloudstreamwebfux.client.SendMessageWebClient;
import com.example.democloudstreamwebfux.model.StringPayload;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.EmitterProcessor;
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

    private final Many<Message<StringPayload>> processor ;
    // private Many<Message<StringPayload>> processor = Sinks.many().multicast().directBestEffort();

    private final Many<StringPayload> consumeProcessor;


    public  EventProducerController() {
        processor = Sinks.many().multicast().onBackpressureBuffer(1,false);
        consumeProcessor = Sinks.many().multicast().onBackpressureBuffer(1,false);
        // EmitterProcessor pp =  EmitterProcessor.create(1,false);
        // FluxSink ss = pp.sink(FluxSink.OverflowStrategy.BUFFER);
    }

    @Bean 
    public Supplier<Flux<Message<StringPayload>>> supply() {
        // return () -> processor.asFlux();
        return () -> processor.asFlux()
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
				.doOnNext(sb -> consumeProcessor.tryEmitNext(sb.getPayload()))
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
        processor.emitNext(MessageBuilder.withPayload(message).build(),
                                        (signalType, emitResult) -> emitResult.equals(Sinks.EmitResult.FAIL_NON_SERIALIZED) ? true : false );

        StringPayload sp =  consumeProcessor.asFlux().log().next().block();
        log.info("sendMessageNoReactive: {}", sp);
        return sp;
                                
    }


    @PostMapping("/messages" )
    public Mono<StringPayload>  sendMessage(@RequestBody StringPayload message) {
        
        log.info("Going to add message {} to app-topic." ,message);
        // getProcessor().tryEmitNext(MessageBuilder.withPayload(new StringPayload(message)).build());
        processor.emitNext(MessageBuilder.withPayload(message).build(),
                                        (signalType, emitResult) -> emitResult.equals(Sinks.EmitResult.FAIL_NON_SERIALIZED) ? true : false );
        log.info("Sending Result Mono ");

        return  consumeProcessor.asFlux().log().next();


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

   
    
}
