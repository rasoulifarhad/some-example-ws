package com.example.democloudstreamwebfux.intergration.anothermessagechannel;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.integration.channel.AbstractSubscribableChannel;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.config.EnableIntegrationManagement;
import org.springframework.integration.config.EnableMessageHistory;
import org.springframework.integration.history.MessageHistory;
import org.springframework.integration.http.config.EnableIntegrationGraphController;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.converter.GenericMessageConverter;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;

import com.example.democloudstreamwebfux.intergration.ChannelConfiguration;
import com.example.democloudstreamwebfux.intergration.ChannelConfiguration.TestStringChannels;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import lombok.extern.slf4j.Slf4j;

/**
 * The MessageChannel interface only contains two send methods with the following signatures:
 * 
 *  - boolean send(Message<?> message)
 *    Sends a message to the message channel with an indefinite timeout and return true if sending the message was successful.
 *  - boolean send(Message<?> message, long timeout)
 *    Sends a message to the message channel with a specified timeout.
 *  
 * Sending one or more messages is done in almost all the examples related to message channels and no special examples will be given.
 * 
 * The AbstractMessageChannel class is the class from which all the different types of message channels inherit. It implements support for the following features:
 * 
 *     - Retaining a history of messages sent through the message channel (tracking).
 *     - Message channel statistics.
 *     - Logging.
 *       Outputs log messages during the different stages of sending a message to the message channel if logging is enabled and the log level is set to debug or lower.
 *     - Configuring one or more message datatypes accepted by the message channel.
 *       In addition, a message converter can be configured on message channels which will attempt to convert any message of other datatype sent to the message channel to a datatype accepted by the message channel.
 *     - Interceptors.
 * 
 */
@Slf4j
public class AnotherIntegrationApplication {
    
    /**
    * Creating a subscribable message channel and sending a message
    * to it without any subscribers being subscribed.
    * Expected result:
    * An exception should be thrown indicating that the message could
    * not be delivered.
    */
    @EnableAutoConfiguration
    @Configuration
    @IntegrationComponentScan
    @EnableIntegrationGraphController
    @Slf4j
    public static class NoSubscriberTestApplication {
        public static void main(String[] args) throws Exception {
              new SpringApplicationBuilder()
                      .sources(NoSubscriberTestApplication.class,TestStringChannels.class,ChannelConfiguration.class,
                      NoSubscriberTestConfig.class
                                          )
                      .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/intergration/anothermessagechannel/")
                      .run("--management.endpoints.web.exposure.include=*","--spring.main.lazy-initialization=false" 
                                  ,"--logging.level.root=INFO" 
                                  ,"--logging.level.org.springframework.retry.support.RetryTemplate=DEBUG"
                                  ,"--spring.cloud.stream.defaultBinder=rabbit");
        }
    }

    @Configuration
    @Slf4j
    public static class  NoSubscriberTestConfig {

        @Bean
        @Order(30)
        public ApplicationRunner noSubscriberRunner() {

            return args -> {
                final SubscribableChannel theSubscribableChannel ;
                final Message<String> theInputMessage ;

                theInputMessage = MessageBuilder
                                            .withPayload("hiiiiiiiiii")
                                            .build();

                theSubscribableChannel = new DirectChannel() ;

                ((AbstractSubscribableChannel)theSubscribableChannel).setBeanName("MessageChannelWithNoSubscribers");

                try {
                   theSubscribableChannel.send(theInputMessage);
                   log.info("must not print this.");
                } catch(MessageDeliveryException e) {

                    log.info("Error: {}",e);

                }
            };

        }
    }

    /**
    * Creating a subscribable message channel and subscribing one
    * subscriber to the channel. A message is then sent to the channel.
    * Expected result:
    * The single subscriber should receive the message sent to the
    * subscribable message channel.
    */

    @EnableAutoConfiguration
    @Configuration
    @IntegrationComponentScan
    @EnableIntegrationGraphController
    @Slf4j
    public static class SingleSubscriberTestApplication {
        public static void main(String[] args) throws Exception {
              new SpringApplicationBuilder()
                      .sources(SingleSubscriberTestApplication.class,TestStringChannels.class,ChannelConfiguration.class,
                      SingleSubscriberTestConfig.class
                                          )
                      .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/intergration/anothermessagechannel/")
                      .run("--management.endpoints.web.exposure.include=*","--spring.main.lazy-initialization=false" 
                                  ,"--logging.level.root=INFO" 
                                  ,"--logging.level.org.springframework.retry.support.RetryTemplate=DEBUG"
                                  ,"--spring.cloud.stream.defaultBinder=rabbit");
        }
    }

    @Configuration
    @Slf4j
    public static class  SingleSubscriberTestConfig {
        
