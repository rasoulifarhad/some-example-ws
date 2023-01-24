package com.example.democloudstreamwebfux.channelresolver.binderaware;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.stream.binding.BinderAwareChannelResolver;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmitFailureHandler;
import reactor.util.concurrent.Queues;

@SpringBootApplication
// @EnableAutoConfiguration
@Slf4j
public class Application {
    
    /**
     * 
     * The BinderAwareChannelResolver is a special bean registered automatically by the framework. You can autowire this bean into your 
     *application and use it to resolve output destination at runtime
     *
     * The 'spring.cloud.stream.dynamicDestinations' property can be used for restricting the dynamic destination names to a known set 
     * (that is, intentionally allowed values). If this property is not set, any destination can be bound dynamically.
     * 
     * The following example demonstrates one of the common scenarios where REST controller uses a path variable to 
     * determine target destination:
     * 
     * 
     * You can also delegate to the framework to dynamically resolve the output destination by specifying spring.cloud.stream.sendto.destination 
     * header set to the name of the destination to be resolved.
     * 
     */

    public static void main(String[] args) throws Exception {

        new SpringApplicationBuilder()
                .sources(Application.class,Resource.class/*,TestSink.class*/)
                .bannerMode(Banner.Mode.OFF)
                .properties("spring.config.name=channelresolver")
                .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/channelresolver/binderaware/")
                // .properties("management.endpoints.web.exposure.include='*'")
                // .properties("spring.cloude.stream.source=toStream")
                .web(WebApplicationType.REACTIVE)
                .run("--management.endpoints.web.exposure.include=*"
                            //   ,"--spring.cloud.stream.function.routing.enabled=true"
                            //   ,"--spring.cloude.stream.source=toStream"

                              );


    }

    @Data
    @AllArgsConstructor(staticName = "of")
    @NoArgsConstructor
    public static class PayloadData {
        private String data ;
    }
      
    /**
     * run
     * 
     * echo '{"id":"customerId-1","billPay":"150"}' | curl -isS --location --request  POST http://localhost:8080/customers -H "Content-Type: application/json" -d @- ;echo
     *
     * echo '{"id":"order-1","billPay":"150"}' | curl -isS --location --request  POST http://localhost:8080/orders -H "Content-Type: application/json" -d @- ;echo
     *  
     * echo '{"id":"customerId-1","billPay":"150"}' | curl -isS --location --request  POST http://localhost:8080/dest/def/orders -H "Content-Type: application/json" -d @- ;echo
     * 
     * echo '{"id":"order-1","billPay":"150"}' | curl -isS --location --request  POST http://localhost:8080/dest/def/customers -H "Content-Type: application/json" -d @- ;echo
     * 
     * echo '{"id":"customerId-1","bill-pay":"100"}' | curl --location --request  POST http://localhost:8080/withSendto -H "Content-Type: application/json" -d @-
     *
     * echo '{"id":"customerId-2","bill-pay":"150"}' | curl --location --request  POST http://localhost:8080/withSendto -H "Content-Type: application/json" -d @-
     *
     */

    // @Bean
    // public NewDestinationBindingCallback<RabbitProducerProperties> dynamicConfigurer() {
    //     return (name, channel, props, extended) -> {
            
    //         props.setRequiredGroups("bindThisQueue");
    //         extended.setQueueNameGroupOnly(true);
    //         extended.setAutoBindDlq(true);
    //         extended.setDeadLetterQueueName("myDLQ");

    //         log.info("============================> {}",extended.getStreamMessageConverterBeanName());
    //     };
    // }

     @RestController
     @Slf4j
    public static class Resource{

        private static final String TYPE_DEFAULT = "dest";
        private static final String CAT_DEFAULT = "def";

        private final BinderAwareChannelResolver resolver ;
        // private final EmitterProcessor<Message<PayloadData>> emitterProcessor;
        private final Sinks.Many<Message<PayloadData>> sinksMany;

        private final StreamBridge streamBridge ; 
        

        @Autowired
        public Resource(BinderAwareChannelResolver resolver,StreamBridge streamBridge,Sinks.Many<Message<PayloadData>> sinksMany) {
            this.resolver = resolver;
            // this.emitterProcessor = emitterProcessor;
            this.sinksMany= sinksMany;
            this.streamBridge = streamBridge;

        }

        @PostMapping(value = "/binding/{bindingName}")
        @ResponseStatus(code = HttpStatus.OK)
        public void sendToBindingWithStreamBridge(@RequestBody PayloadData payloadData, @PathVariable("bindingName") String bindingName
                                                                             
                                                                                                            ) {
            log.info("send: {} to {}",payloadData,bindingName );
            // streamBridge.send(bindingName+ "-out-0",payloadData);
            streamBridge.send("toCustomerId-1Stream-out-0",payloadData);
        }

        @PostMapping(value = "/destination/{type}/{cat}/{target}")
        @ResponseStatus(code = HttpStatus.OK)
        public void sendToDestinationWithStreamBridge(@RequestBody PayloadData payloadData, @PathVariable("type") String type,
                                                                                @PathVariable("cat") String cat,
                                                                                @PathVariable("target") String target
                                                                                                            ) {
            log.info("send: {} to {}",payloadData,type + "/" + cat + "/" + target);
            // streamBridge.send(type + "/" + cat + "/" + target,payloadData);
            streamBridge.send("dest/def/customerId-1-out-0",payloadData);

        }

