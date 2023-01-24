package com.example.democloudstreamwebfux.intergration;

import java.util.Date;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.messaging.MessagingException;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class ChannelConfiguration {

        @Bean(name = "logChannel")
        public PublishSubscribeChannel logChannel() {
            return MessageChannels
                        .publishSubscribe()
                        .datatype(String.class)
                        .get()
                        ;
        }

        @Bean(name = "exec")
        public AsyncTaskExecutor exec() {
            SimpleAsyncTaskExecutor simpleAsyncTaskExecutor = new SimpleAsyncTaskExecutor();
            simpleAsyncTaskExecutor.setThreadNamePrefix("exec-");
            return simpleAsyncTaskExecutor ;
        }

        

        @Bean(name = "pubSubRecoveryChannel")
        public PublishSubscribeChannel pubSubRecoveryChannel() {
            return MessageChannels
                            .publishSubscribe()
                            .datatype(MessagingException.class)
                            .get()
                            ;
        }

        @Bean(name = "directRecoveryChannel")
        public DirectChannel directRecoveryChannel() {
            return MessageChannels
                            .direct()
                            .datatype(MessagingException.class)
                            .get()
                            ;
        }
 
        @Bean(name = "recoveryChannel")
        public QueueChannel recoveryChannel() {
            return MessageChannels
                            .queue()
                            .datatype(MessagingException.class)
                            .get()
                            ;
        }

        @Bean(name = "routingChannel")
        public QueueChannel routingChannel() {
            return MessageChannels
                            .queue()
                            .datatype(String.class,Integer.class,Long.class)
                            .get()
                            ;
        }

        @Bean(name = "stringRoutingChannel")
        public QueueChannel stringRoutingChannel() {
            return MessageChannels
                            .queue()
                            .datatype(String.class,Integer.class,Long.class)
                            .get()
                            ;
        }

        @Bean
        public PublishSubscribeChannel stringChannel() {
            return MessageChannels
                            .publishSubscribe()
                            .datatype(String.class)
                            .get()
                            ;
        }
        
        @Bean
        public PublishSubscribeChannel integerChannel() {
            return MessageChannels
                            .publishSubscribe()
                            .datatype(Integer.class)
                            .get()
                            ;
        }

        @Bean
        public PublishSubscribeChannel longChannel() {
            return MessageChannels
                            .publishSubscribe()
                            .datatype(Long.class)
                            .get()
                            ;
        }

        @Bean
        @Order(1)
        ApplicationRunner channelSubscriberRunner(  
                                                    PublishSubscribeChannel integerChannel,
                                                    PublishSubscribeChannel longChannel,
                                                    PublishSubscribeChannel stringChannel ,
                                                    PublishSubscribeChannel pubSubRecoveryChannel,
                                                    PublishSubscribeChannel logChannel
                                                    ) {
            return args ->  {

                longChannel
                            .subscribe(message -> log.info("longChannel: {} ",message) );

                integerChannel
                            .subscribe(message -> log.info("integrChannel: {} ",message) );
                
                stringChannel
                            .subscribe(message -> log.info("stringChannel: {} ",message) );

                            
                pubSubRecoveryChannel
                            .subscribe(message -> log.info("pubSubRecoveryChannel: {} ",message) );


                logChannel
                            .subscribe(message -> log.info("logChannel: {} ",message) );

            };
        }

        @Configuration
        @Slf4j
        public static class TestStringChannels {
    
            @Bean(name = "stringChannel1")
            public PublishSubscribeChannel stringChannel1() {
                return MessageChannels
                                .publishSubscribe()
                                .datatype(String.class)
                                .get()
                                ;
            }
        
            @Bean(name = "stringChannel2")
            public PublishSubscribeChannel stringChannel2() {
                return MessageChannels
                                .publishSubscribe()
                                .datatype(String.class)
                                .get()
                                ;
            }
    
            @Bean(name = "stringChannel3")
            public PublishSubscribeChannel stringChannel3() {
                return MessageChannels
                                .publishSubscribe()
                                .datatype(String.class)
                                .get()
                                ;
            }
    
            @Bean(name = "stringChannel4")
            public PublishSubscribeChannel stringChannel4() {
                return MessageChannels
                                .publishSubscribe()
                                .datatype(String.class)
                                .get()
                                ;
            }
            @Bean(name = "stringChannel5")
            public PublishSubscribeChannel stringChannel5() {
                return MessageChannels
                                .publishSubscribe()
                                .datatype(String.class)
                                .get()
                                ;
            }
            @Bean(name = "stringChannel6")
            public PublishSubscribeChannel stringChannel6() {
                return MessageChannels
                                .publishSubscribe()
                                .datatype(String.class)
                                .get()
                                ;
            }
            @Bean
            @Order(2)
            ApplicationRunner subscribtionToLongChannel(@Qualifier("stringChannel1") PublishSubscribeChannel stringChannel1,
                                                        @Qualifier("stringChannel2") PublishSubscribeChannel stringChannel2,
                                                        @Qualifier("stringChannel3") PublishSubscribeChannel stringChannel3,
                                                        @Qualifier("stringChannel4") PublishSubscribeChannel stringChannel4,
                                                        @Qualifier("stringChannel5") PublishSubscribeChannel stringChannel5,
                                                        @Qualifier("stringChannel6") PublishSubscribeChannel stringChannel6
                                                        ) {
        
                return args -> {
                    stringChannel1.subscribe(message -> log.info("stringChannel_1: {}",message));
    
                    stringChannel2.subscribe(message -> log.info("stringChannel_2: {}",message));

                    stringChannel3.subscribe(message -> log.info("stringChannel_3: {}",message));
    
                    stringChannel4.subscribe(message -> log.info("stringChannel_4: {}",message));
    
                    stringChannel5.subscribe(message -> log.info("stringChannel_5: {}",message));
    
                    stringChannel6.subscribe(message -> log.info("stringChannel_6: {}",message));
    
                };
            }
        }
    

        @Configuration
        @Slf4j
        public static class TestDateChannels {
    
            @Bean(name = "dateQueueChannel1")
            public QueueChannel dateQueueChannel1() {
                return MessageChannels
                                .queue()
                                .datatype(Date.class)
                                .get()
                                ;
            }

            @Bean(name = "datePubSubChannel1")
            public PublishSubscribeChannel datePubSubChannel1() {
                return MessageChannels
                                .publishSubscribe()
                                .datatype(Date.class)
                                .get()
                                ;
            }
        
            @Bean(name = "datePubSubChannel2")
            public PublishSubscribeChannel datePubSubChannel2() {
                return MessageChannels
                                .publishSubscribe()
                                .datatype(Date.class)
                                .get()
                                ;
            }
        
            @Bean(name = "datePubSubChannel3")
            public PublishSubscribeChannel datePubSubChannel3() {
                return MessageChannels
                                .publishSubscribe()
                                .datatype(Date.class)
                                .get()
                                ;
            }

            @Bean
            @Order(3)
            ApplicationRunner subscribtionToDateChannel(@Qualifier("datePubSubChannel1") PublishSubscribeChannel datePubSubChannel1,
                                                        @Qualifier("datePubSubChannel2") PublishSubscribeChannel datePubSubChannel2,
                                                        @Qualifier("datePubSubChannel3") PublishSubscribeChannel datePubSubChannel3
                                                        ) {
        
                return args -> {
                    datePubSubChannel1.subscribe(message -> log.info("datePubSubChannel_1: {}",message));
    
                    datePubSubChannel2.subscribe(message -> log.info("datePubSubChannel_2: {}",message));

                    datePubSubChannel3.subscribe(message -> log.info("datePubSubChannel_3: {}",message));
    
    
                };
            }
        }

        @Configuration
        @Slf4j
        public static class TestIntegerChannels {
    
            @Bean(name = "intQueueChannel1")
            public QueueChannel intQueueChannel1() {
                return MessageChannels
                                .queue()
                                .datatype(Integer.class)
                                .get()
                                ;
            }

            @Bean(name = "intPubSubChannel1")
            public PublishSubscribeChannel intPubSubChannel1() {
                return MessageChannels
                                .publishSubscribe()
                                .datatype(Integer.class)
                                .get()
                                ;
            }
        
            @Bean(name = "intPubSubChannel2")
            public PublishSubscribeChannel intPubSubChannel2() {
                return MessageChannels
                                .publishSubscribe()
                                .datatype(Integer.class)
                                .get()
                                ;
            }

            @Bean(name = "intPubSubChannel3")
            public PublishSubscribeChannel intPubSubChannel3() {
                return MessageChannels
                                .publishSubscribe()
                                .datatype(Integer.class)
                                .get()
                                ;
            }

            @Bean(name = "intPubSubChannel4")
            public PublishSubscribeChannel intPubSubChannel4() {
                return MessageChannels
                                .publishSubscribe()
                                .datatype(Integer.class)
                                .get()
                                ;
            }

            @Bean(name = "intPubSubChannel5")
            public PublishSubscribeChannel intPubSubChannel5() {
                return MessageChannels
                                .publishSubscribe()
                                .datatype(Integer.class)
                                .get()
                                ;
            }

            @Bean(name = "intPubSubChannel6")
            public PublishSubscribeChannel intPubSubChannel6() {
                return MessageChannels
                                .publishSubscribe()
                                .datatype(Integer.class)
                                .get()
                                ;
            }

            @Bean(name = "intPubSubChannel7")
            public PublishSubscribeChannel intPubSubChannel7() {
                return MessageChannels
                                .publishSubscribe()
                                .datatype(Integer.class)
                                .get()
                                ;
            }


             @Bean
            @Order(3)
            ApplicationRunner subscribtionToIntChannel(@Qualifier("intPubSubChannel1") PublishSubscribeChannel intPubSubChannel1,
                                                        @Qualifier("intPubSubChannel2") PublishSubscribeChannel intPubSubChannel2,
                                                        @Qualifier("intPubSubChannel3") PublishSubscribeChannel intPubSubChannel3,
                                                        @Qualifier("intPubSubChannel4") PublishSubscribeChannel intPubSubChannel4,
                                                        @Qualifier("intPubSubChannel5") PublishSubscribeChannel intPubSubChannel5,
                                                        @Qualifier("intPubSubChannel6") PublishSubscribeChannel intPubSubChannel6,
                                                        @Qualifier("intPubSubChannel7") PublishSubscribeChannel intPubSubChannel7
                                                        ) {
        
                return args -> {
                    intPubSubChannel1.subscribe(message -> log.info("intPubSubChannel_1: {}",message));
                    intPubSubChannel2.subscribe(message -> log.info("intPubSubChannel_2: {}",message));
                    intPubSubChannel3.subscribe(message -> log.info("intPubSubChannel_3: {}",message));
                    intPubSubChannel4.subscribe(message -> log.info("intPubSubChannel_4: {}",message));
                    intPubSubChannel5.subscribe(message -> log.info("intPubSubChannel_5: {}",message));
                    intPubSubChannel6.subscribe(message -> log.info("intPubSubChannel_6: {}",message));
                    intPubSubChannel7.subscribe(message -> log.info("intPubSubChannel_7: {}",message));

    
                };
            }
        }

        @Configuration
        @Slf4j
        public static class TestLongChannels {
    
            @Bean(name = "longQueueChannel1")
            public QueueChannel longQueueChannel1() {
                return MessageChannels
                                .queue()
                                .datatype(Long.class)
                                .get()
                                ;
            }

            @Bean(name = "longPubSubChannel1")
            public PublishSubscribeChannel longPubSubChannel1() {
                return MessageChannels
                                .publishSubscribe()
                                .datatype(Long.class)
                                .get()
                                ;
            }
        
            @Bean(name = "longPubSubChannel2")
            public PublishSubscribeChannel longPubSubChannel2() {
                return MessageChannels
                                .publishSubscribe()
                                .datatype(Long.class)
                                .get()
                                ;
            }

            @Bean(name = "longPubSubChannel3")
            public PublishSubscribeChannel longPubSubChannel3() {
                return MessageChannels
                                .publishSubscribe()
                                .datatype(Long.class)
                                .get()
                                ;
            }

            @Bean(name = "longPubSubChannel4")
            public PublishSubscribeChannel longPubSubChannel4() {
                return MessageChannels
                                .publishSubscribe()
                                .datatype(Long.class)
                                .get()
                                ;
            }

            @Bean(name = "longPubSubChannel5")
            public PublishSubscribeChannel longPubSubChannel5() {
                return MessageChannels
                                .publishSubscribe()
                                .datatype(Long.class)
                                .get()
                                ;
            }

            @Bean(name = "longPubSubChannel6")
            public PublishSubscribeChannel longPubSubChannel6() {
                return MessageChannels
                                .publishSubscribe()
                                .datatype(Long.class)
                                .get()
                                ;
            }

            @Bean(name = "longPubSubChannel7")
            public PublishSubscribeChannel longPubSubChannel7() {
                return MessageChannels
                                .publishSubscribe()
                                .datatype(Long.class)
                                .get()
                                ;
            }


             @Bean
            @Order(4)
            ApplicationRunner subscribtionToLongChannel2(@Qualifier("longPubSubChannel1") PublishSubscribeChannel longPubSubChannel1,
                                                        @Qualifier("longPubSubChannel2") PublishSubscribeChannel longPubSubChannel2,
                                                        @Qualifier("longPubSubChannel3") PublishSubscribeChannel longPubSubChannel3,
                                                        @Qualifier("longPubSubChannel4") PublishSubscribeChannel longPubSubChannel4,
                                                        @Qualifier("longPubSubChannel5") PublishSubscribeChannel longPubSubChannel5,
                                                        @Qualifier("longPubSubChannel6") PublishSubscribeChannel longPubSubChannel6,
                                                        @Qualifier("longPubSubChannel7") PublishSubscribeChannel longPubSubChannel7
                                                        ) {
        
                return args -> {
                    longPubSubChannel1.subscribe(message -> log.info("longPubSubChannel_1: {}",message));
                    longPubSubChannel2.subscribe(message -> log.info("longPubSubChannel_2: {}",message));
                    longPubSubChannel3.subscribe(message -> log.info("longPubSubChannel_3: {}",message));
                    longPubSubChannel4.subscribe(message -> log.info("longPubSubChannel_4: {}",message));
                    longPubSubChannel5.subscribe(message -> log.info("longPubSubChannel_5: {}",message));
                    longPubSubChannel6.subscribe(message -> log.info("longPubSubChannel_6: {}",message));
                    longPubSubChannel7.subscribe(message -> log.info("longPubSubChannel_7: {}",message));

    
                };
            }
        }


    }
    

