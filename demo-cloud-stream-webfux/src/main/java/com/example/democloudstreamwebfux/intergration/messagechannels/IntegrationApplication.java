package com.example.democloudstreamwebfux.intergration.messagechannels;

import java.util.Comparator;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.ExecutorChannel;
import org.springframework.integration.channel.PriorityChannel;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.channel.RendezvousChannel;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import lombok.extern.slf4j.Slf4j;


@SpringBootApplication
@Slf4j
public class IntegrationApplication {
    
    public static void main(String[] args) throws Exception {

        log.info("");
        // ConfigurableApplicationContext context = 
                    new SpringApplicationBuilder()
                            .sources(IntegrationApplication.class,Boots.class)
                            .bannerMode(Banner.Mode.OFF)
                            .properties("spring.config.name=integrationApplication")
                            .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/integrationApplication/messagechannels/")
                            // .web(WebApplicationType.SERVLET)
                            .run("--management.endpoints.web.exposure.include=*"
                                        ,"--spring.main.lazy-initialization=false" 
                                        ,"--logging.level.root=INFO" 
                                        );
    }


    @Bean(name = "numberChannel")
    public MessageChannel numberChannel() {
        // DirectChannel channel = new DirectChannel();
        DirectChannel channel = MessageChannels.direct().get();
        channel.setDatatypes(Number.class);
        return channel;
    }

    @Bean(name = "queueChannel")
    public QueueChannel queueChannel() {
        // QueueChannel queueChannel =  new QueueChannel(2) ;
        QueueChannel queueChannel =  MessageChannels.queue(2).get();

        queueChannel.setDatatypes(String.class);
        return queueChannel ;
    }

    @Bean(name = "pubSubhannel")
    public PublishSubscribeChannel pubSubhannel() {
        // PublishSubscribeChannel ch = new PublishSubscribeChannel(Executors.newFixedThreadPool(4));
        PublishSubscribeChannel ch = MessageChannels.publishSubscribe(Executors.newFixedThreadPool(4)).get();
        ch.setDatatypes(String.class);
        return ch ;
    }

    @Bean(name = "anotherPubSubhannel")
    public PublishSubscribeChannel anotherPubSubhannel() {
        // PublishSubscribeChannel ch = new PublishSubscribeChannel(Executors.newFixedThreadPool(4));
        PublishSubscribeChannel ch = MessageChannels.publishSubscribe(Executors.newFixedThreadPool(4)).get();

        ch.setDatatypes(String.class);
        return ch ;
    }

    @Bean(name = "executorChannel")
    public ExecutorChannel executorChannel() {
        // ExecutorChannel executorChannel =  new ExecutorChannel(Executors.newFixedThreadPool(5));
        ExecutorChannel executorChannel =  MessageChannels.executor(Executors.newFixedThreadPool(5)).get();

        executorChannel.setDatatypes(String.class);
        return executorChannel ;
    }

    @Bean(name = "priorityChannel")
    public PriorityChannel priorityChannel() {

        PriorityChannel priorityChannel = new PriorityChannel(100,new Comparator<Message<?>>() {

            @Override
            public int compare(Message<?> o1, Message<?> o2) {
                return  o1.getHeaders().getTimestamp().compareTo(o2.getHeaders().getTimestamp()) * -1 ;
                // return ((String)o1.getPayload()).compareTo(((String)o1.getPayload()));
            }

            
        });
        priorityChannel.setDatatypes(String.class);
        return priorityChannel;

    }

    @Bean(name = "intPriorityChannel")
    public PriorityChannel intPriorityChannel() {

        PriorityChannel intPriorityChannel = new PriorityChannel(100,new Comparator<Message<?>>() {

            @Override
            public int compare(Message<?> o1, Message<?> o2) {
                return ((Integer)o1.getPayload()).compareTo(((Integer)o1.getPayload()));
            }

            
        });
        intPriorityChannel.setDatatypes(Integer.class);
        return intPriorityChannel;

    }


    @Bean(name = "rendezvousChannel")
    public RendezvousChannel rendezvousChannel() {
        // RendezvousChannel rendezvousChannel = new RendezvousChannel() ;
        RendezvousChannel rendezvousChannel = MessageChannels.rendezvous().get() ;

        rendezvousChannel.setDatatypes(String.class);
        return rendezvousChannel ;

    }

    @Bean(name = "wireTapedQueueChannel")
    public QueueChannel wireTapedQueueChannel(@Qualifier("logChannel") QueueChannel logChannel) {
        return   MessageChannels
                        .queue(2)
                        .datatype(String.class)
                        .wireTap(logChannel)
                        .get();

    }

    @Bean(name = "logChannel")
    public QueueChannel logChannel() {
        return MessageChannels
                    .queue(20)
                    .get();
    }


}

