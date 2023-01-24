package com.example.demowebfluxmongodbcrud;

import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.ChannelOption;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.ReadTimeoutHandler;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

@Configuration
public class WebClientConfig {
    
    @Bean
    public WebClient getWebClientBuilder() throws SSLException {

        SslContext sslContext = SslContextBuilder
                                        .forClient()
                                        .trustManager(InsecureTrustManagerFactory.INSTANCE)
                                        .build();


        HttpClient httpClient = 
                        HttpClient
                            .create()
                            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 120 * 1000)
                            .doOnConnected(connection -> 
                                      connection.addHandlerLast(new ReadTimeoutHandler(120 * 1000,TimeUnit.MILLISECONDS)))
                            .wiretap("reactor.netty.http.client.HttpClient",LogLevel.DEBUG,AdvancedByteBufFormat.TEXTUAL)        
                            .followRedirect(true)
                            .secure(sslContextSpec -> sslContextSpec.sslContext(sslContext) )
                            ;

        return WebClient
                    .builder()
                    .clientConnector(new ReactorClientHttpConnector(httpClient))
                    .build();
    }

    
}
