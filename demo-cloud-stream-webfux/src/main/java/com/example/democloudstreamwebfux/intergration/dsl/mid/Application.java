package com.example.democloudstreamwebfux.intergration.dsl.mid;

import org.aopalliance.aop.Advice;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.handler.advice.ExpressionEvaluatingRequestHandlerAdvice;
import org.springframework.integration.http.config.EnableIntegrationGraphController;

import com.example.democloudstreamwebfux.intergration.ChannelConfiguration;
import com.example.democloudstreamwebfux.intergration.ChannelConfiguration.TestStringChannels;

import lombok.extern.slf4j.Slf4j;

public class Application {
    
    @EnableAutoConfiguration
    @Configuration
    @IntegrationComponentScan
    @EnableIntegrationGraphController
    @Slf4j
    public static class IntegrationMisconfigurationExampleApplication {
        public static void main(String[] args) throws Exception {

            log.info("--");
            new SpringApplicationBuilder()
                      .sources(IntegrationMisconfigurationExampleApplication.class,TestStringChannels.class,ChannelConfiguration.class,
                      IntegrationMisconfigurationExampleConfig.class
                                          )
                      .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/intergration/dsl/mid/")
                      .run("--management.endpoints.web.exposure.include=*","--spring.main.lazy-initialization=false" 
                                  ,"--logging.level.root=INFO" 
                                  ,"--logging.level.org.springframework.retry.support.RetryTemplate=DEBUG"
                                  ,"--spring.cloud.stream.defaultBinder=rabbit");
        }
    }

    @Configuration
    @Slf4j
    public static class IntegrationMisconfigurationExampleConfig {

        @Bean
        public MessagingTemplate messagingTemplate() {
            return new MessagingTemplate();
        }

        @Bean(name = "input")
        public DirectChannel input() {
            return MessageChannels
                            .direct()
                            .get();
        }

        @Bean
        public IntegrationFlow loggingFlow() {
            return IntegrationFlows
                                .from("input")
                                .<String,String>transform(String::toUpperCase )
                                // .channel("nullChannel")
                                .get();
        }


        @Bean
        @Order(30)
        public ApplicationRunner runner() {
            return args ->  {
                final MessagingTemplate template = messagingTemplate() ;

                template.convertAndSend("input", "abcdef");
            };
        }
    }


    // /**
    //  * The way to catch runtime exception like that and do some analyzing work is with the ExpressionEvaluatingRequestHandlerAdvice, which you 
    //  * can add to your transform(String::toUpperCase) configuration in the second argument for endpoint configuration:
    //  * 
    //  *     .<String, String>transform(String::toUpperCase, e-> e.advice(myExpressionEvaluatingRequestHandlerAdvice()
    //  * 
    //  */
    // @EnableAutoConfiguration
    // @Configuration
    // @IntegrationComponentScan
    // @EnableIntegrationGraphController
    // @Slf4j
    // public static class IntegrationExampleApplication {
    //     public static void main(String[] args) throws Exception {

    //         log.info("--");
    //         new SpringApplicationBuilder()
    //                   .sources(IntegrationExampleApplication.class,TestStringChannels.class,ChannelConfiguration.class,
    //                   IntegrationExampleConfig.class
    //                                       )
    //                   .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/intergration/dsl/mid/")
    //                   .run("--management.endpoints.web.exposure.include=*","--spring.main.lazy-initialization=false" 
    //                               ,"--logging.level.root=INFO" 
    //                               ,"--logging.level.org.springframework.retry.support.RetryTemplate=DEBUG"
    //                               ,"--spring.cloud.stream.defaultBinder=rabbit");
    //     }
    // }

    // @Configuration
    // @Slf4j
    // public static class IntegrationExampleConfig {

    //     @Bean
    //     public Advice expressionAdvice() {
    //         ExpressionEvaluatingRequestHandlerAdvice advice = new ExpressionEvaluatingRequestHandlerAdvice();
    //         advice.setSuccessChannelName("success.input");
    //         advice.setOnSuccessExpressionString("payload + ' was successful'");
    //         advice.setFailureChannelName("failure.input");
    //         advice.setOnFailureExpressionString("payload + ' was bad, with reason: ' + #exception.cause.message");
    //         advice.setTrapException(true);
    //         return advice ;
    //     }

    //     @Bean
    //     public MessagingTemplate messagingTemplate() {
    //         return new MessagingTemplate();
    //     }

    //     @Bean(name = "input")
    //     public DirectChannel input() {
    //         return MessageChannels
    //                         .direct()
    //                         .get();
    //     }

    //     @Bean
    //     public IntegrationFlow loggingFlow() {
    //         return IntegrationFlows
    //                             .from("input")
    //                             .<String,String>transform(String::toUpperCase,e -> e.advice(expressionAdvice()) )
    //                             .channel("nullChannel")
                                
    //                             .get();
    //     }

    //     @Bean
    //     public IntegrationFlow success() {
    //         return flow -> flow
    //                             .handle(message -> log.info("{}",message));
    //     }

    //     @Bean 
    //     public IntegrationFlow failure() {

    //         return flow -> flow
    //                             .handle(message -> log.info("{}",message));
    //     }        
        
    //     @Bean
    //     @Order(30)
    //     public ApplicationRunner runner() {
    //         return args ->  {
    //             final MessagingTemplate template = messagingTemplate() ;

    //             template.convertAndSend("input", "abcdef");
    //         };
    //     }
    // }

}