        @Bean
        @Order(31)
        public ApplicationRunner singleSubscriberRunner() {
    
            return args -> {
                final SubscribableChannel theSubscribableChannel ;
                final Message<String> theInputMessage ;
                final List<Message> theSubscriberReceivedMessages = new CopyOnWriteArrayList<>();
    
                theInputMessage = MessageBuilder
                                            .withPayload("hiiiiiiiiii")
                                            .build();
    
                theSubscribableChannel = new DirectChannel() ;
    
                ((AbstractSubscribableChannel)theSubscribableChannel).setBeanName("MessageChannelWithSingleSubscriber");
    
                final MessageHandler theSubscriber = theSubscriberReceivedMessages::add;
    
                final boolean theSubscribedFlag =  theSubscribableChannel.subscribe(theSubscriber);
                log.info("theSubscriber subscribed? --> {}",theSubscribedFlag);
    
                theSubscribableChannel.send(theInputMessage);
    
                TimeUnit.SECONDS.sleep(2);
    
                log.info("#messages:{} , {} ",theSubscriberReceivedMessages.size(),theSubscriberReceivedMessages.get(0));
            };
        }
    }

    /**
     * Creating a subscribable message channel and subscribing two
     * subscribers to the channel. A message is then sent to the channel.
     * Expected result:
     * One single message should be received by one of the subscribers
     * subscribed to the message channel. No message should be received by
     * the other subscriber.
     * Note!
     * This behaviour is not common for all subscribable message channels!
     * The {@code PublishSubscribeChannel} will publish a message sent
     * to the message channel to all of its subscribers.
     */
    @EnableAutoConfiguration
    @Configuration
    @IntegrationComponentScan
    @EnableIntegrationGraphController
    @Slf4j
    public static class MultipleSubscriberTestApplication {
        public static void main(String[] args) throws Exception {
              new SpringApplicationBuilder()
                      .sources(MultipleSubscriberTestApplication.class,TestStringChannels.class,ChannelConfiguration.class,
                      MultipleSubscriberTestConfig.class
                                          )
                      .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/intergration/anothermessagechannel/")
                      .run("--management.endpoints.web.exposure.include=*","--spring.main.lazy-initialization=false" 
                                  ,"--logging.level.root=INFO" 
                                  ,"--logging.level.org.springframework.retry.support.RetryTemplate=DEBUG"
                                  ,"--spring.cloud.stream.defaultBinder=rabbit");
        }
    }

    @Configuration
    @Slf4j
    public static class  MultipleSubscriberTestConfig {

        @Bean 
        public  ApplicationRunner multipleSubscriberRunner() {

            return args -> {

                final SubscribableChannel theSubscribableChannel;
                final Message<String> theInputMessage ;
                final List<Message> theFirstSubscriberReceivedMessages = new CopyOnWriteArrayList<>();
                final List<Message> theSecondSubscriberReceivedMessages = new CopyOnWriteArrayList<>();

                theInputMessage = MessageBuilder.withPayload("hiiiiiiiiiiiiiiiiiiiiii").build();

                theSubscribableChannel = new DirectChannel();

                ((AbstractSubscribableChannel)theSubscribableChannel).setBeanName("MessageChannelWithMultipleSubscribers");


                final MessageHandler theFirstSubscriber = theFirstSubscriberReceivedMessages::add;
                final MessageHandler theSecondSubscriber = theSecondSubscriberReceivedMessages::add;

                final boolean  theFirstSubscribedFlag = theSubscribableChannel.subscribe(theFirstSubscriber);
                final boolean  theSecondSubscribedFlag = theSubscribableChannel.subscribe(theSecondSubscriber);

                log.info("theFirstSubscribedFlag subscribed? --> {}",theFirstSubscribedFlag);
                log.info("theSecondSubscribedFlag subscribed? --> {}",theSecondSubscribedFlag);

                theSubscribableChannel.send(theInputMessage);

                TimeUnit.SECONDS.sleep(2);

                log.info("first#messages:{} , {} ",theFirstSubscriberReceivedMessages.size(),
                    (theFirstSubscriberReceivedMessages.size() > 0 ? theFirstSubscriberReceivedMessages.get(0) : null));
                log.info("second#messages:{} , {} ",theSecondSubscriberReceivedMessages.size(),
                 (theSecondSubscriberReceivedMessages.size() > 0 ? theSecondSubscriberReceivedMessages.get(0) : null));
            };
        }
    }

