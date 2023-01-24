package com.example.democloudstreamwebfux.intergration.router;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.integration.annotation.Router;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.router.AbstractMessageRouter;
import org.springframework.integration.router.HeaderValueRouter;
import org.springframework.integration.router.PayloadTypeRouter;
import org.springframework.integration.router.RecipientListRouter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.messaging.support.MessageBuilder;

import com.example.democloudstreamwebfux.intergration.ChannelConfiguration;
import com.example.democloudstreamwebfux.intergration.ChannelConfiguration.TestStringChannels;

import lombok.extern.slf4j.Slf4j;

// @EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class,DataSourceTransactionManagerAutoConfiguration.class,
//     HibernateJpaAutoConfiguration.class,})
@Slf4j
public class RouterApplication {
    

    // public static void main(String[] args) throws Exception {

    //     log.info("");
    //     ConfigurableApplicationContext context = 
    //                 new SpringApplicationBuilder()
    //                         .sources(IntegrationApplication.class,Boots.class)
    //                         .bannerMode(Banner.Mode.OFF)
    //                         .properties("spring.config.name=integrationApplication")
    //                         .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/integrationApplication/messagechannels/")
    //                         // .web(WebApplicationType.SERVLET)
    //                         .run("--management.endpoints.web.exposure.include=*"
    //                                     ,"--spring.main.lazy-initialization=false" 
    //                                     ,"--logging.level.root=INFO" 
    //                                     );
    // }    

    @EnableAutoConfiguration    
    
    public static class App1 {
        public static void main(String[] args) throws Exception {
            new SpringApplicationBuilder()
                    .sources(App1.class,Boots.class,TestStringChannels.class,ChannelConfiguration.class,RoutingChannelConfig.class)
                    .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/integrationApplication/router/")
                    .run("--management.endpoints.web.exposure.include=*","--spring.main.lazy-initialization=false" 
                                ,"--logging.level.root=INFO" );
        }
    }

    @EnableAutoConfiguration    
    
    public static class App2 {
        public static void main(String[] args) throws Exception {
            new SpringApplicationBuilder()
                    .sources(App1.class,Boots.class,TestStringChannels.class,ChannelConfiguration.class,PayloadTypeRouterDslConfig.class)
                    .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/integrationApplication/router/")
                    .run("--management.endpoints.web.exposure.include=*","--spring.main.lazy-initialization=false" 
                                ,"--logging.level.root=INFO" );
        }
    }

    
    public static class RoutingChannelConfig {

        @Bean
        @ServiceActivator(inputChannel = "routingChannel")
        public PayloadTypeRouter payloadTypeRouter() {

            PayloadTypeRouter router = new PayloadTypeRouter();
            router.setChannelMapping(String.class.getName(), "stringChannel");
            router.setChannelMapping(Integer.class.getName(), "integerChannel");
            return router;
        }

        @Bean
        @Order(10)
        ApplicationRunner payloadTypeRouterRunner(  QueueChannel routingChannel ) {
            return args ->  {

                routingChannel.send( new GenericMessage<Integer>(Integer.valueOf(1234)));
                TimeUnit.MILLISECONDS.sleep(20);
                routingChannel.send( new GenericMessage<String>("StringPayload"));
            };
        }
    }

    
    public static class PayloadTypeRouterDslConfig {

        @Bean
        public IntegrationFlow routerFlow(PayloadTypeRouter payloadTypeRouter) {

            return IntegrationFlows
                            .from("routingChannel")
                            .route(payloadTypeRouter)
                            .get()
                            ;

        }

        @Bean
        @Order(20)
        ApplicationRunner payloadTypeRouterFlowRunner(  QueueChannel routingChannelFlow) {
            return args ->  {

                routingChannelFlow.send( new GenericMessage<Integer>(Integer.valueOf(5555)));
                TimeUnit.MILLISECONDS.sleep(20);
                routingChannelFlow.send( new GenericMessage<String>("StringPayloadFlow"));
            };
        }
    }

    @EnableAutoConfiguration    
    
