package com.example.democloudstreamwebfux.intergration.tcp.mid;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.dsl.Transformers;
import org.springframework.integration.http.config.EnableIntegrationGraphController;
import org.springframework.integration.ip.dsl.Tcp;
import org.springframework.integration.ip.tcp.serializer.TcpCodecs;
import org.springframework.messaging.support.GenericMessage;

import com.example.democloudstreamwebfux.intergration.ChannelConfiguration;
import com.example.democloudstreamwebfux.intergration.ChannelConfiguration.TestStringChannels;

import lombok.extern.slf4j.Slf4j;

public class Application {
    
    @EnableAutoConfiguration
    @Configuration
    @IntegrationComponentScan
    @EnableIntegrationGraphController
    @Slf4j
    public static class IntegrationApplication {
        public static void main(String[] args) throws Exception {

            log.info("--");
            new SpringApplicationBuilder()
                      .sources(IntegrationApplication.class,
                      IntegrationConfig.class
                                          )
                      .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/intergration/tcp/mid/")
                      .run("--management.endpoints.web.exposure.include=*","--spring.main.lazy-initialization=false" 
                                  ,"--logging.level.root=INFO" 
                                  ,"--logging.level.org.springframework.retry.support.RetryTemplate=DEBUG"
                                  ,"--spring.cloud.stream.defaultBinder=rabbit");
        }
    }

    @Configuration
    @Slf4j
    public static class IntegrationConfig {

        @Bean(name = "tcpInbound")
        public DirectChannel tcpInbound() {
            return MessageChannels.direct().get();
        }

        @Bean(name = "tcpIn.errorChannel")
        public DirectChannel tcpInErrorChannel() {
            return MessageChannels.direct().get() ;
        }

        @Bean
        public IntegrationFlow server() {

            return IntegrationFlows.from(Tcp.inboundGateway(Tcp.netServer(1234)
                                                               .deserializer(TcpCodecs.lengthHeader1())
                                                               .serializer(TcpCodecs.lengthHeader1())
                                                               .backlog(30))
                                            .errorChannel("tcpIn.errorChannel")
                                            .id("tcpIn"))
                                   .transform(Transformers.objectToString())                   
                                   .channel("tcpInbound")
                                   .<String,String>transform(String::toUpperCase)
                                   .logAndReply()
                                //    .get()
                                   ;
        }

        @Bean
        public IntegrationFlow client() {

            return flow -> flow.handle(Tcp.outboundGateway(Tcp.netClient("localhost", 1234)
                                                            .deserializer(TcpCodecs.lengthHeader1())
                                                            .serializer(TcpCodecs.lengthHeader1())));
        }

        @Bean
        public MessagingTemplate messagingTemplate() {
            return new MessagingTemplate() ;
        }
        
        @Bean
        @Order(30)
        public ApplicationRunner runner(MessagingTemplate messagingTemplate) {
            return args -> {

                log.info("--");
                log.info("Response:{} ",messagingTemplate.sendAndReceive("client.input",new GenericMessage<String>("Hiiiiiiiiiiiiiiiiiii") ));
            };
        }
    }
}