    /**
     * Creating a subscribable message channel that publishes
     * messages sent to the message channel to all subscribers
     * (a {@code PublishSubscribeChannel})
     * and subscribing two subscribers to the channel.
     * Send a message to the message channel.
     * Unsubscribe one of the subscribers from the message channel.
     * Send another message to the message channel.
     * Expected result:
     * The first message should be received by both the subscribers.
     * The second message should be received only by the remaining
     * subscriber.
     */
    @EnableAutoConfiguration
    @Configuration
    @IntegrationComponentScan
    @EnableIntegrationGraphController
    @Slf4j
    public static class UnSubscribeTestApplication {
        public static void main(String[] args) throws Exception {
              new SpringApplicationBuilder()
                      .sources(UnSubscribeTestApplication.class,TestStringChannels.class,ChannelConfiguration.class,
                      UnSubscribeTestConfig.class
                                          )
                      .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/intergration/anothermessagechannel/")
                      .run("--management.endpoints.web.exposure.include=*","--spring.main.lazy-initialization=false" 
                                  ,"--logging.level.root=INFO" 
                                  ,"--logging.level.org.springframework.retry.support.RetryTemplate=DEBUG"
                                  ,"--spring.cloud.stream.defaultBinder=rabbit");
        }
    }

    @Configuration
    @Slf4j
    public static class  UnSubscribeTestConfig {

        @Bean 
        public  ApplicationRunner unSubscribeRunner() {
            return args -> {

                final SubscribableChannel theSubscribableChannel;

                final Message<String> theFirstInputMessage ;
                final Message<String> theSecondInputMessage ;
                final List<Message> theFirstSubscriberReceivedMessages = new CopyOnWriteArrayList<>();
                final List<Message> theSecondSubscriberReceivedMessages = new CopyOnWriteArrayList<>();

                theFirstInputMessage = MessageBuilder.withPayload("hiiiiiiiii_first").build();
                theSecondInputMessage = MessageBuilder.withPayload("hiiiiiiiii_second").build();

                theSubscribableChannel = new PublishSubscribeChannel();

                ((AbstractSubscribableChannel)theSubscribableChannel).setBeanName("MessageChannelToUnSubscribeFrom");


                final MessageHandler theFirstSubscriber = theFirstSubscriberReceivedMessages::add;
                final MessageHandler theSecondSubscriber = theSecondSubscriberReceivedMessages::add;

                final boolean  theFirstSubscribedFlag = theSubscribableChannel.subscribe(theFirstSubscriber);
                final boolean  theSecondSubscribedFlag = theSubscribableChannel.subscribe(theSecondSubscriber);

                log.info("theFirstSubscribedFlag subscribed? --> {}",theFirstSubscribedFlag);
                log.info("theSecondSubscribedFlag subscribed? --> {}",theSecondSubscribedFlag);

                log.info("subscribed count: {}",((AbstractSubscribableChannel)theSubscribableChannel).getSubscriberCount());
                
                theSubscribableChannel.send(theFirstInputMessage);

                TimeUnit.SECONDS.sleep(1);

                theSubscribableChannel.unsubscribe(theFirstSubscriber);

                log.info("subscribed count: {}",((AbstractSubscribableChannel)theSubscribableChannel).getSubscriberCount());

                theSubscribableChannel.send(theSecondInputMessage);

                TimeUnit.SECONDS.sleep(1);

                log.info("first#messages:{} , {} ",theFirstSubscriberReceivedMessages.size(),
                    (theFirstSubscriberReceivedMessages.size() > 0 ? theFirstSubscriberReceivedMessages.get(0) : null));
                log.info("second#messages:{} , {} ",theSecondSubscriberReceivedMessages.size(),
                 (theSecondSubscriberReceivedMessages.size() > 0 ? theSecondSubscriberReceivedMessages.get(0) : null));
            };
        }
    }

