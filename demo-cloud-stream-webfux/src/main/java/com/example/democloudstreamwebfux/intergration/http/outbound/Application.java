package com.example.democloudstreamwebfux.intergration.http.outbound;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.http.config.EnableIntegrationGraphController;
import org.springframework.integration.http.dsl.Http;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.example.democloudstreamwebfux.intergration.ChannelConfiguration;
import com.example.democloudstreamwebfux.intergration.ChannelConfiguration.TestStringChannels;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;


public class Application {
    
    @EnableAutoConfiguration
    @Configuration
    @IntegrationComponentScan
    @EnableIntegrationGraphController
    @Slf4j
    public static class IntegrationApplication {
        public static void main(String[] args) throws Exception {

            log.info("--");
            new SpringApplicationBuilder()
                      .sources(IntegrationApplication.class,TestStringChannels.class,ChannelConfiguration.class,
                      IntegrationConfig.class,UppercaserEndpoint.class,HttpInboundGateway.class,HttpOutboundGateway.class
                                          )
                      .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/intergration/http/outbound/")
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
    public static class HttpInboundGateway {

        @Bean(name = "sampleChannel")
        public DirectChannel sampleChannel()  {
            return MessageChannels
                            .direct()
                            .get();
        }

        @Bean
        public IntegrationFlow inGate() {
            log.info("Initialize inbound gateway ....");
            return IntegrationFlows
                                .from(
                                        Http
                                            .inboundGateway("/checkInbound")
                                            .requestMapping(m -> m.methods(HttpMethod.POST))
                                            .mappedRequestHeaders("customHeaders")
                                            .id("idInGate"))
                                .enrichHeaders(h -> h.header("addHeader", "inboundHeader") )
                                .headerFilter("accept-encoding", false)
                                .channel("sampleChannel")
                                .get();
        }
    }

    @Data
    public static class CatFact {
        private String fact;
        private int length ;
    }

    @Configuration
    public static class HttpOutboundGateway {

        @Bean
        public IntegrationFlow outGate() {
            return IntegrationFlows
                                .from("sampleChannel")
                                .handle( Http
                                                    .outboundGateway("https://catfact.ninja/fact")
                                                    // .outboundGateway("https://catfact.ninja/fact/{pathParam}")
                                                    .httpMethod(HttpMethod.GET)
                                                    .expectedResponseType(CatFact.class)
                                                    // .uriVariable("pathParam", "headers[customHeader]")
                                )
                                .logAndReply()
                                // .get()
                                ;
        }
    }

    @Configuration
    @Slf4j
    public static class IntegrationConfig {

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
                        restTemplate.exchange("http://localhost:8080/checkInbound",HttpMethod.POST, request, String.class);

                log.info("response: {}",response.getBody());
                TimeUnit.SECONDS.sleep(10);
            };
        }

    }   
}
