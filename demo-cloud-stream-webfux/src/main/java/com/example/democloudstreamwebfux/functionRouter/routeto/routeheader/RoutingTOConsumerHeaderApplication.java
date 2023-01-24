package com.example.democloudstreamwebfux.functionRouter.routeto.routeheader;

import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.support.MessageBuilder;

import lombok.extern.slf4j.Slf4j;

@EnableAutoConfiguration
@Slf4j
public class RoutingTOConsumerHeaderApplication {
    


    /**
     * Event Routing, in the context of Spring Cloud Stream, is the ability to either a) route events to a particular event subscriber or 
     * b) route events produced by an event subscriber to a particular destination. 
     * Here we’ll refer to it as route ‘TO’ and route ‘FROM’.
     * 
     * By sending a message to the functionRouter-in-0 destination exposed by the binder (i.e., rabbit, kafka), such message will 
     * be routed to the appropriate (‘even’ or ‘odd’) Consumer.
     * 
     * By default RoutingFunction will look for a spring.cloud.function.definition or spring.cloud.function.routing-expression (for 
     * more dynamic scenarios with SpEL) header and if it is found, its value will be treated as the routing instruction
     * 
     * For example, setting spring.cloud.function.routing-expression header to value T(java.lang.System).currentTimeMillis() % 2 == 0 ? 'even' : 'odd' 
     * will end up semi-randomly routing request to either odd or even functions.
     * 
     * Also, for SpEL, the root object of the evaluation context is Message so you can do evaluation on individual headers (or message) 
     * as well …​.routing-expression=headers['type']
     * 
     * The spring.cloud.function.routing-expression and/or spring.cloud.function.definition can be passed as application properties (e.g., 
     * spring.cloud.function.routing-expression=headers['type'
     */
    public static void main(String[] args) throws Exception {

        new SpringApplicationBuilder()
                .sources(RoutingTOConsumerHeaderApplication.class)
                .bannerMode(Banner.Mode.OFF)
                .properties("spring.config.name=functionRouter")
                .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/functionRouter/routeto/routeheader/")
                // .properties("management.endpoints.web.exposure.include='*'")
                // .properties("spring.cloude.stream.source=toStream")
                .web(WebApplicationType.SERVLET)
                .run("--management.endpoints.web.exposure.include=*"
                              ,"--spring.cloud.stream.function.routing.enabled=true"
                              ,"--spring.cloud.stream.defaultBinder=rabbit"
                              );
    }

    @Bean
    public Consumer<String> even() {
        return value -> log.info("Even: {}",value);
    }

    @Bean
    public Consumer<String> odd() {
        return value -> log.info("Odd: {}",value);
    }

    /**
     * Routing TO Consumer
     * @param streamBridge
     * @return
     */
    @Bean
    ApplicationRunner sendData(@Autowired StreamBridge streamBridge) {
        return args -> {
            streamBridge.send("functionRouter-in-0", MessageBuilder
                                                                    .withPayload("hello")
                                                                    .setHeader("type", "even")
                                                                    .setHeader("spring.cloud.function.routing-expression", 
                                                                        "headers['type']")
                                                                    .build()
                                                                                            );
            streamBridge.send("functionRouter-in-0", MessageBuilder
                                                                    .withPayload("hello2")
                                                                    .setHeader("type", "odd")
                                                                    .setHeader("spring.cloud.function.routing-expression", 
                                                                       "headers['type']")
                                                                    .build()
            );
            streamBridge.send("functionRouter-in-0", MessageBuilder
                                                                    .withPayload("hello3")
                                                                    .setHeader("spring.cloud.function.definition","even")
                                                                    .build()
            );
            streamBridge.send("functionRouter-in-0", MessageBuilder
                                                                    .withPayload("hello4")
                                                                    .setHeader("spring.cloud.function.definition","odd")
                                                                    .build()
            );
        };

    }

}