    /**
     * Tests enabling message history for a message channel.
     * Note that in this test, message history is only enabled for one single message channel.
     * To enable message history for all message channels, use the {@code @EnableMessageHistory}
     * annotation on a configuration class.
     *
     * Expected result: One message history entry should be generated for the message sent
     * and it should contain the name of the message channel to which the message was sent.
     *
     * @throws Exception If an error occurs. Indicates test failure.
     */
    /**
     * Message history is one of those patterns that helps by giving you an option to maintain some level of awareness of a message path either for debugging purposes or for maintaining an audit trail. Spring integration provides a simple way to configure your message flows to maintain the message history by adding a header to the message and updating that header every time a message passes through a tracked component.
     * 
     * To enable message history, you need only define the message-history element (or @EnableMessageHistory) in your configuration
     * 
     * Now every named component (component that has an 'id' defined) is tracked. The framework sets the 'history' header in your message. Its value a List<Properties>.
     * 
     * To get access to message history, you need only access the MessageHistory header. 
     * 
     * You might not want to track all of the components. To limit the history to certain components based on their names, you can provide the tracked-components attribute and specify a comma-delimited list of component names and patterns that match the components you want to track. 
     * 
     * @EnableMessageHistory("*Gateway", "sample*", "aName")
     */
    @EnableAutoConfiguration
    @Configuration
    @IntegrationComponentScan
    @EnableIntegrationGraphController
    @EnableMessageHistory
    @Slf4j
    public static class MessageHistoryTestApplication {
        public static void main(String[] args) throws Exception {
              new SpringApplicationBuilder()
                      .sources(MessageHistoryTestApplication.class,TestStringChannels.class,ChannelConfiguration.class,
                      MessageHistoryTestConfig.class
                                          )
                      .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/intergration/anothermessagechannel/")
                      .run("--management.endpoints.web.exposure.include=*","--spring.main.lazy-initialization=false" 
                                  ,"--logging.level.root=INFO" 
                                  ,"--logging.level.org.springframework.retry.support.RetryTemplate=DEBUG"
                                  ,"--spring.cloud.stream.defaultBinder=rabbit");
        }
    }

    @Configuration
    @Slf4j
    public static class  MessageHistoryTestConfig {

        @Bean 
        public  ApplicationRunner unSubscribeRunner() {
            return args -> {

                final AbstractMessageChannel theMessageChannel ;
                final Message<String> theInputMessage ;
                final Message<String> theInputMessage2 ;

                final List<Message> theSubscriberReceivedMessage  = new CopyOnWriteArrayList<>() ;

                // Create the message channel and enable message history for the individual message channel.
                theMessageChannel = new DirectChannel() ;
                theMessageChannel.setComponentName("DirectMessageChannelWithHistory");
                theMessageChannel.setShouldTrack(true);

                final MessageHandler theSubscriber = theSubscriberReceivedMessage::add ;
                ((DirectChannel)theMessageChannel).subscribe(theSubscriber);

                theInputMessage = MessageBuilder.withPayload("Hiiiiii").build();
                theInputMessage2 = MessageBuilder.withPayload("Hiiiiii_2").build();
                theMessageChannel.send(theInputMessage);
                theMessageChannel.send(theInputMessage2);

                TimeUnit.SECONDS.sleep(1);

                final Message<String> theFirstReceivedMessage = theSubscriberReceivedMessage.get(0);

                final MessageHistory theFirstReceivedMessageHistory = MessageHistory.read(theFirstReceivedMessage);
                final Properties theMessageHistoryEntry = theFirstReceivedMessageHistory.get(0);

                log.info("Message History Object: {}",theFirstReceivedMessageHistory);
                log.info("Message History Entry: {}",theMessageHistoryEntry);

            };
        }
    }

    @EnableAutoConfiguration
    @Configuration
    @IntegrationComponentScan
    @EnableIntegrationGraphController
    @EnableIntegrationManagement    
    @Slf4j
    public static class MessageChannelStatisticsFullTestApplication {
        public static void main(String[] args) throws Exception {
              new SpringApplicationBuilder()
                      .sources(MessageChannelStatisticsFullTestApplication.class,TestStringChannels.class,ChannelConfiguration.class,
                      MessageChannelStatisticsFullTestConfig.class
                                          )
                      .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/intergration/anothermessagechannel/")
                      .run("--management.endpoints.web.exposure.include=*","--spring.main.lazy-initialization=false" 
                                  ,"--logging.level.root=INFO" 
                                  ,"--logging.level.org.springframework.retry.support.RetryTemplate=DEBUG"
                                  ,"--spring.cloud.stream.defaultBinder=rabbit");
        }
    }

    @Configuration
    @Slf4j
    public static class  MessageChannelStatisticsFullTestConfig {

        @Bean
        public MeterRegistry meterRegistry() {
            SimpleMeterRegistry registry = new SimpleMeterRegistry();
            return registry  ;
        }

