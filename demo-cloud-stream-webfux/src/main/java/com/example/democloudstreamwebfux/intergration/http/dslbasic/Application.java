package com.example.democloudstreamwebfux.intergration.http.dslbasic;

import java.util.Arrays;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.http.config.EnableIntegrationGraphController;
import org.springframework.integration.http.dsl.Http;
import org.springframework.integration.http.support.DefaultHttpHeaderMapper;
import org.springframework.integration.mapping.HeaderMapper;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.example.democloudstreamwebfux.intergration.ChannelConfiguration;
import com.example.democloudstreamwebfux.intergration.ChannelConfiguration.TestStringChannels;

import lombok.extern.slf4j.Slf4j;


public class Application {
    
    @EnableAutoConfiguration
    @Configuration
    @IntegrationComponentScan
    @EnableIntegrationGraphController
    @Slf4j
    public static class HttpBasicIntegrationApplication {
        public static void main(String[] args) throws Exception {

            log.info("--");
            new SpringApplicationBuilder()
                      .sources(HttpBasicIntegrationApplication.class,TestStringChannels.class,ChannelConfiguration.class,
                      HttpBasicIntegrationConfig.class,UppercaserEndpoint.class
                                          )
                      .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/intergration/http/dslbasic/")
                      .run("--management.endpoints.web.exposure.include=*","--spring.main.lazy-initialization=false" 
                                  ,"--logging.level.root=INFO" 
                                  ,"--logging.level.org.springframework.retry.support.RetryTemplate=DEBUG"
                                  ,"--spring.cloud.stream.defaultBinder=rabbit");
        }
    }

    @Component(value = "uppercaserEndpoint")
    public static class UppercaserEndpoint {

        public Message<?> uppercase(Message<String> msg) {
            return MessageBuilder
                            .withPayload(msg.getPayload().toUpperCase())
                            .setHeader("http_statusCode", HttpStatus.OK)
                            .build() ;
        }

    }

    @Configuration
    @Slf4j
    public static class HttpBasicIntegrationConfig {

        @Bean
        public ExpressionParser parser() {
            return new SpelExpressionParser();
        }

        @Bean
        public HeaderMapper<HttpHeaders> headerMapper() {
            return new DefaultHttpHeaderMapper();
        }

        @Bean
        public IntegrationFlow dslInbound() {

            return IntegrationFlows
                                .from(Http
                                        .inboundGateway("/dslFoo")
                                        .requestMapping(m -> m.methods(HttpMethod.POST) )
                                        .requestPayloadType(String.class)
                                        .id("dslInbound")
                                )
                                .channel("dslHttpRequestChannel")
                                .handle("uppercaserEndpoint", "uppercase")
                                .get()
                                ;
        }

        @Bean
        @Order(30)
        public ApplicationRunner runner( ) {

            return args -> {
                RestTemplate restTemplate = new RestTemplate();

                HttpHeaders headers = new HttpHeaders();
                headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
                headers.setContentType(MediaType.APPLICATION_JSON); 
                
                HttpEntity<String> request = new HttpEntity<>("Hiiiiiiiiiiiiiiiiiiii",headers);
                

                ResponseEntity<String> response = 
                        restTemplate.exchange("http://localhost:8080/dslFoo",HttpMethod.POST, request, String.class);

                log.info("response: {}",response.getBody());

            };

        }

        
        // @Bean
        // @ServiceActivator(inputChannel = "httpOutRequestChannel")
        // public HttpRequestExecutingMessageHandler outbound() {
            
        //     HttpRequestExecutingMessageHandler handler = new HttpRequestExecutingMessageHandler("http://localhost:8080/foo");
        //     handler.setHttpMethod(HttpMethod.POST);        
        //     handler.setExpectedResponseType(String.class);
        //     return handler ;

        // }

        // @Bean
        // public IntegrationFlow dslOutbound(DirectChannel dslHttpOutRequestChannel) {

        //     return IntegrationFlows
        //                         .from(dslHttpOutRequestChannel)
        //                         .handle(Http
        //                                     .outboundGateway("http://localhost:8080/dslFoo")
        //                                     .httpMethod(HttpMethod.POST)
        //                                     .expectedResponseType(String.class))
        //                         .get();
        // }

        // @Bean
        // @Order(30)
        // public ApplicationRunner runner(QueueChannel dslHttpRequestChannel ,
        //                                 QueueChannel httpRequestChannel) {

        //     return args -> {

        //         // dslHttpRequestChannel.subscribe(message -> log.info("received: {}",message));
        //         // httpRequestChannel.subscribe(message -> log.info("received: {}",message));
        //     };

        // }

        // @Bean
        // @Order(30)
        // public ApplicationRunner runnerOutbound(DirectChannel dslHttpOutRequestChannel ,
        //                                 DirectChannel httpOutRequestChannel) {

        //     return args -> {



        //     };

        // }

    }

}