    public static class App3 {
        public static void main(String[] args) throws Exception {
            new SpringApplicationBuilder()
                    .sources(App1.class,Boots.class,TestStringChannels.class,ChannelConfiguration.class,PayloadTypeRouterFunctionDslConfig.class)
                    .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/integrationApplication/router/")
                    .run("--management.endpoints.web.exposure.include=*","--spring.main.lazy-initialization=false" 
                                ,"--logging.level.root=INFO" );
        }
    }

    
    public static class PayloadTypeRouterFunctionDslConfig {

        @Bean
        public IntegrationFlow routerFlowRoutingFunctionDsl() {
            return IntegrationFlows
                                .from("routingChannel")
                                .<Object,Class<?>>route(Object::getClass, m -> m
                                            .channelMapping(String.class, "stringChannel")
                                            .channelMapping(Integer.class, "integerChannel")
                                )
                                .get()
                                ;
        }

        @Bean
        @Order(30)
        ApplicationRunner routerFlowRoutingFunctionDslRunner(  QueueChannel routingChannel) {
            return args ->  {

                routingChannel.send( new GenericMessage<Integer>(Integer.valueOf(6666)));
                TimeUnit.MILLISECONDS.sleep(20);
                routingChannel.send( new GenericMessage<String>("StringPayloadFlowDsl"));
            };
        }
    }

    @EnableAutoConfiguration    
    
    public static class App4 {
        public static void main(String[] args) throws Exception {
            new SpringApplicationBuilder()
                    .sources(App1.class,Boots.class,TestStringChannels.class,ChannelConfiguration.class,HeaderValueRouterConfig.class)
                    .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/integrationApplication/router/")
                    .run("--management.endpoints.web.exposure.include=*","--spring.main.lazy-initialization=false" 
                                ,"--logging.level.root=INFO" );
        }
    }


    
    public static class HeaderValueRouterConfig {

        @Bean
        @ServiceActivator(inputChannel = "routingChannel")
        public HeaderValueRouter headerValueRouter() {
            HeaderValueRouter router = new HeaderValueRouter("routeHeader");
            router.setChannelMapping("integer", "integerChannel");
            router.setChannelMapping("string", "stringChannel");
            return router ;
        }

        @Bean
        @Order(40)
        ApplicationRunner headerbasedRoutingChannelRunner(  QueueChannel routingChannel) {
            return args ->  {
                Message<?> integerMessage =  MessageBuilder
                                                    .withPayload(Integer.valueOf(7777))
                                                    .setHeader("routeHeader", "integerChannel")
                                                    .build() ;


                Message<?> stringMessage =  MessageBuilder
                                                    .withPayload("headerbasedRoutingChannel-StringPayload")
                                                    .setHeader("routeHeader", "stringChannel")
                                                    .build() ;


                routingChannel.send( integerMessage);
                TimeUnit.MILLISECONDS.sleep(20);
                routingChannel.send(stringMessage);
                                        
            };
        }
    }

    @EnableAutoConfiguration    
    
    public static class App5 {
        public static void main(String[] args) throws Exception {
            new SpringApplicationBuilder()
                    .sources(App1.class,Boots.class,TestStringChannels.class,ChannelConfiguration.class,HeaderValueRouterDslConfig.class)
                    .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/integrationApplication/router/")
                    .run("--management.endpoints.web.exposure.include=*","--spring.main.lazy-initialization=false" 
                                ,"--logging.level.root=INFO" );
        }
    }
    

    
    public static class HeaderValueRouterDslConfig {

         public HeaderValueRouter headerValueRouterFlow() {
            HeaderValueRouter router = new HeaderValueRouter("routeHeader");
            router.setChannelMapping("integer", "integerChannel");
            router.setChannelMapping("string", "stringChannel");
            return router ;
        }

        @Bean
        public IntegrationFlow headerRouteFlow() {
            return IntegrationFlows
                            .from("routingChannel")
                            .route(headerValueRouterFlow())
                            .get()
                            ;
        }

        @Bean
        @Order(50)
        ApplicationRunner headerbasedRoutingChannelFlowRunner(  QueueChannel routingChannel) {
            return args ->  {
                Message<?> integerMessage =  MessageBuilder
                                                    .withPayload(Integer.valueOf(8888))
                                                    .setHeader("routeHeader", "integerChannel")
                                                    .build() ;


                Message<?> stringMessage =  MessageBuilder
                                                    .withPayload("headerbasedRoutingChannelFlow-StringPayload")
                                                    .setHeader("routeHeader", "stringChannel")
                                                    .build() ;


                routingChannel.send( integerMessage);
                TimeUnit.MILLISECONDS.sleep(20);
                routingChannel.send(stringMessage);
                                        
            };
        }
    }