        @Bean 
        public  ApplicationRunner messageChannelStatisticsFullRunner(MeterRegistry meterRegistry) {
            return args -> {

                final AbstractMessageChannel theMessageChannel ;
                final List<Message> theSubscriberReceivedMessage = new CopyOnWriteArrayList<>() ;

                theMessageChannel = new DirectChannel() ;
                theMessageChannel.setComponentName("DirectChannelWithMetricsFull");

                final MessageHandler theSubscriber = theSubscriberReceivedMessage::add ;
                ((DirectChannel)theMessageChannel).subscribe(theSubscriber);

                theMessageChannel.send(MessageBuilder.withPayload("1111111111111").build());

                TimeUnit.MILLISECONDS.sleep(40);
                theMessageChannel.send(MessageBuilder.withPayload("2222222222222222").build());
                TimeUnit.MILLISECONDS.sleep(50);
                theMessageChannel.send(MessageBuilder.withPayload("3333333333333").build());

                TimeUnit.SECONDS.sleep(1);

                
                log.info("{}",meterRegistry.get("spring.integration.send")
                                        .tag("type", "channel")
                                        .tag("name", "DirectChannelWithMetricsFull")
                                        .tag("result", "success")
                                        .timer()
                                        .count()
                );

                log.info("{}",meterRegistry.get("spring.integration.receive")
                                        .tag("type", "channel")
                                        .tag("name", "DirectChannelWithMetricsFull")
                                        .tag("result", "success")
                                        .counter()
                                        .count()
                );
                // log.info("{}",meterRegistry.get("spring.integration.send")
                //                         .tag("type", "handler")
                //                         .tag("name", "DirectChannelWithMetricsFull")
                //                         .tag("result", "success")
                //                         .timer()
                //                         .count()
                // );
                // log.info("{}",meterRegistry.get("spring.integration.receive")
                //                         .tag("type", "source")
                //                         .tag("name", "DirectChannelWithMetricsFull")
                //                         .tag("result", "success")
                //                         .counter()
                //                         .count()
                // );
                log.info("{}",meterRegistry.get("spring.integration.channels")
                                                            .gauge()
                                                            .value()
                );

                log.info("{}",meterRegistry.get("spring.integration.handlers")
                                                            .gauge()
                                                            .value()
                );
                log.info("{}",meterRegistry.get("spring.integration.sources")
                                                            .gauge()
                                                            .value()
                );
            };
        }
    }

    @EnableAutoConfiguration
    @Configuration
    @IntegrationComponentScan
    @EnableIntegrationGraphController
    @EnableIntegrationManagement    
    @Slf4j
    public static class RestrictDataTypesAlloowedTypeTestApplication {
        public static void main(String[] args) throws Exception {
              new SpringApplicationBuilder()
                      .sources(RestrictDataTypesAlloowedTypeTestApplication.class,TestStringChannels.class,ChannelConfiguration.class,
                      RestrictDataTypesAlloowedTypeTestConfig.class
                                          )
                      .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/intergration/anothermessagechannel/")
                      .run("--management.endpoints.web.exposure.include=*","--spring.main.lazy-initialization=false" 
                                  ,"--logging.level.root=INFO" 
                                  ,"--logging.level.org.springframework.retry.support.RetryTemplate=DEBUG"
                                  ,"--spring.cloud.stream.defaultBinder=rabbit");
        }
    }

    @Configuration
    @Slf4j
    public static class RestrictDataTypesAlloowedTypeTestConfig {

        @Bean 
        public  ApplicationRunner messageChannelStatisticsFullRunner(MeterRegistry meterRegistry) {
            return args -> {
                final AbstractMessageChannel theMessageChannel ;
                final Message<?> theInputMessage ;
                final List<Message<?>> theSubscriberReceivedMessage = new CopyOnWriteArrayList<>() ;

                theInputMessage = MessageBuilder.withPayload(new Long(1347)).build() ;
                
                theMessageChannel = new DirectChannel() ;
                theMessageChannel.setComponentName("MyDirectChannel");

                theMessageChannel.setDatatypes(Long.class);

                final MessageHandler theSubscriber = theSubscriberReceivedMessage::add;
                ((DirectChannel)theMessageChannel).subscribe(theSubscriber);

                theMessageChannel.send(theInputMessage);

                log.info("#{} message shoud have been received",theSubscriberReceivedMessage.size());
                log.info("Message received: {}",theSubscriberReceivedMessage.get(0));

            };
        }
    }

    @EnableAutoConfiguration
    @Configuration
    @IntegrationComponentScan
    @EnableIntegrationGraphController
    @EnableIntegrationManagement    
    @Slf4j
    public static class RestrictDataTypesNotAlloowedTypeTestApplication {
        public static void main(String[] args) throws Exception {
              new SpringApplicationBuilder()
                      .sources(RestrictDataTypesNotAlloowedTypeTestApplication.class,TestStringChannels.class,ChannelConfiguration.class,
                      RestrictDataTypesNotAlloowedTypeTestConfig.class
                                          )
                      .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/intergration/anothermessagechannel/")
                      .run("--management.endpoints.web.exposure.include=*","--spring.main.lazy-initialization=false" 
                                  ,"--logging.level.root=INFO" 
                                  ,"--logging.level.org.springframework.retry.support.RetryTemplate=DEBUG"
                                  ,"--spring.cloud.stream.defaultBinder=rabbit");
        }
    }

    @Configuration
    @Slf4j
    public static class RestrictDataTypesNotAlloowedTypeTestConfig {

