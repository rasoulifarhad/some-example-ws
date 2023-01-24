package com.example.democloudstreamwebfux;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Component
@Slf4j
public class EventProducerHandler {

    @Autowired
    private Sinks.Many<Message<String>> many;

    public Mono<ServerResponse> sendMessagMono(ServerRequest request) {
        // many.emitNext(MessageBuilder.withPayload(request.).build(),Sinks.EmitFailureHandler.FAIL_FAST);
        Mono<String> messageMono =  request.bodyToMono(String.class);
        log.info("Going to add message {} to sendMessage." ,messageMono);
        return messageMono
                        .flatMap(message -> {
                            many.emitNext(MessageBuilder.withPayload(message).build(),Sinks.EmitFailureHandler.FAIL_FAST);
                            return 
                                    ServerResponse
                                                .ok()
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .body(message,String.class)
                                                ;
                                    

                        });
                

    }
     
}
