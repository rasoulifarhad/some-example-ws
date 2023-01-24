package com.example.democloudstreamwebfux.intergration.channeladapter;

import java.util.concurrent.TimeUnit;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.Publisher;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.config.EnablePublisher;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;

import com.example.democloudstreamwebfux.intergration.ChannelConfiguration;
import com.example.democloudstreamwebfux.intergration.ChannelConfiguration.TestStringChannels;
import com.example.democloudstreamwebfux.intergration.StringPayload;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * A channel adapter is a message endpoint that enables connecting a single sender or receiver to a message channel.
 * 
 * An inbound-channel-adapter element (a SourcePollingChannelAdapter in Java configuration) can invoke any method on a Spring-managed 
 * object and send a non-null return value to a MessageChannel after converting the method’s output to a Message.
 * 
 * An outbound-channel-adapter element (a @ServiceActivator for Java configuration) can also connect a MessageChannel to any POJO 
 * consumer method that should be invoked with the payload of messages sent to that channel. 
 */

@Slf4j
public class ChannelAdapterApplication {
    

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
    @Slf4j
    public static class InboundChannelAdapterDsl1Application {
        public static void main(String[] args) throws Exception {
            new SpringApplicationBuilder()
                    .sources(InboundChannelAdapterDsl1Application.class,TestStringChannels.class,ChannelConfiguration.class,InboundChannelAdapterDsl1Config.class)
                    .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/integrationApplication/router/")
                    .run("--management.endpoints.web.exposure.include=*","--spring.main.lazy-initialization=false" 
                                ,"--logging.level.root=INFO" );
        }
    }

    @Slf4j
    public static class InboundChannelAdapterDsl1Config {

        @Bean
        public IntegrationFlow pollingSourceDsl(PublishSubscribeChannel stringChannel) {
             return IntegrationFlows
                            .from(() -> new GenericMessage<>("polling Source Dsl"),
                                    e -> e.poller(p -> p.fixedRate(5000)) )
                            .channel(stringChannel)
                            // .channel("stringChannel")
                            .get();
        }
    }


    @EnableAutoConfiguration    
    @Slf4j
    public static class OutboundChannelAdapterDsl1Application {
        public static void main(String[] args) throws Exception {
            new SpringApplicationBuilder()
                    .sources(OutboundChannelAdapterDsl1Application.class,TestStringChannels.class,ChannelConfiguration.class,OutboundChannelAdapterDsl1Config.class)
                    .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/integrationApplication/router/")
                    .run("--management.endpoints.web.exposure.include=*","--spring.main.lazy-initialization=false" 
                                ,"--logging.level.root=INFO" );
        }
    }

    @Slf4j
    public static class OutboundChannelAdapterDsl1Config {

        @Slf4j
        public static class MyMessageHandler{

            public void handle(String payload) {
                log.info("MyMessageHandler: handled -> {}",payload);
            }
        }

        public static class AnotherMyMessageHandler {

            @ServiceActivator(inputChannel = "routingChannel" ,poller = @Poller(fixedRate = "10000"))
            public void handle(Object payload) {
                log.info("AnotherMyMessageHandler: handled -> {}",payload);

            }
        }

        @Bean
        public MyMessageHandler myMessageHandler() {
            return new MyMessageHandler();
        }

        @Bean
        public IntegrationFlow outboundChannelAdapterFlow(MyMessageHandler myMessageHandler,PublishSubscribeChannel stringChannel) {
            return  f -> f
                        // .channel(routingChannel)
                        .channel(stringChannel)
                        .handle(myMessageHandler,"handle");
        }

        @Bean
        public AnotherMyMessageHandler anotherMyMessageHandler()   {
            return new AnotherMyMessageHandler();
        }


        @Bean
        public IntegrationFlow outboundChannelAdapterFlow2(AnotherMyMessageHandler anotherMyMessageHandler,QueueChannel routingChannel) {
            return  f -> f
                        // .channel(routingChannel)
                        .handle(anotherMyMessageHandler,"handle");
        }

        @Bean
        @Order(20)
        public ApplicationRunner outboundChannelAdapterFlow1Runner(QueueChannel routingChannel,PublishSubscribeChannel stringChannel) {

            return args -> {
                stringChannel.send(new GenericMessage<String>("Hello_boy"));

                TimeUnit.SECONDS.sleep(20);

                routingChannel.send(new GenericMessage<String>("Another_Hello_boy"));




            };

        }
    }

    @EnableAutoConfiguration  
    @EnablePublisher  
    @Slf4j
    public static class MessagePublishingApplication {
        public static void main(String[] args) throws Exception {
            new SpringApplicationBuilder()
                    .sources(MessagePublishingApplication.class,TestStringChannels.class,ChannelConfiguration.class,MessagePublisher.class,MessagePublishingConfig.class)
                    .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/integrationApplication/router/")
                    .run("--management.endpoints.web.exposure.include=*","--spring.main.lazy-initialization=false" 
                                ,"--logging.level.root=INFO" );
        }
    }

    @Component
    @Slf4j
    public static class MessagePublisher {

        @Publisher(channel = "stringChannel")
        public String argumentAsPayload(@Payload String fname, @Header String lname) {
            return fname + " " + lname;
        }

        @Publisher(channel = "stringChannel")
        public String defaultPayload(String fname , String lname) {
            return fname + " " + lname;
        }

        @Publisher(channel = "stringChannel")
        public String defaultPayload2(String fname ,@Header("last") String lname,@Header String lname2) {
            return fname + " " + lname;
        }

        @Publisher(channel = "stringChannel")
        @Payload
        public String defaultPayloadButExplicitAnnotation(String fname ,@Header("last") String lname,@Header String lname2) {
            return fname + " " + lname;
        }

        @Publisher(channel = "stringChannel")
        @Payload(expression = "#return + #args.lname")
        public String setName(String fname , String lname,@Header String lname2) {
            return fname + " " + lname;
        }
    }

    @Slf4j
    public static class MessagePublishingConfig {

        @Bean
        @Order(20)
        public ApplicationRunner MessagePublishingRunner(MessagePublisher messagePublisher) {
            return args -> {

                messagePublisher.argumentAsPayload("farhad", "rasouli");
                messagePublisher.defaultPayload("farhad", "rasouli");
                messagePublisher.defaultPayload2("farhad", "rasouli","Amiri");
                messagePublisher.defaultPayloadButExplicitAnnotation("farhad", "rasouli","Amiri");
                messagePublisher.setName("farhad", "rasouli","Amiri");

            };
        }


    }


    @EnableAutoConfiguration  
    @EnablePublisher  
    @Slf4j
    public static class MessageTransformationApplication {
        public static void main(String[] args) throws Exception {
            new SpringApplicationBuilder()
                    .sources(MessageTransformationApplication.class,TestStringChannels.class,ChannelConfiguration.class,MessageTransformationConfig.class)
                    .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/integrationApplication/router/")
                    .run("--management.endpoints.web.exposure.include=*","--spring.main.lazy-initialization=false" 
                                ,"--logging.level.root=INFO" );
        }
    }

    @Slf4j
    public static class MessageTransformationConfig {

        @Transformer(inputChannel = "routingChannel",outputChannel = "stringChannel")
        public StringPayload generateStringPayload(String value) {
            return StringPayload.of(value);
        }

        @Bean
        @Order(20)
        public ApplicationRunner MessageTransformationRunner(QueueChannel routingChannel,PublishSubscribeChannel stringChannel) {
            return args -> {
                routingChannel.send(new GenericMessage<String>("Convert_To_Payload"));
            };
        }

    }

}
