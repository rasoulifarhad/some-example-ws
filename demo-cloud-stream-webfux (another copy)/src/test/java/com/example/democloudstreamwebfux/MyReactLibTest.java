package com.example.democloudstreamwebfux;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
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
}
