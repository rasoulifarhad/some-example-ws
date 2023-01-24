package com.example.democloudstreamwebfux;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmitResult;

@Slf4j
public class ReactorsTests {
    

    private WebClient webclient ;
    @BeforeEach
    public void setupWebClient() {

        WebClient.Builder builder = WebClient.builder();
        webclient =  builder
                                    .baseUrl("https://www.google.com")
                                    .build();
    }

    @Test
    public void test() throws InterruptedException{

        long startTime = System.currentTimeMillis();

        Mono<Long> longMono =  Mono.just(System.currentTimeMillis() - startTime);
        
        Mono<Long> deferedLongMono = Mono.defer(() -> Mono.just(System.currentTimeMillis() - startTime));

        TimeUnit.SECONDS.sleep(5);

        longMono.subscribe(t -> log.info("{}",t));

        deferedLongMono.subscribe(t -> log.info("{}",t));
    }

    @Test
    public void sinksReplyTest() throws InterruptedException {

        // final CountDownLatch count = new CountDownLatch(5);
        
        Flux<Boolean> monoC = Sinks.many()
                                    .replay()
                                    .latestOrDefault(true)
                                    .asFlux()
                                    .doOnNext(bool -> log.info("{} - {}   emiting next {} ",new Date() , Thread.currentThread().getName(),bool));
        for (int i = 0; i < 5; i++) {
            new Thread( () -> {
                monoC.flatMap( unused -> 
                      webclient
                            .get()
                            .uri("https://www.google.com")
                            .retrieve()
                            .toEntityFlux(String.class)
                            .doOnSuccess(stringResponseEntity -> log.info("{}  -  {}  finished processing",new Date() ,Thread.currentThread().getName()) )
                            .doOnError(t ->  log.info("{}  -  {}  error in  processing",new Date() ,Thread.currentThread().getName()) )
                    )   
                    // .doOnTerminate(() -> count.countDown())                   
                    .subscribe();
            }).start();
        } 
        
        // count.await();
        TimeUnit.SECONDS.sleep(5);
    }


    @Test
    public void sinksReplyTest2() throws InterruptedException {

        // final CountDownLatch count = new CountDownLatch(5);
        Sinks.Many<Integer> sinks =  Sinks.unsafe().many().replay().latest();     
                                
        sinks.asFlux()
                    .doOnNext(it ->  log.info("{} is emiting  {} " , Thread.currentThread().getName(),it))
                    .flatMap(
                        counter -> {
                            return webclient
                                .get()
                                .uri("https://www.google.com")
                                .retrieve()
                                .toEntityFlux(String.class)
                                .doOnSuccess(stringResponseEntity -> {
                                        log.info("{} finished processing {} with status {}",Thread.currentThread().getName(),counter,stringResponseEntity.getStatusCode());
                                })
                                .then(Mono.just(counter));

             //concurrency = 1 causes the flatMap being handled only once in parallel            
            },1)
            .doOnError(Throwable::printStackTrace)
            //this subscription also must be done in @PostConstruct
            .subscribe(counter -> log.info("{} completed  {}",  Thread.currentThread().getName(),counter));
            
            
        //and this is your endpoint method
        for (int i = 0; i < 5; i++) {
            int counter = i ;
            new Thread( () -> {
                    
                EmitResult emitResult = sinks.tryEmitNext(counter);

                if (emitResult.isFailure()) {
                       //mb in that case you should retry
                    log.info("{} emitted {}. with fail: {}", Thread.currentThread().getName(), counter, emitResult);
                } else {
                    log.info("{} successfully emitted {}", Thread.currentThread().getName(), counter);
                }

            }).start();
        } 
        
        // count.await();
        TimeUnit.SECONDS.sleep(20);
    }

}