    @EnableAutoConfiguration    
    
    public static class App6 {
        public static void main(String[] args) throws Exception {
            new SpringApplicationBuilder()
                    .sources(App1.class,Boots.class,TestStringChannels.class,ChannelConfiguration.class,RecipientListRouterConfig.class)
                    .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/integrationApplication/router/")
                    .run("--management.endpoints.web.exposure.include=*","--spring.main.lazy-initialization=false" 
                                ,"--logging.level.root=INFO" );
        }
    }


    
    public static class RecipientListRouterConfig {

        @Bean
        @ServiceActivator(inputChannel = "routingChannel")
        public RecipientListRouter recipientListRouter() {
    
            RecipientListRouter router = new RecipientListRouter();
            router.setSendTimeout(1_234L);
            router.setIgnoreSendFailures(true);
            router.setApplySequence(true);
            router.addRecipient("stringChannel1");
            router.addRecipient("stringChannel2");
            router.addRecipient("stringChannel3");
            return router ;
        }

        @Bean
        @Order(70)
        ApplicationRunner recipientListRoutingRunner(QueueChannel routingChannel) {
    
            return args -> {
                TimeUnit.SECONDS.sleep(5);
                routingChannel.send(new GenericMessage<String>("Recipient List Router Test 1 .")); 
                routingChannel.send(new GenericMessage<String>("Recipient List Router Test 2 ."));
                routingChannel.send(new GenericMessage<String>("Recipient List Router Test 3 ."));
                routingChannel.send(new GenericMessage<String>("Recipient List Router Test 4 ."));
                routingChannel.send(new GenericMessage<String>("Recipient List Router Test 5 ."));
                routingChannel.send(new GenericMessage<String>("Recipient List Router Test 6 ."));
    
            };
        }
    }

    @EnableAutoConfiguration    
    
    public static class App7 {
        public static void main(String[] args) throws Exception {
            new SpringApplicationBuilder()
                    .sources(App1.class,Boots.class,TestStringChannels.class,ChannelConfiguration.class,RecipientListRouterDslConfig.class)
                    .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/integrationApplication/router/")
                    .run("--management.endpoints.web.exposure.include=*","--spring.main.lazy-initialization=false" 
                                ,"--logging.level.root=INFO" );
        }
    }


    
    public static class RecipientListRouterDslConfig {

        @Bean
        public IntegrationFlow recipientListRouterFlow() {
            return IntegrationFlows
                            .from("routingChannel")
                            .routeToRecipients(flow -> 
                                                 flow
                                                    .ignoreSendFailures(true)
                                                    .applySequence(true)
                                                    .sendTimeout(1_234L)
                                                    .recipient("stringChannel1")
                                                    .recipient("stringChannel2")
                                                    .recipient("stringChannel3")
                                                    
                            )
                            .get()
                            ;
        }
    
        @Bean
        @Order(70)
        ApplicationRunner recipientListRoutingRunner(QueueChannel routingChannel) {
    
            return args -> {
                TimeUnit.SECONDS.sleep(5);
                routingChannel.send(new GenericMessage<String>("Recipient List Router Test 1 .")); 
                routingChannel.send(new GenericMessage<String>("Recipient List Router Test 2 ."));
                routingChannel.send(new GenericMessage<String>("Recipient List Router Test 3 ."));
                routingChannel.send(new GenericMessage<String>("Recipient List Router Test 4 ."));
                routingChannel.send(new GenericMessage<String>("Recipient List Router Test 5 ."));
                routingChannel.send(new GenericMessage<String>("Recipient List Router Test 6 ."));
    
            };
        }
    }

    @EnableAutoConfiguration    
    
