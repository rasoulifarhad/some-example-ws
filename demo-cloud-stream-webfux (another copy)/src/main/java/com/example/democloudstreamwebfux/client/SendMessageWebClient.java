package com.example.democloudstreamwebfux.client;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.democloudstreamwebfux.model.StringPayload;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
@Component
public class SendMessageWebClient  {
    
    private final  WebClient webClient ;

    
    

    public SendMessageWebClient(WebClient.Builder builder) {
        this.webClient = builder
                                .baseUrl("http://localhost:8080")
                                .build();
    }

    public Flux<StringPayload> sendMessage(String message) throws Exception {
        log.info("---------------------------------------------------------------------------------------------------------------------");
        return 
                webClient
                        .post()
                        .uri("/messages")
                        .accept(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(StringPayload.builder().value(message).build()))
                        .retrieve()
                        .bodyToFlux(StringPayload.class)
                        // .map(StringPayload::getValue)
                        ;

                                        // Flux<String> flux1= 
                                        // webClient
                                        //     .post()
                                        //     .uri("/messages")
                                        //     .bodyValue("message1")
                                        //     .accept(MediaType.TEXT_EVENT_STREAM)
                                        //     .retrieve()
                                        //     .bodyToFlux(String.class)
                                        //     // .flatMap(t -> Mono.just(t))
                                        //     // .log()
                                        //     // .subscribe(t -> log.info("Ok I receive {} " , t))
                                        //     // .doOnError(throwable -> log.error("Result error out for POST user", throwable))
                                        //     // .doFinally(signalType -> log.info("Result Completed for POST User: {}", signalType));
                                            
                                        //     ;

            //                             Flux<String> flux2= 
            //                             webClient
            //                                 .post()
            //                                 .uri("/messages")
            //                                 .bodyValue("message2")
            //                                 .accept(MediaType.TEXT_EVENT_STREAM)
            //                                 .retrieve()
                                            
            //                                 .bodyToFlux(String.class)
            //                                 // .flatMap(t -> Mono.just(t))
            //                                 // .log()
            //                                 // .subscribe(t -> log.info("Ok I receive {} " , t))
            //                                 // .doOnError(throwable -> log.error("Result error out for POST user", throwable))
            //                                 // .doFinally(signalType -> log.info("Result Completed for POST User: {}", signalType));
                                            
            //                                 ;

       
            //                             // System.out.println(    flux1.collectList().block());
            //                             // System.out.println(    flux2.collectList().block());
            // flux1
            // .subscribe(t -> log.info("Ok I receive {} " , t))
            // ;
            // flux2
            // .subscribe(t -> log.info("Ok I receive {} " , t))
            // ;

    }
}
