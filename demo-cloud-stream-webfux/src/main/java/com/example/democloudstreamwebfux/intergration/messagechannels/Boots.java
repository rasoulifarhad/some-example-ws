package com.example.democloudstreamwebfux.intergration.messagechannels;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.ExecutorChannel;
import org.springframework.integration.channel.PriorityChannel;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.channel.RendezvousChannel;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class Boots {
    

    @Bean
    @Order(1)
    ApplicationRunner runnerDirectChannel(@Qualifier("numberChannel") MessageChannel numberChannel) {
        return args ->  {

            ((DirectChannel)numberChannel).subscribe(message -> log.info("DirectChannel(numberChannel) Received: {}",message.getPayload()));

            numberChannel.send(new GenericMessage<Integer>(10));
    
            numberChannel.send(new GenericMessage<String>("5"));
    
        };
    }

    @Bean
    @Order(2)
    ApplicationRunner runnerQueueChannel(@Qualifier("queueChannel") QueueChannel queueChannel) {
        return args ->  {

            queueChannel.send(new GenericMessage<String>("QueueChannel hello"));
            queueChannel.send(new GenericMessage<String>("QueueChannel boy"));
    
            Message<?> first =  queueChannel.receive();
            Message<?> second =  queueChannel.receive();
            log.info("QueueChannel First Received: {}",first.getPayload());
            log.info("QueueChannel Second Received: {}",second.getPayload());
        
        };
    }


    @Bean
    @Order(3)
    ApplicationRunner runnerPubSibChannel(@Qualifier("pubSubhannel") PublishSubscribeChannel pubSubhannel) {
        return args ->  {

            
            pubSubhannel.send(new GenericMessage<String>("PubSubhannel hello"));
            pubSubhannel.send(new GenericMessage<String>("PubSubhannel boy"));
    

            pubSubhannel.subscribe(message -> log.info("PubSubhannel Subscriber1 received: {}",message.getPayload()));
            pubSubhannel.subscribe(message -> log.info("PubSubhannel Subscriber2 received: {}",message.getPayload()));
            pubSubhannel.subscribe(message -> log.info("PubSubhannel Subscriber3 received: {}",message.getPayload()));
        };
    }


    @Bean
    @Order(4)
    ApplicationRunner runnerAnotherPubSibChannel(@Qualifier("anotherPubSubhannel") PublishSubscribeChannel pubSubhannel) {
        return args ->  {

            pubSubhannel.subscribe(message -> log.info("PubSubhannel Another Subscriber1 received: {}",message.getPayload()));
            pubSubhannel.subscribe(message -> log.info("PubSubhannel Another Subscriber1 received: {}",message.getPayload()));
            pubSubhannel.subscribe(message -> log.info("PubSubhannel Another Subscriber2 received: {}",message.getPayload()));
            pubSubhannel.subscribe(message -> log.info("PubSubhannel Another Subscriber3 received: {}",message.getPayload()));
            pubSubhannel.subscribe(message -> log.info("PubSubhannel Another Subscriber4 received: {}",message.getPayload()));
            pubSubhannel.subscribe(message -> log.info("PubSubhannel Another Subscriber5 received: {}",message.getPayload()));
            pubSubhannel.subscribe(message -> log.info("PubSubhannel Another Subscriber6 received: {}",message.getPayload()));

            pubSubhannel.send(new GenericMessage<String>("Another PubSubhannel hello"));
            pubSubhannel.send(new GenericMessage<String>("Another PubSubhannel boy"));

            };
    }

    @Bean
    @Order(5)
    ApplicationRunner runnerExecutorChannel(@Qualifier("executorChannel") ExecutorChannel executorChannel) {
        return args ->  {

            executorChannel.subscribe(message -> log.info("ExecutorChannel Subscriber1 received: {}",message.getPayload()));
            executorChannel.subscribe(message -> log.info("ExecutorChannel Subscriber2 received: {}",message.getPayload()));
            
            TimeUnit.MILLISECONDS.sleep(200);

            executorChannel.send(new GenericMessage<String>("ExecutorChannel hello"));
            TimeUnit.MILLISECONDS.sleep(10);
            executorChannel.send(new GenericMessage<String>("ExecutorChannel boy"));

            TimeUnit.MILLISECONDS.sleep(500);
    
        };
    }

    @Bean
    @Order(6)
    ApplicationRunner runnerIntPriorityChannel(@Qualifier("intPriorityChannel") PriorityChannel intPriorityChannel) {
        return args -> {

            Random random = new Random(System.currentTimeMillis());

            IntStream intStream = random.ints(10,1000,10000);
            // IntStream intStream = random.ints().limit(10);

            // random.ints().limit(10).forEach(
            // random.ints(10,1000,10000).forEach(
            intStream.forEach(
                    value -> {

                    log.info("intPriorityChannel sended: {}",value);
                    intPriorityChannel.send(new GenericMessage<Integer>(value));

                    try {
                        TimeUnit.MILLISECONDS.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
        
                }
            );

            TimeUnit.MILLISECONDS.sleep(200);

            random.ints().limit(10).forEach(
                value -> {

                    log.info("intPriorityChannel received: {}",intPriorityChannel.receive().getPayload());

                    try {
                        TimeUnit.MILLISECONDS.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }                    
                }
            );

        };
    }


    @Bean
    @Order(7)
    ApplicationRunner runnerPriorityChannelLifo(@Qualifier("priorityChannel") PriorityChannel priorityChannel) {
        return args -> {

            Random random = new Random(System.currentTimeMillis());

            IntStream intStream = random.ints(10,1000,10000);
            // IntStream intStream = random.ints().limit(10);

            // random.ints().limit(10).forEach(
            intStream.forEach(
                value -> {

                    log.info("PriorityChannel sended: {}",value);
                    priorityChannel.send(new GenericMessage<String>("" + value));

                    try {
                        TimeUnit.MILLISECONDS.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
        
                }
            );

            TimeUnit.MILLISECONDS.sleep(200);

            random.ints().limit(10).forEach(
                value -> {

                    Message<?> msg = priorityChannel.receive() ;

                    log.info("PriorityChannel received: {} at {} ",msg.getPayload(),msg.getHeaders().getTimestamp());

                    try {
                        TimeUnit.MILLISECONDS.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }                    
                }
            );

         };
    }


    @Bean
    @Order(8)
    ApplicationRunner runnerRendezvousChannel(@Qualifier("rendezvousChannel") RendezvousChannel rendezvousChannel) {
        return args ->  {

            Executors.newSingleThreadExecutor().submit(
                () -> {
                    rendezvousChannel.send(new GenericMessage<String>("rendezvousChannel hello"));
                    log.info("rendezvousChannel Sended:  Hello");

                }
            );
            
            Executors.newSingleThreadExecutor().submit(
                () -> {
                    Message<?> outMessage =  rendezvousChannel.receive();
                    log.info("rendezvousChannel Received: {}",outMessage.getPayload());
        
                }
            );
        };
    }

    @Bean
    @Order(9)
    ApplicationRunner runnerWireTapedQueueChannel(@Qualifier("wireTapedQueueChannel") QueueChannel wireTapedQueueChannel,
                                                @Qualifier("logChannel") QueueChannel logChannel
                                                                                    ) {
        return args ->  {

            wireTapedQueueChannel.send(new GenericMessage<String>("wireTapedQueueChannel hello"));
            wireTapedQueueChannel.send(new GenericMessage<String>("wireTapedQueueChannel boy"));
    
            Message<?> first =  wireTapedQueueChannel.receive();
            Message<?> second =  wireTapedQueueChannel.receive();
            log.info("wireTapeQqueueChannel First Received: {}",first.getPayload());
            log.info("wireTapeQqueueChannel Second Received: {}",second.getPayload());


            Executors.newSingleThreadExecutor().submit(
                () -> {
                    log.info("logChannel First Received: {}",logChannel.receive());
                    log.info("logChannel Second Received: {}",logChannel.receive());
                
                }
            );

        
        };
    }


    
}
