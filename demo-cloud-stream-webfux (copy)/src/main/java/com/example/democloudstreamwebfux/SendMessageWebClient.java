package com.example.democloudstreamwebfux;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;
@Slf4j
public class SendMessageWebClient {
    
    public static void main(String[] args) {
        WebClient client = WebClient.create("http://localhost:8080");
        // Mono<ClientResponse> result = 
                                        client
                                            .post()
                                            .uri(uriBuilder -> 
                                                            uriBuilder
                                                                    .path("/messages")
                                                                    .queryParam("message", "message1")
                                                                    .build()
                                                            )
                                            .accept(MediaType.TEXT_PLAIN)
                                            .retrieve()
                                            .bodyToMono(String.class)
                                            .log()
                                            .doOnError(throwable -> log.error("Result error out for POST user", throwable))
                                            .doFinally(signalType -> log.info("Result Completed for POST User: {}", signalType));
                                            ;

    }
}
