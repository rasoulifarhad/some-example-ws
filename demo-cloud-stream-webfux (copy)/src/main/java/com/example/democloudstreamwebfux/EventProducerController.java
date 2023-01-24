package com.example.democloudstreamwebfux;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@RestController
@Slf4j
public class EventProducerController {

    @Autowired
    private Sinks.Many<Message<String>> many;

    @PostMapping("messagesNoReactive")
    public ResponseEntity<String> sendMessageNoReactive(@RequestParam String message) {
        log.info("Going to add message {} to sendMessage." ,message);
        many.emitNext(MessageBuilder.withPayload(message).build(),Sinks.EmitFailureHandler.FAIL_FAST);
        return ResponseEntity.ok(message);

    }


    @PostMapping("messages")
    public Mono<ResponseEntity<String>>  sendMessage(@RequestParam (name = "message") String message) {
        
        log.info("Going to add message {} to app-topic." ,message);
        many.emitNext(MessageBuilder.withPayload(message).build(),Sinks.EmitFailureHandler.FAIL_FAST);
        return Mono.just(  ResponseEntity
                                        .ok()
                                        .contentType(MediaType.TEXT_PLAIN)
                                        .body(message)
                    )
                                        ;
    }
    
}
