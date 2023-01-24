package com.example.democloudstreamwebfux.webfluxClient;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;

import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientRequest;
import reactor.test.StepVerifier;


@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT,classes = Application.class)
@Slf4j
public class EventResourceTests {

    @LocalServerPort
    Integer port;

    @Autowired
    private WebTestClient webTestClient;

    private WebClient createWebClientWithConnectAndReadTimeOuts(int connectTimeOut, long readTimeOut) {
        // create reactor netty HTTP client
        HttpClient client = HttpClient.create()
                                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)                                        
                                        .option(ChannelOption.SO_KEEPALIVE, true)
                                        .option(EpollChannelOption.TCP_KEEPIDLE, 300)
                                        .option(EpollChannelOption.TCP_KEEPINTVL, 60)
                                        .option(EpollChannelOption.TCP_KEEPCNT, 8)
                                        .responseTimeout(Duration.ofSeconds(1))
                                        .doOnConnected(conn -> conn
                                                            .addHandler(new ReadTimeoutHandler(10, TimeUnit.SECONDS))
                                                            .addHandler(new WriteTimeoutHandler(10)));
                                        ; 
        // create a client http connector using above http client
        ClientHttpConnector connector = new ReactorClientHttpConnector(client);
        // use this configured http connector to build the web client
        return WebClient.builder().clientConnector(connector).build();
    }
    
    @BeforeEach
    public void beforeEach() {
        assertThat(port).isNotNull()
                        .isPositive();

        webTestClient = webTestClient
                                    .mutate()
                                    .responseTimeout(Duration.ofSeconds(50))
                                    
                                    .baseUrl(format("http://127.0.0.1:%s",port))
                                    .defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                                    .build();
    }


    static final Function<Integer,WebClient> 
                                restClient = port -> 
                                            WebClient
                                                .builder().clientConnector(null)
                                                .baseUrl(format("http://127.0.0.1:%s",port))      
                                                .defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                                                .build();

    @Test
    public void longStreamTest() {
        Flux<Long> longStreamFlux = 
                                webTestClient
                                        .get()
                                        .uri("/longstream")
                                        .accept(MediaType.APPLICATION_NDJSON)
                                        .exchange()
                                        .expectStatus().isOk()
                                        .returnResult(Long.class)
                                        .getResponseBody();

        StepVerifier.create(longStreamFlux)
                                        .expectNext(0L)
                                        .expectNext(1L)
                                        .expectNext(2L)
                                        .thenCancel()
                                        .verify();
    }


    @Test
    public void should_get_event_stream_by_one_client() throws Exception {
        FluxExchangeResult<Event> fluxExchangeResult = webTestClient
                                                            .get()      
                                                            .uri("/events2")
                                                            .accept(TEXT_EVENT_STREAM)
                                                            .exchange()
                                                            .expectStatus().isOk()
                                                            .returnResult(Event.class);

        fluxExchangeResult
                        .getResponseBody()
                        .map(Event::toString)
                        .map(eventDesc -> "ev2: " + eventDesc)
                        .subscribe(log::info);


        // StepVerifier.create(fluxExchangeResult.getResponseBody())
        //                                                     .expectNextCount(4)
        //                                                     // .consumeNextWith(event -> log.info("{}",event))
        //                                                     .thenCancel()
        //                                                     .verify();

        
        TimeUnit.SECONDS.sleep(9);
    }                                                
                
    @Test
    public void should_get_event_stream_by_multiple_clients() throws Exception {
        RequestHeadersSpec<?> spec =  restClient.apply(port)
                                                        .get()
                                                        .uri("/events2")
                                                        .httpRequest(httpRequest -> {
                                                            HttpClientRequest reactorRequest = httpRequest.getNativeRequest();
                                                            reactorRequest.responseTimeout(Duration.ofSeconds(2));
                                                            
                                                        })
                                                        .accept(TEXT_EVENT_STREAM)
                                                        ;

        spec
            .retrieve()
            .onStatus(HttpStatus::isError, clientResponse -> {
                log.error("Error while calling endpoint {} with status code {}", "/events2", clientResponse.statusCode());
                throw new RuntimeException("Error while calling  accounts endpoint");
            })                

            .bodyToFlux(Event.class)   
            // .timeout(Duration.ofSeconds(3)) 
            .map(Event::toString)   
            // detecting the timeout error
            .doOnError(error -> log.error("Error signal detected", error))           
            .subscribe(eventDesc -> log.info("ev: {}",eventDesc));             
            
        WebClient
                .create(format("http://127.0.0.1:%s",port))
                .get()      
                .uri("/events2")
                .httpRequest(httpRequest -> {
                    HttpClientRequest reactorRequest = httpRequest.getNativeRequest();
                    reactorRequest.responseTimeout(Duration.ofSeconds(2));
                    
                })
                .accept(TEXT_EVENT_STREAM)
                .retrieve()
                .onStatus(HttpStatus::isError, clientResponse -> {
                    log.error("Error while calling endpoint {} with status code {}", "/events2", clientResponse.statusCode());
                    throw new RuntimeException("Error while calling  accounts endpoint");
                })                
                .bodyToFlux(Event.class)
                // .timeout(Duration.ofSeconds(3))
                .map(Event::toString)
                .map(eventDesc -> "ev2: " + eventDesc)
                // detecting the timeout error
                .doOnError(error -> log.error("Error signal detected", error))           
                .subscribe(log::info);

        // spec.exchangeToFlux(clientResponse -> clientResponse
        //                                                .bodyToFlux(Event.class))
        //     .map(String::valueOf)
        //     .subscribe(log::info);

        TimeUnit.SECONDS.sleep(9);
    }
    
    @Test
    public void should_get_event_by_id() {
        long randomNumbetr = new Random().nextLong() ;
        long positiveNumber = randomNumbetr < 0 ? -randomNumbetr : randomNumbetr ;

        RequestBodySpec spec = restClient.apply(port)
                                                    .method(GET)
                                                    // .get()
                                                    .uri(format("/events2/%d",positiveNumber));

        Map response1  = spec.exchangeToMono(clientResponse -> clientResponse.bodyToMono(Map.class))
                              .block();

        Event response2 = spec.exchangeToMono(clientResponse -> clientResponse.bodyToMono(Event.class))
                                .block();


        assertThat(response1.get("id")).isNotNull() ;
        assertThat(response2.getId()).isNotNull();
        assertThat(response1.get("id")).isEqualTo(response2.getId())
                                        .isEqualTo(positiveNumber);

        assertThat(response1.get("when")).isNotNull();
        assertThat(response2.getWhen()).isNotNull();
        assertThat(response1.get("when")).isNotEqualTo(response2.getWhen());
        assertThat(response2.getWhen()).isBeforeOrEqualTo(ZonedDateTime.now());

        log.info("resp 1: {}",response1);
        log.info("resp 2: {}",response2);
    }

    @Test
    public void should_create_webClient_and_get_event_by_id() {

        long randomNumbetr = new Random().nextLong() ;
        long positiveNumber = randomNumbetr < 0 ? -randomNumbetr : randomNumbetr ;

        WebClient webClient = WebClient.builder() 
                                        .baseUrl(format("http://127.0.0.1:%s/events2/%d",port,positiveNumber))
                                        .build();



        Map response1  = webClient
                                .get()
                                .exchangeToMono(clientResponse -> clientResponse.bodyToMono(Map.class))
                                .block();

        Event response2 = webClient
                                .get()
                                .exchangeToMono(clientResponse -> clientResponse.bodyToMono(Event.class))
                                .block();


        assertThat(response1.get("id")).isNotNull() ;
        assertThat(response2.getId()).isNotNull();
        assertThat(response1.get("id")).isEqualTo(response2.getId())
                                        .isEqualTo(positiveNumber);

        assertThat(response1.get("when")).isNotNull();
        assertThat(response2.getWhen()).isNotNull();
        assertThat(response1.get("when")).isNotEqualTo(response2.getWhen());
        assertThat(response2.getWhen()).isBeforeOrEqualTo(ZonedDateTime.now());

        log.info("resp 1: {}",response1);
        log.info("resp 2: {}",response2);
    }

}