        @Bean 
        public  ApplicationRunner messageChannelStatisticsFullRunner(MeterRegistry meterRegistry) {
            return args -> {

                final AbstractMessageChannel theMessageChannel ;
                final Message<?> theInputMessage ;
                final List<Message> theSubscriberReceivedMessages = new CopyOnWriteArrayList<>() ;

                theInputMessage = MessageBuilder.withPayload("1345").build();

                theMessageChannel = new DirectChannel() ;
                theMessageChannel.setComponentName("MyDirectChannel");

                theMessageChannel.setDatatypes(Long.class);

                final MessageHandler theSubscriber = theSubscriberReceivedMessages::add ;
                ((DirectChannel)theMessageChannel).subscribe(theSubscriber);

                try {
                    theMessageChannel.send(theInputMessage);
                } catch (final Exception e) {
                    log.info("Exception in send: {}",e);
                }

            };
        }
    }

    @EnableAutoConfiguration
    @Configuration
    @IntegrationComponentScan
    @EnableIntegrationGraphController
    @EnableIntegrationManagement    
    @Slf4j
    public static class RestrictMessageChannelDataTypesWithMessageConverterTestApplication {
        public static void main(String[] args) throws Exception {
              new SpringApplicationBuilder()
                      .sources(RestrictMessageChannelDataTypesWithMessageConverterTestApplication.class,TestStringChannels.class,ChannelConfiguration.class,
                      RestrictMessageChannelDataTypesWithMessageConverterTestConfig.class
                                          )
                      .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/intergration/anothermessagechannel/")
                      .run("--management.endpoints.web.exposure.include=*","--spring.main.lazy-initialization=false" 
                                  ,"--logging.level.root=INFO" 
                                  ,"--logging.level.org.springframework.retry.support.RetryTemplate=DEBUG"
                                  ,"--spring.cloud.stream.defaultBinder=rabbit");
        }
    }

    @Configuration
    @Slf4j
    public static class RestrictMessageChannelDataTypesWithMessageConverterTestConfig {

        @Bean 
        public  ApplicationRunner messageChannelStatisticsFullRunner(MeterRegistry meterRegistry) {
            return args -> {

                final AbstractMessageChannel theMessageChannel ;
                final Message<?> theInputMessage ;
                final List<Message> theSubscriberReceivedMessages = new CopyOnWriteArrayList<>() ;

                final GenericMessageConverter theMessageConverter ; 

                theInputMessage = MessageBuilder.withPayload("1345").build() ;

                theMessageChannel = new PublishSubscribeChannel() ;
                theMessageChannel.setComponentName("MyPublishSubscribeChannel");
                theMessageChannel.setDatatypes(Long.class);

                theMessageConverter = new GenericMessageConverter();
                theMessageChannel.setMessageConverter(theMessageConverter);

                final MessageHandler theSubscriber = theSubscriberReceivedMessages::add;
                ((PublishSubscribeChannel)theMessageChannel).subscribe(theSubscriber);

                theMessageChannel.send(theInputMessage);

                log.info("#{} message received",theSubscriberReceivedMessages.size());
                log.info("Received: {}",theSubscriberReceivedMessages.get(0));
            };
        }
    }

    /**
     * Message Channel Interceptors
     * 
     * Message channel interceptors are similar to around advice in aspect-oriented programming and the intercepting filter JavaEE pattern. For those familiar with Java servlets, an interceptor is very similar to a servlet filter.
     * 
     * In other words, a message channel interceptor intercepts sending and/or receiving of messages from a message channel at certain locations allowing for modification of messages. An interceptor can even stop the sending of a message to or receiving a message from a message channel from happening.
     * 
     * All channel interceptors in Spring Integration implement the ChannelInterceptor interface that contain the following methods:
     * 
     *    - Message<?> preSend(Message<?> message, MessageChannel channel)
     *      Invoked before a message is being sent to a message channel.
     *      Allows for returning a modified message, or null to stop sending message to channel.
     * 
     *    - void postSend(Message<?> message, MessageChannel channel, boolean sent)
     *      Invoked after a message has been sent to a message channel. The boolean indicates whether message was successfully sent.
     * 
     *    - void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex)
     *      Invoked after the sending of a message to a message channel has completed, regardless of any exception thrown during the send operation.
     * 
     *    - boolean preReceive(MessageChannel channel)
     *      Invoked before trying to receive a message from a pollable message channel. No message will be retrieved if this method returns false.
     * 
     *    - Message<?> postReceive(Message<?> message, MessageChannel channel)
     *      Invoked after a message has been received from a pollable message channel. Allows for returning a modified message, or null to stop any further interceptors from being called.
     * 
     *    - void afterReceiveCompletion(@Nullable Message<?> message, MessageChannel channel, Exception ex)
     *      Invoked after the receiving of a message from a pollable message channel has completed, regardless of any exceptions thrown during the receive operation.
     * 
     * 
     * 
     * 
     */
    @EnableAutoConfiguration
    @Configuration
    @IntegrationComponentScan
    @EnableIntegrationGraphController
    @EnableIntegrationManagement    
    @Slf4j
    public static class LoggingAndCountingChannelInterceptorTestApplication {
        public static void main(String[] args) throws Exception {
              new SpringApplicationBuilder()
                      .sources(LoggingAndCountingChannelInterceptorTestApplication.class,TestStringChannels.class,ChannelConfiguration.class,
                      LoggingAndCountingChannelInterceptorTestConfig.class
                                          )
                      .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/intergration/anothermessagechannel/")
                      .run("--management.endpoints.web.exposure.include=*","--spring.main.lazy-initialization=false" 
                                  ,"--logging.level.root=INFO" 
                                  ,"--logging.level.org.springframework.retry.support.RetryTemplate=DEBUG"
                                  ,"--spring.cloud.stream.defaultBinder=rabbit");
        }
    }

