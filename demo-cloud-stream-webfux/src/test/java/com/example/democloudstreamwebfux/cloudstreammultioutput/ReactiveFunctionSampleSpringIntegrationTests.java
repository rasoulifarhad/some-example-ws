package com.example.democloudstreamwebfux.cloudstreammultioutput;

import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.webflux.dsl.WebFlux;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import reactor.core.publisher.Flux;


public class ReactiveFunctionSampleSpringIntegrationTests {
    
    @Test
    public void testIntegrationFlowAsFunctionForCloudStream() {

        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(
                                                    TestChannelBinderConfiguration.getCompleteConfiguration(
                                                        ReactiveFunctionSampleSpringIntegration.class
                                                    )
                                                ).web(WebApplicationType.REACTIVE)
                                                .run("--spring.cloud.function.definition=reactiveUppercase")) {


            InputDestination inputDestination = context.getBean(InputDestination.class);
            // OutputDestination outputDestination = context.getBean(OutputDestination.class);

            Message<Flux<String> > inputMessage = MessageBuilder.withPayload(Flux.just("Hello")).build();
            inputDestination.send(inputMessage,"reactiveUppercase-in-0");

            // Message<byte[]> outpuMessage =  outputDestination.receive(0, "reactiveUppercase-out-0");
            
        } 
    }


    public interface FluxFunction extends Function<Flux<String>,Flux<String>> {

    }

    @EnableAutoConfiguration
    public static class ReactiveFunctionSampleSpringIntegration {

        @Bean
        public Publisher<Message<byte[]>> httpSupplierFlow() {
            return  IntegrationFlows
                                .from(WebFlux.inboundChannelAdapter("/requests"))
                                .toReactivePublisher();
        }

        @Bean
        public  Supplier<Flux<Message<byte[]>>> httpSupplier(Publisher<Message<byte[]>> httpRequPublisher) {
            return () -> Flux.from(httpRequPublisher);
        }

        // @Bean
        // public IntegrationFlow reactiveUppercaseFlow() {
        //     return IntegrationFlows
        //                     .from(FluxFunction.class ,gateway -> gateway.beanName("reactiveUppercase") )
        //                     .<Flux<String>>handle((payload, headers) -> 
        //                                     payload
        //                                         .map(t -> t.toUpperCase())
        //                                         .subscribeOn(Schedulers.boundedElastic())

        //                                         .subscribe(str -> log.info("=============================> {}",str) )
        //                     )
        //                     .get()
        //                     ;
             
        // }

    }
}