    public static class App8 {
        public static void main(String[] args) throws Exception {
            new SpringApplicationBuilder()
                    .sources(App1.class,Boots.class,TestStringChannels.class,ChannelConfiguration.class,CustomRouterConfig.class)
                    .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/integrationApplication/router/")
                    .run("--management.endpoints.web.exposure.include=*","--spring.main.lazy-initialization=false" 
                                ,"--logging.level.root=INFO" );
        }
    }

    
    public static class CustomRouterConfig {    

        @Bean
        public QueueChannel customRouterRoutingChannel() {
            return MessageChannels
                            .queue()
                            .datatype(String.class,Long.class)
                            .get();
        }
    
        @Bean
        @Router(inputChannel = "routingChannel")
        public AbstractMessageRouter customRouterX(PublishSubscribeChannel longChannel) {
    
            return new AbstractMessageRouter() {
    
                @Override
                protected Collection<MessageChannel> determineTargetChannels(Message<?> message) {
                    return Collections.singletonList(longChannel);
                }
                
            };
        }
    
        @Bean
        ApplicationRunner customeRouteXRunner(QueueChannel routingChannel) {
            return args -> {
                TimeUnit.SECONDS.sleep(5);
                routingChannel.send(new GenericMessage<Long>(7_896L)); 
            };
    
        }
    }

    @EnableAutoConfiguration    
    
    public static class App9 {
        public static void main(String[] args) throws Exception {
            new SpringApplicationBuilder()
                    .sources(App1.class,Boots.class,TestStringChannels.class,ChannelConfiguration.class,CustomRouterDslConfig.class)
                    .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/integrationApplication/router/")
                    .run("--management.endpoints.web.exposure.include=*","--spring.main.lazy-initialization=false" 
                                ,"--logging.level.root=INFO" );
        }
    }

    
    
    public static class CustomRouterDslConfig {    
    
        @Bean
        public IntegrationFlow customRouterXXFlow(PublishSubscribeChannel longChannel){
            return IntegrationFlows
                        .from("routingChannel")
                        .route(new AbstractMessageRouter() {
    
                            @Override
                            protected Collection<MessageChannel> determineTargetChannels(Message<?> message) {
                                return Collections.singletonList(longChannel);
                            }
                            
                        })
                        .get();
        }
    
        @Bean
        ApplicationRunner customeRouteXXRunner(QueueChannel routingChannel) {
            return args -> {
                TimeUnit.SECONDS.sleep(5);
                routingChannel.send(new GenericMessage<Long>(8_855L)); 
            };
    
        }
    }

    
    @EnableAutoConfiguration    
    
    public static class App10 {
        public static void main(String[] args) throws Exception {
            new SpringApplicationBuilder()
                    .sources(App10.class,Boots.class,TestStringChannels.class,ChannelConfiguration.class,AnnotaionRouterConfig.class)
                    .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/integrationApplication/router/")
                    .run("--management.endpoints.web.exposure.include=*","--spring.main.lazy-initialization=false" 
                                ,"--logging.level.root=INFO" );
        }
    }

    public static class AnnotaionRouterConfig {

        @Autowired
        @Qualifier("stringChannel1")
        PublishSubscribeChannel stringChannel1;

        @Router(inputChannel = "routingChannel")
        public MessageChannel routeFromMessage(Message<?> message) {

            return stringChannel1;
        }

        @Router(inputChannel = "stringRoutingChannel")
        public String routeFromPayload(String payload) {

            log.info("routeFromPayload");
            return "stringChannel2";
        }

        @Router(inputChannel = "stringRoutingChannel")
        public String routeFromPayloadAndHeader(@Header(MessageHeaders.ID) String id,String payload) {
            log.info("routeFromPayloadAndHeader: ID {}",id);
            return "stringChannel2";
        }

        @Bean
        ApplicationRunner routeRunner(QueueChannel routingChannel,QueueChannel stringRoutingChannel) {
            return args -> {
                TimeUnit.MILLISECONDS.sleep(50);
                routingChannel.send(new GenericMessage<String>("routeRunnerMessage")); 

                TimeUnit.MILLISECONDS.sleep(50);
                stringRoutingChannel.send(new GenericMessage<String>("routeRunnerPayload")); 


                TimeUnit.MILLISECONDS.sleep(50);
                stringRoutingChannel.send(new GenericMessage<String>("routeRunnerPayload")); 
            };
    
        }
    }



}