    /**
    * Message channel interceptor that logs information about the messages being
    * sent and received to/from the channel.
    * In addition, a count for each of the different intercepting points is
    * maintained counting the number of received messages.
    *
    */
    @Slf4j
    public static class LoggingAndCountingChannelInterceptor implements ChannelInterceptor {

        protected AtomicInteger mPreSendMessageCount = new AtomicInteger();
        protected AtomicInteger mPostSendMessageCount = new AtomicInteger() ;
        protected AtomicInteger mAfterSendCompletionMessageCount = new AtomicInteger() ;
        protected AtomicInteger mPreReceiveMessageCount = new AtomicInteger() ;
        protected AtomicInteger mPostReceiveMessageCount = new AtomicInteger();
        protected AtomicInteger mAfterReceivedCompletionMessageCount = new AtomicInteger() ;


        
        public int getmPreSendMessageCount() {
            return mPreSendMessageCount.get();
        }
        public int getmPostSendMessageCount() {
            return mPostSendMessageCount.get();
        }
        public int getmAfterSendCompletionMessageCount() {
            return mAfterSendCompletionMessageCount.get();
        }
        public int getmPreReceiveMessageCount() {
            return mPreReceiveMessageCount.get();
        }
        public int getmPostReceiveMessageCount() {
            return mPostReceiveMessageCount.get();
        }
        public int getmAfterReceivedCompletionMessageCount() {
            return mAfterReceivedCompletionMessageCount.get();
        }
        protected void logMessageWithChannelAndPayload(final String inLogMessage,
                                                       final Message<?>  inMessage,
                                                       final MessageChannel inMessageChannel,
                                                       final Object... inAdditionalInMessage ){

                                                        final int theAppendMsgParamsStartIndex =
                                                        (inAdditionalInMessage != null) ? inAdditionalInMessage.length : 0;
                                        
            String theLogMessage =
                new StringBuilder().append(inLogMessage)
                    .append(" Channel: {")
                    .append(theAppendMsgParamsStartIndex)
                    .append("}. Payload: {")
                    .append(theAppendMsgParamsStartIndex + 1)
                    .append("}")
                    .toString();

            final Object[] theLogMessageParameters;
            if (inAdditionalInMessage != null) {
                theLogMessageParameters = Arrays.copyOf(inAdditionalInMessage,
                    inAdditionalInMessage.length + 2);
            } else {
                theLogMessageParameters = new Object[2];
            }

            theLogMessageParameters[theAppendMsgParamsStartIndex] =
                (inMessageChannel != null)
                    ? inMessageChannel.toString() : "null message channel";
            theLogMessageParameters[theAppendMsgParamsStartIndex + 1] =
                (inMessage != null) ? inMessage.getPayload()
                    : "null message";
            theLogMessage =
                MessageFormat.format(theLogMessage, theLogMessageParameters);
        
            log.info("{}",theLogMessage);

        }
        @Override
        public void afterReceiveCompletion(Message<?> message, MessageChannel channel, Exception ex) {

            log.info(
                "After message receive completion. Channel: {} Payload: {}  Exception: {} " , 
                    channel.toString(),
                    message.getPayload(),
                    ex);
            mAfterReceivedCompletionMessageCount.incrementAndGet() ;
            ChannelInterceptor.super.afterReceiveCompletion(message, channel, ex);
        }

