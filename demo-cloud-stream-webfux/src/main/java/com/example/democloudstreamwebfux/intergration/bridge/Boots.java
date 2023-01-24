package com.example.democloudstreamwebfux.intergration.bridge;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.integration.IntegrationMessageHeaderAccessor;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.support.GenericMessage;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class Boots {

    @Bean
    @Order(10)
    ApplicationRunner runnerMessageTemplate(@Qualifier("pubSubLogChannel") PublishSubscribeChannel pubSubLogChannel) {
        return args ->  {

            pubSubLogChannel.subscribe(message -> log.info("Subscriber for pubSubLogChannel received: {} ",message));

            MessageHandler m ;
            MessagingTemplate messagingTemplate = new MessagingTemplate();
            messagingTemplate.send(pubSubLogChannel, new GenericMessage<String>("From MessageTemplate: hi"));;



        };
    }

    @Bean
    @Order(11)
    ApplicationRunner runnerMessageTemplate2(@Qualifier("myPubSubChannel") PublishSubscribeChannel myPubSubChannel
                                            ) {
        return args ->  {


            MessagingTemplate messagingTemplate = new MessagingTemplate();
            Message<?> responseMessage =  messagingTemplate.sendAndReceive(myPubSubChannel, new GenericMessage<String>("From MessageTemplate: hi"));;
            log.info("Received MessageTemplate {}",responseMessage);




        };
    }

    @Bean
    @Order(11)
    ApplicationRunner runnerPrintService(@Qualifier("printServiceChannel") PublishSubscribeChannel printServiceChannel
                                            ) {
        return args ->  {


            MessagingTemplate messagingTemplate = new MessagingTemplate();
            Message<?> responseMessage =  messagingTemplate.sendAndReceive(printServiceChannel, new GenericMessage<String>("From printServiceChannel: hi"));;
            log.info("Received printServiceChannel {}",responseMessage);




        };
    }

    @Bean
    @Order(12)
    ApplicationRunner runnePpollableChannelPrintServiceBridgeFrom(@Qualifier("polledChannelPrintService") QueueChannel  polledChannelPrintService
                                            ) {
        return args ->  {


            MessagingTemplate messagingTemplate = new MessagingTemplate();

            Random random = new Random() ;

            random.ints().limit(10).forEach(
                value -> {
                    Message<?> responseMessage =  messagingTemplate.sendAndReceive(polledChannelPrintService, new GenericMessage<String>("From runnePpollableChannelPrintServiceBridgeFrom: hi:" + value));;
                    log.info("Received runnePpollableChannelPrintServiceBridgeFrom {}",responseMessage);

                    try {
                        TimeUnit.SECONDS.sleep(2);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
        
                }
            );


        };
    }

    
    @Bean
    @Order(13)
    ApplicationRunner runnePpollableChannelPrintServiceBridgeTo(@Qualifier("polledChannelPrintServiceBridgeTo") QueueChannel  polledChannelPrintServiceBridgeTo
                                            ) {
        return args ->  {


            MessagingTemplate messagingTemplate = new MessagingTemplate();

            Random random = new Random() ;

            random.ints().limit(10).forEach(
                value -> {
                    Message<?> responseMessage =  messagingTemplate.sendAndReceive(polledChannelPrintServiceBridgeTo, new GenericMessage<String>("From runnePpollableChannelPrintServiceBridgeTo: hi:" + value));;
                    log.info("Received runnePpollableChannelPrintServiceBridgeTo {}",responseMessage);

                    try {
                        TimeUnit.SECONDS.sleep(2);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
        
                }
            );


        };
    }


    @Bean
    @Order(14)
    ApplicationRunner runnerDirectService(@Qualifier("polled") QueueChannel  polled
                                            ) {
        return args ->  {


            MessagingTemplate messagingTemplate = new MessagingTemplate();

            Random random = new Random() ;

            random.ints().limit(10).forEach(
                value -> {
                    Message<?> responseMessage =  messagingTemplate.sendAndReceive(polled, new GenericMessage<String>("From runnerDirectService: hi:" + value));;
                    log.info("Received runnerDirectService {}",responseMessage);

                    try {
                        TimeUnit.SECONDS.sleep(2);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
        
                }
            );


        };
    }

    @Bean
    @Order(15)
    ApplicationRunner runnerDirectServiceDSL( @Qualifier("polledDSL") QueueChannel polledDSL
                                            ) {
        return args ->  {


            MessagingTemplate messagingTemplate = new MessagingTemplate();

            Random random = new Random() ;

            random.ints().limit(10).forEach(
                value -> {
                    Message<?> responseMessage =  messagingTemplate.sendAndReceive(polledDSL, new GenericMessage<String>("From runnerDirectServiceDSL: hi:" + value));;
                    log.info("Received runnerDirectServiceDSL {}",responseMessage);
                    IntegrationMessageHeaderAccessor accessor = new IntegrationMessageHeaderAccessor(responseMessage);
                    log.info("ReplyChannel: {} ",accessor.getReplyChannel()) ;
                    try {
                        TimeUnit.SECONDS.sleep(2);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
        
                }
            );


        };
    }

    
}
