package com.example.democloudstreamwebfux.intergration.tcp.annotation;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegrationManagement;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.http.config.EnableIntegrationGraphController;
import org.springframework.integration.ip.tcp.TcpInboundGateway;
import org.springframework.integration.ip.tcp.TcpOutboundGateway;
import org.springframework.integration.ip.tcp.connection.AbstractClientConnectionFactory;
import org.springframework.integration.ip.tcp.connection.AbstractServerConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpNetClientConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpNetServerConnectionFactory;
import org.springframework.messaging.MessageHandler;

import com.example.democloudstreamwebfux.intergration.tcp.annotation.Application.IntegrationConfig.Echo;

import lombok.extern.slf4j.Slf4j;

/**
 *  @EnableIntegration: Standard Spring Integration annotation enabling the infrastructure for an integration application.
 *  @IntegrationComponentScan: Searches for @MessagingGateway interfaces.
 *  @MessagingGateway(defaultRequestChannel="toTcp"): The entry point to the client-side of the flow. The calling application can use @Autowired for this Gateway bean and invoke its method.
 *  @ServiceActivator(inputChannel="toTcp"): Outbound endpoints consist of a MessageHandler and a consumer that wraps it. In this scenario, the @ServiceActivator configures the endpoint, according to the channel type.
 *  @Bean: Inbound endpoints (in the TCP/UDP module) are all message-driven and so only need to be declared as simple @Bean instances.
 *  public static class Echo {..: This class provides a number of POJO methods for use in this sample flow (a @Transformer and @ServiceActivator on the server side and a @Transformer on the client side).
 *  clientCF: The client-side connection factory.
 *  serverCF: The server-side connection factory.
 * 
 */
public class Application {
    
    // @EnableIntegration
    @EnableAutoConfiguration
    @Configuration
    @IntegrationComponentScan
    @EnableIntegrationGraphController
    @EnableIntegrationManagement
    @Slf4j
    public static class IntegrationApplication {
        public static void main(String[] args) throws Exception {

            log.info("--");
            new SpringApplicationBuilder()
                      .sources(IntegrationApplication.class,
                      IntegrationConfig.class,Echo.class
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

        @Bean(name = "resultToString")
        public DirectChannel resultToString() {
            return MessageChannels.direct().get() ;
        }

        @Bean(name = "fromTcp")
        public DirectChannel fromTcp() {
            return MessageChannels.direct().get() ;
        }

        @Bean(name = "toTcp")
        public DirectChannel toTcp() {
            return MessageChannels.direct().get() ;
        }

        @Bean
        public AbstractClientConnectionFactory clientCF() {
            return new TcpNetClientConnectionFactory("localhost", 1234);
        }

        @Bean
        public AbstractServerConnectionFactory serverCF() {
            return new TcpNetServerConnectionFactory(1234);
        }
        
        @MessagingGateway(name = "viaTcpGateway",defaultRequestChannel = "toTcp")
        public interface Gateway {

            String viaTcp(String in);
        }

        @MessageEndpoint
        public static class Echo {

            @Transformer(inputChannel = "fromTcp" , outputChannel = "toEcho")
            public String convert(byte [] bytes) {

                return new String(bytes);

            }

            @ServiceActivator(inputChannel = "toEcho")
            public String upCase(String in) {

                return in.toUpperCase();

            }

            @Transformer(inputChannel = "resultToString ")
            public String convertResult(byte [] bytes) {
                return new String(bytes);

            }
        }

        @Bean
        @ServiceActivator(inputChannel = "toTcp")
        public MessageHandler tcpOutGate(AbstractClientConnectionFactory clientCF) {

            TcpOutboundGateway outGate = new TcpOutboundGateway() ;
            outGate.setConnectionFactory(clientCF);
            outGate.setOutputChannelName("resultToString");
            return outGate ;

        }

        @Bean
        public TcpInboundGateway tcpInGate(AbstractServerConnectionFactory serverCF ,DirectChannel fromTcp) {

            TcpInboundGateway inGate = new TcpInboundGateway();
            inGate.setConnectionFactory(serverCF);
            inGate.setRequestChannel(fromTcp);
            return inGate;

        }

        @Bean
        public ApplicationRunner runner(Gateway viaTcpGateway) {

            return args -> {

                log.info("response from via tcp gateway: {}", viaTcpGateway.viaTcp("hi via Tcp Gateway!"));
            };

        }
    }
}