        @Override
        public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {

            logMessageWithChannelAndPayload("After completion of message sending. Exception: {0}", 
                                            message, 
                                            channel, 
                                            ex);

            mAfterSendCompletionMessageCount.incrementAndGet();
            ChannelInterceptor.super.afterSendCompletion(message, channel, sent, ex);
        }

        @Override
        public Message<?> postReceive(Message<?> message, MessageChannel channel) {

            logMessageWithChannelAndPayload("Post-receive.", 
                                            message, 
                                            channel, 
                                            (Object []) null);

            mPostReceiveMessageCount.incrementAndGet() ;
            return ChannelInterceptor.super.postReceive(message, channel);
        }

        @Override
        public void postSend(Message<?> message, MessageChannel channel, boolean sent) {

            logMessageWithChannelAndPayload("After message send.", 
                                            message, 
                                            channel, 
                                            (Object []) null);
            mPostSendMessageCount.incrementAndGet() ;
            ChannelInterceptor.super.postSend(message, channel, sent);
        }

        @Override
        public boolean preReceive(MessageChannel channel) {
            logMessageWithChannelAndPayload("Pre-receive.", 
                                            null, 
                                            channel, 
                                            (Object []) null);
            mPreReceiveMessageCount.incrementAndGet();
            return ChannelInterceptor.super.preReceive(channel);
        }

        @Override
        public Message<?> preSend(Message<?> message, MessageChannel channel) {
            logMessageWithChannelAndPayload("Before message send.", 
                                            message, channel,(Object []) null);

            mPreSendMessageCount.incrementAndGet();
            return ChannelInterceptor.super.preSend(message, channel);
        }
        
    }

    /** 
     * Adding an interceptor for a message channel and send some messages
     * to the message channel.
     * While one or more interceptors can be added to all types of message channels,
     * different types of message channels invoke different sets of methods on the
     * interceptors. Further examples can be found elsewhere.
     *
     * Expected result: The interceptor's preSend, postSend and afterSendCompletion
     * should be invoked once for every message sent.
     */
    @Configuration
    @Slf4j
    public static class LoggingAndCountingChannelInterceptorTestConfig {

        @Bean 
        public  ApplicationRunner messageChannelStatisticsFullRunner(MeterRegistry meterRegistry) {
            return args -> {

                final AbstractMessageChannel theMessageChannel ;
                final LoggingAndCountingChannelInterceptor theLoggingAndCountingChannelInterceptor;
                final List<Message> theSubscriberReceivedMessages = new CopyOnWriteArrayList<>();

                theMessageChannel = new DirectChannel();
                theMessageChannel.setComponentName("MyDirectChannel");
                theLoggingAndCountingChannelInterceptor = new LoggingAndCountingChannelInterceptor();
                theMessageChannel.addInterceptor(theLoggingAndCountingChannelInterceptor);

                final MessageHandler  theSubscriber = theSubscriberReceivedMessages::add;
                ((DirectChannel)theMessageChannel).subscribe(theSubscriber);

                theMessageChannel.send(MessageBuilder.withPayload("11111111111").build());
                TimeUnit.MILLISECONDS.sleep(50);

                theMessageChannel.send(MessageBuilder.withPayload("222222222222222222").build());
                TimeUnit.MILLISECONDS.sleep(70);

                theMessageChannel.send(MessageBuilder.withPayload("33333333333333333").build());
                TimeUnit.MILLISECONDS.sleep(30);

                theMessageChannel.send(MessageBuilder.withPayload("33333333333333333").build());
                TimeUnit.MILLISECONDS.sleep(120);

                theMessageChannel.send(MessageBuilder.withPayload("33333333333333333").build());
                TimeUnit.MILLISECONDS.sleep(65);

                log.info("getmAfterReceivedCompletionMessageCount: {}" ,theLoggingAndCountingChannelInterceptor.getmAfterReceivedCompletionMessageCount());
                log.info("getmAfterSendCompletionMessageCount: {}" ,theLoggingAndCountingChannelInterceptor.getmAfterSendCompletionMessageCount());
                log.info("getmPostReceiveMessageCount: {}" ,theLoggingAndCountingChannelInterceptor.getmPostReceiveMessageCount());
                log.info("getmPostReceiveMessageCount: {}" ,theLoggingAndCountingChannelInterceptor.getmPostReceiveMessageCount());
                log.info("getmPostSendMessageCount: {}" ,theLoggingAndCountingChannelInterceptor.getmPostSendMessageCount());
                log.info("getmPreReceiveMessageCount: {}" ,theLoggingAndCountingChannelInterceptor.getmPreReceiveMessageCount());
                log.info("getmPreSendMessageCount: {}" ,theLoggingAndCountingChannelInterceptor.getmPreSendMessageCount());
            };
        }
    }

}
