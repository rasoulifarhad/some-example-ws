package com.example.democloudstreamwebfux;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmitFailureHandler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

public class MyReactLibTest {
    
    @Test
	public void testAlphabet4LimitToz() {
		MyReactLib lib = new MyReactLib() ;
		// MyReactLib libLog = new MyReactLib() ;
		// libLog.alphabet5('x').subscribe(t -> System.out.println(t) );
		StepVerifier
					.create(lib.alphabet5('x'))
					.expectNext("x","y" , "z")
					.expectComplete()
					.verify();
	}

	@Test
	public void testCorrectAlphabet5() {
		MyReactLib lib = new MyReactLib() ;
		// MyReactLib libLog = new MyReactLib() ;
		// libLog.alphabet5('x').subscribe(t -> System.out.println(t) );
		StepVerifier
					.create(lib.correctAlphabet5('x'))
					.expectNext("x","y" , "z")
					.expectComplete()
					.verify();
	}


    @Test
    public void testWithDelay() {

        MyReactLib lib = new MyReactLib() ;

        Duration testDuration = 
                                StepVerifier
                                            .create(lib.withDelay("foo", 30))
                                            .expectSubscription()
                                            .thenAwait(Duration.ofSeconds(10))
                                            .expectNoEvent(Duration.ofSeconds(10))
                                            .thenAwait(Duration.ofSeconds(10))
                                            .expectNext("foo")
                                            .expectComplete()
                                            .verify()
                                            ;
                                            
        System.out.println(testDuration.toMillis() + "ms");                                            
    }


    @Test
    public void testWithDelay_withVirtualTime() {

        MyReactLib lib = new MyReactLib() ;

        Duration testDuration = 
                                StepVerifier
                                            .withVirtualTime(() -> lib.withDelay("foo", 30))
                                            .expectSubscription()
                                            .thenAwait(Duration.ofSeconds(10))
                                            .expectNoEvent(Duration.ofSeconds(10))
                                            .thenAwait(Duration.ofSeconds(10))
                                            .expectNext("foo")
                                            .expectComplete()
                                            .verify()
                                            ;

        System.out.println(testDuration.toMillis() + "ms");                                            
                                            
    }

    @Test
    public void customHotSource() throws InterruptedException {

        SomeFeed<PriceTick> feed = new SomeFeed<>() ;

        Flux<PriceTick> flux = 
                            Flux.create(emitter -> {
                                SomeListener listener = new SomeListener() {

                                    @Override
                                    public void error(Throwable throwable) {

                                        emitter.error(throwable);
                                        
                                    }

                                    @Override
                                    public void priceTick(PriceTick event) {
                                        
                                        emitter.next(event);
                                        if (event.isLast())
                                            emitter.complete();
                                    }
                                    
                                    
                                };
                                feed.register(listener);
                            } , FluxSink.OverflowStrategy.BUFFER);

        ConnectableFlux<PriceTick>  hot = flux.publish() ;

        hot.subscribe(priceTick -> System.out.printf("%s %4s %6.2f%n", priceTick
        .getDate(), priceTick.getInstrument(), priceTick.getPrice()));

        hot.subscribe(priceTick -> System.out.println(priceTick.getInstrument()) );

        hot.connect();

        Thread.sleep(5000);


    }

    @Test
    public void sinksManyTest1() throws InterruptedException {

        Sinks.Many<Integer>  intSink = Sinks.many().replay().all() ;
 
        Runnable r = () ->  {
            intSink.emitNext(1, EmitFailureHandler.FAIL_FAST);
            intSink.emitNext(2, EmitFailureHandler.FAIL_FAST);
            intSink.emitNext(3, EmitFailureHandler.FAIL_FAST);
            intSink.emitComplete(EmitFailureHandler.FAIL_FAST);
        };
        new Thread(r).start(); ;
                                    
        Flux<Integer> fluxView = intSink.asFlux() ;
        fluxView
                .takeWhile(i -> i <  10 )
                .log()
                .subscribe(t -> System.out.println(t));
        // Integer last = fluxView
        //                     .takeWhile(i -> i < 10 )
        //                     .log()
        //                     .blockLast();
        // System.out.println(last);       
        TimeUnit.SECONDS.sleep(1);
    }

    @Test
    public void sinksManyTestMulticast() throws InterruptedException {

        Sinks.Many<Integer>  intSink = Sinks.many().multicast().onBackpressureBuffer();
 
        Runnable r = () ->  {
            intSink.emitNext(1, EmitFailureHandler.FAIL_FAST);
            intSink.emitNext(2, EmitFailureHandler.FAIL_FAST);
            intSink.emitNext(3, EmitFailureHandler.FAIL_FAST);
            intSink.emitComplete(EmitFailureHandler.FAIL_FAST);
        };
        new Thread(r).start(); ;
                                    
        Flux<Integer> fluxView = intSink.asFlux() ;
        fluxView
                .takeWhile(i -> i <  10 )
                .log()
                .subscribe(t -> System.out.println(t));
        // Integer last = fluxView
        //                     .takeWhile(i -> i < 10 )
        //                     .log()
        //                     .blockLast();
        // System.out.println(last);       
        TimeUnit.SECONDS.sleep(1);
    }

    @Test
    public void fluxReduceTest() throws InterruptedException, ExecutionException {

        int min = 50 ;
        int max = 250 ;

        long randomNumber1 = (min + (long) (Math.random() * ((max - min))));

        long randomNumber2 = (min + (long) (Math.random() * ((max - min))));

        CompletableFuture<Long> firstFuture = CompletableFuture.supplyAsync(() -> {
                                                                                try {
                                                                                    System.out.println("firstFuture latency : " + randomNumber1);
                                                                                    Thread.sleep(randomNumber1);
                                                                                } catch (InterruptedException e) {
                                                                                    throw new RuntimeException(e);
                                                                                }
                                                                                return 1L;
                                                                            });

        //
        CompletableFuture<Long> secondFuture = CompletableFuture.supplyAsync(() -> {
                                                                                try {
                                                                                    
                                                                                    System.out.println("secondFuture latency : " + randomNumber2);
                                                                                    Thread.sleep(randomNumber2);
                                                                                } catch (InterruptedException e) {
                                                                                    throw new RuntimeException(e);
                                                                                }
                                                                                return 5L;
                                                                            });

        CompletableFuture<Object> reultFuture =  CompletableFuture.anyOf(firstFuture,secondFuture);
        System.out.println(reultFuture.get());                                                                            


        Flux.fromIterable(Arrays.asList(1,2,3))
                                                .parallel().runOn(Schedulers.parallel())
                                                .filter(i -> i % 2 == 0 )
                                                .map(i -> i * 2)
                                                .reduce((t1, t2) -> t1 * t2  )
                                                .subscribe(System.out::println);
    }
}
