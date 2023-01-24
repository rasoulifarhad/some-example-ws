package com.example.democloudstreamwebfux.channelresolver.sendto;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmitResult;
import reactor.core.scheduler.Schedulers;

@SpringBootApplication
// @EnableAutoConfiguration
@Slf4j
public class SendtoApplication {

    @Bean
    public Sinks.Many<Message<String>> sinksMany() {
        return Sinks.many().multicast().directBestEffort();
    }

     @Bean
    public Supplier<Flux<Message<String>>> supplier(Sinks.Many<Message<String>> sinksMany) {
        return () -> sinksMany.asFlux().doOnNext(m -> log.info("supplied: {}",m));
    }

    @Bean
    public Function<Flux<Message<String>>,Flux<Message<String>>> func(Sinks.Many<Message<String>> sinksMany) {
        return flux -> 
                    flux.log()
                    .map(msg -> MessageBuilder
                                            .withPayload(msg.getPayload())
                                            .copyHeaders(msg.getHeaders())
                                            .setHeader("spring.cloud.stream.sendto.destination","myTopic")
                                            //    .setHeader(BinderHeaders.TARGET_DESTINATION,"myTopic")
                                            //    .setHeader("spring.cloud.stream.sendto.binder", "rabbit")
                                            .build() 
                    )
                    .doOnNext(m -> log.info("funced: {}",m))
                    .log()
                    ;
    }
    
    /**
     * run
     * 
     * echo 'customerId-1' | curl --location --request  POST http://localhost:8080/withSendto -H "Content-Type: application/json" -d @-
     *
     * echo 'customerId-2' | curl --location --request  POST http://localhost:8080/withSendto -H "Content-Type: application/json" -d @-
     *
     */
    
    @RestController
    @Slf4j
    public static class Resource{

        private final Sinks.Many<Message<String>> sinksMany;

        @Autowired
        public Resource(Sinks.Many<Message<String>> sinksMany) {

           this.sinksMany= sinksMany;

        }

        @PostMapping(value = "/withSendto")
        @ResponseStatus(code = HttpStatus.OK)
        public void withSendto(@RequestBody String payloadData) {

            Message<String>  message = MessageBuilder
                                                 .withPayload(payloadData)
                                                 .build();
            
            EmitResult emitResult =  sinksMany.tryEmitNext(message);
            log.info("{} emitted with {}",payloadData,emitResult);
        }
    }

    @Bean 
    public Consumer<Flux<Message<String>>> receive1() {
        return flux -> flux
                        .log()
                        .subscribeOn(Schedulers.boundedElastic())
                        .subscribe(msg -> log.info("'{}' Data received from consumer1 ......",msg.getPayload()))
                        ;
    }

    @Bean 
    public Consumer<Flux<Message<String>>> receive2() {
        return flux -> flux
                        .log()
                        .subscribeOn(Schedulers.boundedElastic())
                        .subscribe(msg -> log.info("'{}' Data received from consumer2 ......",msg.getPayload()))
                        ;
    }
    
    // @Bean
    // public Consumer<Flux<String>> consumer( Sinks.Many<Message<String>> sinksMany) {
    //     return fluxMsg ->  fluxMsg
    //                             .map(msg ->
    //                                         MessageBuilder
    //                                                     .withPayload(msg) 
    //                                                     .setHeader("spring.cloud.stream.sendto.destination","topic-out")
    //                                                     .setHeader("spring.cloud.stream.sendto.binder", "rabbit")
    //                                                     .build())
    //                             .doOnNext(sinksMany::tryEmitNext)
    //                             .subscribe(t -> log.info("Conjsume subscribed") )
    //                             ;
    // }


    public static void main(String[] args) throws Exception {

        new SpringApplicationBuilder()
                .sources(SendtoApplication.class,Resource.class)
                .bannerMode(Banner.Mode.OFF)
                .properties("spring.config.name=sendtoApplication")
                .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/channelresolver/sendto/")
                // .properties("management.endpoints.web.exposure.include='*'")
                // .properties("spring.cloude.stream.source=toStream")
                .web(WebApplicationType.REACTIVE)
                .run("--management.endpoints.web.exposure.include=*"
                            ,"--spring.main.lazy-initialization=false"
                            //   ,"--spring.cloud.stream.function.routing.enabled=true"
                              );
    }
    
}