        @PostMapping(value = "/{type}/{cat}/{target}")
        @ResponseStatus(code = HttpStatus.OK)
        public void send(@RequestBody PayloadData payloadData, @PathVariable("type") String type,
                                                   @PathVariable("cat") String cat,
                                                   @PathVariable("target") String target
                                                                             ) {
            log.info("send: {} to {}",payloadData,type + "/" + cat + "/" + target);
            resolver.resolveDestination(type + "/" + cat + "/" + target).send(MessageBuilder
                                                                                        .withPayload(payloadData)
                                                                                        // .setHeaderIfAbsent(MessageHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
                                                                                        .build());

        }

        @PostMapping(value = "/{target}")
        @ResponseStatus(code = HttpStatus.OK)
        public void defSend(@RequestBody PayloadData payloadData, @PathVariable("target") String target
                                                                             ) {
                                                                                    
            log.info("send: {} to {}",payloadData,TYPE_DEFAULT + "/" + CAT_DEFAULT + "/" + target);
            resolver.resolveDestination(TYPE_DEFAULT + "/" + CAT_DEFAULT + "/" + target).send(MessageBuilder
                                                                                                    .withPayload(payloadData)
                                                                                                    // .setHeaderIfAbsent(MessageHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
                                                                                                    .build());

        }

        @PostMapping(value = "/withSendto")
        @ResponseStatus(code = HttpStatus.OK)
        public void withSendto(@RequestBody PayloadData payloadData) {

            Message<PayloadData>  message = MessageBuilder
                                                    .withPayload(payloadData)
                                                    // .setHeader("spring.cloud.stream.sendto.destination", payloadData.getData())
                                                    // .setHeaderIfAbsent(MessageHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
                                                    .build();

            // emitterProcessor.onNext(message);
            sinksMany.emitNext(message,EmitFailureHandler.FAIL_FAST);
        }

  
        @PostMapping(value = "/withSendto2")
        @ResponseStatus(code = HttpStatus.OK)
        public void withSendto2(@RequestBody PayloadData payloadData) {

            Message<PayloadData>  message = MessageBuilder
                                                    .withPayload(payloadData)
                                                    // .setHeaderIfAbsent(BinderHeaders.TARGET_DESTINATION, payloadData.getData())
                                                    // .setHeaderIfAbsent(MessageHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
                                                    .build();

            // emitterProcessor.onNext(message);
            sinksMany.emitNext(message,EmitFailureHandler.FAIL_FAST);
        }

    }

    @Bean
    public Supplier<Flux<Message<PayloadData>>> supplierFlux(/*EmitterProcessor<Message<PayloadData>> emitterProcessor,*/Sinks.Many<Message<PayloadData>> sinksMany) {
        // return () -> emitterProcessor
        //                             // .asFlux()
        //                             // .log()
        //                             ;
        return () -> sinksMany.asFlux().log();
    }    

    @Bean
    public Function<Message<PayloadData>,Message<PayloadData>> transformData() {
        return message -> MessageBuilder
                                    .withPayload(PayloadData.of(message.getPayload().getData().toUpperCase()))
                                    .copyHeaders(message.getHeaders())
                                    .setHeaderIfAbsent("spring.cloud.stream.sendto.destination", "dest/def/customerId-2")
                                    .build()
                                    
                                    ;
                                    
    }

    @Bean
    public Function<Flux<Message<PayloadData>>,Flux<Message<PayloadData>>> transformDataFlux() {
        return flux -> flux.map(message -> 
                                    MessageBuilder
                                        .withPayload(PayloadData.of(message.getPayload().getData().toUpperCase()))
                                        .copyHeaders(message.getHeaders())
                                        .setHeaderIfAbsent("spring.cloud.stream.sendto.destination", "dest/def/customerId-2")
                                        .build())
                                        
                                        ;
                                    
    }

    // @Bean
    // public EmitterProcessor<Message<PayloadData>> emitterProcessor() {
    //     EmitterProcessor<Message<PayloadData>> emitterProcessor = EmitterProcessor.create(false);
    //     return emitterProcessor;
    // }

    @Bean
    public Sinks.Many<Message<PayloadData>> sinksMany() {
        return Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);
    }

     /**
     * 
     * You can also delegate to the framework to dynamically resolve the output destination by specifying spring.cloud.stream.sendto.destination 
     * header set to the name of the destination to be resolved.
     * 
     * our output is a Message with spring.cloud.stream.sendto.destination header set to the value of he input argument. The framework will consult 
     * this header and will attempt to create or discover a destination with that name and send output to it.
     */

    // @Bean
    // public Function<Message<PayloadData> , Message<PayloadData>>  process() {
    //     return value -> {
    //         log.info("Process Called");
    //         return MessageBuilder.withPayload(value.getPayload())
    //                         .setHeader("spring.cloud.stream.sendto.destination", "sendto-")
    //                         .build();
    //     } ;
    // }


   

    // @Configuration
    // @Slf4j
    // public static class TestSink  {

        @Bean 
        public Consumer<Flux<Message<PayloadData>>> fluxReceive1() {
            return flux -> flux
                            .subscribe(msg -> log.info("'{}' Data received from fluxReceive1 ......",msg.getPayload()))
                ;
        }

        @Bean 
        public Consumer<Message<PayloadData>> fluxReceive2() {
            return msg -> log
                            .info("'{}' Data received from fluxReceive2 ......",msg.getPayload())
                ;
        }

        @Bean 
        public Consumer<Message<PayloadData>> receive1() {
            return msg -> log
                            .info("'{}' Data received from receive1 ......",msg.getPayload())
                ;
        }

        @Bean 
        public Consumer<Flux<Message<PayloadData>>> receive2() {
            return flux -> flux
                            .subscribe(msg -> log.info("'{}' Data received from receive2 ......",msg.getPayload()))
                ;
        }
    // }
    
}
