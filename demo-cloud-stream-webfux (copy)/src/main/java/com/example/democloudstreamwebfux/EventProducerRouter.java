package com.example.democloudstreamwebfux;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;


import static org.springframework.web.reactive.function.server.RouterFunctions.* ;
import static org.springframework.web.reactive.function.server.RequestPredicates.* ;
@Configuration
public class EventProducerRouter {
    
    @Bean
    RouterFunction<ServerResponse> routes(EventProducerHandler handler) {
        return  route(
                    POST("/messagesRouter")
                        .and(accept(MediaType.APPLICATION_JSON)),handler::sendMessagMono);
        

    } 

}
