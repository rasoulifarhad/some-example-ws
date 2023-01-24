package com.example.democloudstreamwebfux.intergration.bridge;

import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.BridgeFrom;
import org.springframework.integration.annotation.BridgeTo;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.handler.BridgeHandler;
import org.springframework.integration.transformer.MessageTransformingHandler;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.MessageBuilder;

import lombok.extern.slf4j.Slf4j;



@SpringBootApplication
@Slf4j
public class BridgeIntegrationApplication {

    public static void main(String[] args) throws Exception {

        log.info("");
        ConfigurableApplicationContext context = 
                    new SpringApplicationBuilder()
                            .sources(BridgeIntegrationApplication.class,Boots.class,PrintService.class)
                            .bannerMode(Banner.Mode.OFF)
                            .properties("spring.config.name=bridgeIntegrationApplication")
                            .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/integrationApplication/bridge/")
                            // .web(WebApplicationType.SERVLET)
                            .run("--management.endpoints.web.exposure.include=*"
                                        ,"--spring.main.lazy-initialization=false" 
                                        ,"--logging.level.root=INFO" 
                                        );
    }

    @Bean(name = "pubSubLogChannel")
    public PublishSubscribeChannel pubSubLogChannel() {
        return MessageChannels
                    .publishSubscribe(Executors.newFixedThreadPool(10))
                    .get();
    }

    // @Bean
    // public IntegrationFlow integrationFlow() {
    //     return flow -> flow.log();
    // }


    
    // @Transformer
    // public Message<String> toUppercaTransformer(Message<String>  message)  {
    //     return MessageBuilder.withPayload(message.getPayload().toUpperCase())
    //                         .copyHeaders(message.getHeaders()) 
    //                         .build();
    // }

    @Bean(name = "myPubSubChannel")
    public PublishSubscribeChannel myPubSubChannel() {
        return MessageChannels
                    .publishSubscribe(Executors.newFixedThreadPool(10))
                    .get();
    }

    @Bean
    @ServiceActivator(inputChannel = "myPubSubChannel" )
    public MessageHandler myService() {


        MessageTransformingHandler messageTransformingHandler = new MessageTransformingHandler(new org.springframework.integration.transformer.Transformer() {

            @Override
            public Message<?> transform(Message<?> message) {
                        return MessageBuilder.withPayload(((String)message.getPayload()).toUpperCase())
                        .copyHeaders(message.getHeaders()) 
                        .build();
            }
            
        });
        // messageTransformingHandler.setLoggingEnabled(true);
        return messageTransformingHandler;
    }



    @Bean(name = "printServiceChannel")
    public PublishSubscribeChannel printServiceChannel() {
        return MessageChannels
                    .publishSubscribe(Executors.newFixedThreadPool(10))
                    .get();
    }



    @Bean(name = "polledChannelPrintService")
    public QueueChannel  polledChannelPrintService() {
        return MessageChannels
                    .queue()
                    .get();
    }

    @Bean(name = "printServiceDirectChannelBridgeFrom")
    @BridgeFrom(value = "polledChannelPrintService" , poller = @Poller(fixedDelay = "5000"))
    public SubscribableChannel printServiceDirectChannelBridgeFrom() {
        return MessageChannels
                    .direct()
                    .get();
    }




    @Bean(name = "polledChannelPrintServiceBridgeTo")
    @BridgeTo(value = "printServiceDirectChannelBridgeTo" , poller = @Poller(fixedDelay = "5000"))
    public QueueChannel  polledChannelPrintServiceBridgeTo() {
        return MessageChannels
                    .queue()
                    .get();
    }

    @Bean(name = "printServiceDirectChannelBridgeTo")
    public SubscribableChannel printServiceDirectChannelBridgeTo() {
        return MessageChannels
                    .direct()
                    .get();
    }

    @MessageEndpoint
    @Slf4j
    public static class PrintService {

        @ServiceActivator(inputChannel = "printServiceChannel")
        public String print(String string) {
            log.info("PrintService printed: {}",string);

            return "Responce From PrintService";
        }

        @ServiceActivator(inputChannel = "printServiceDirectChannelBridgeFrom")
        public String printBridge(String string) {
            log.info("printServiceDirectChannelBridgeFrom PrintService printed: {}",string);

            return "Responce: " + string.toUpperCase();
        }

        @ServiceActivator(inputChannel = "printServiceDirectChannelBridgeTo")
        public String printBridgeTo(String string) {
            log.info("printServiceDirectChannelBridgeTo PrintService printed: {}",string);

            return "Responce: " + string.toUpperCase();
        }

        @ServiceActivator(inputChannel = "direct")
        public String directService(String string) {
    
            log.info("directService : {}",string);
            return "Responce: " +  string.toUpperCase() ;
    
    
        }
    

    }

    @Bean
    public QueueChannel polled() {
        return MessageChannels
                            .queue()
                            .get();
    }

    @Bean
    public DirectChannel direct() {
        return MessageChannels
                            .direct()
                            .get();
    }

    @Bean
    @ServiceActivator(inputChannel = "polled", poller = @Poller(fixedDelay = "5000")) 
    public BridgeHandler bridge(@Qualifier("direct") DirectChannel direct ) {
        BridgeHandler  bridge = new BridgeHandler();
        bridge.setOutputChannel(direct);
        return bridge;
    }
    
    @Bean(name = "polledDSL")
    public QueueChannel polledDSL() {
        return MessageChannels
                            .queue()
                            .get();
    }

    @Bean(name = "directDSL")
    public DirectChannel directDSL() {
        return MessageChannels 
                            .direct()
                            .get();

    }

    @Bean
    public IntegrationFlow bridgeFlow(@Qualifier("polledDSL") QueueChannel polledDSL ,@Qualifier("directDSL") DirectChannel directDSL) {
        return IntegrationFlows
                            .from(polledDSL)
                            .bridge(bh ->  bh.poller(Pollers.fixedDelay(5000).maxMessagesPerPoll(1)))
                            .channel(directDSL)
                            .get();
    }


    @ServiceActivator(inputChannel = "directDSL")
    public String directServiceDSL(String string) {

        log.info("directServiceDSL : {}",string);
        return "Responce: " +  string.toUpperCase() ;


    }

    
}
