package com.example.democloudstreamwebfux.broadcaster;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Configuration
@Slf4j
public class BroadcasterConfig {

    //But if you don't really need to expose the Processor to clients of your library, it is fine to use a sink, expose its asFlux()/asMono() 
    //view to clients and automatically feed it by bridging with .subscribe(v -> {...}, e -> {...}, () -> {...}) (the lambda-based subscribe).

    //If the same sink is only used for a single subscription, then tryEmitXxx API can be used in a simplified manner since we're within RS 
    //spec serialization guarantees.

    //More specifically, for Sinks.Many<T>:
    //
    // upstream.subscribe(
    // //you could use `sink::tryEmitNext` below, but it can return FAIL_CANCELLED
    // //emitNext has the benefit of discarding the `v` in that case, but with `tryEmitNext`
    // //you could check the result and explicitly dispose the value if that is relevant
    // v -> sink.emitNext(v, FAIL_FAST), 
    // e -> sink.tryEmitError,
    // //ok not to check return value below, only because we're inside `subscribe`.
    // //consider making that fact explicit in your codebase with a comment
    // sink::tryEmitComplete 
    // );
    
    // For Sinks.One<T>:

    // upstream.subscribe(
    //   //you could use `sink::tryEmitValue` below, but it can return FAIL_CANCELLED
    //   //emitValue has the benefit of discarding the `v` in that case, but with `tryEmitValue`
    //   //you could check the result and explicitly dispose the value if that is relevant
    //   v -> sink.emitValue(v, FAIL_FAST), 
    //   e -> sink.tryEmitError,
    //   //ok not to check return value below, only because we're inside `subscribe`.
    //   //even if we triggered `emitValue` above (which implies an onComplete), this
    //   //would just return `EmitResult.TERMINATED` which can also be ignored here.
    //   //consider making that fact explicit in your codebase with a comment though
    //   sink::tryEmitEmpty 
    // );
    
    // And for Sinks.Empty<Void>:
    
    // upstream.subscribe(
    //   //that one is tricky. `Sinks.Empty` cannot deal with a value, so be careful
    //   //not to subscribe to something that can produce `onNext`. Typically a `Publisher<Void>`.
    //   v -> {}, //consider discarding any unexpected `v` here if relevant 
    //   e -> sink.tryEmitError,
    //   //ok not to check return value below, only because we're inside `subscribe`
    //   //and the source is a Publisher<Void>: no onNext, RS guarantees no competing signals
    //   //like onError, and even FAIL_CANCELLED can be ignored since we're not dealing with any value...
    //   //consider making that fact explicit in your codebase with a comment though
    //   sink::tryEmitEmpty 
    // );

    // Instead, given all the above, it would be beneficial to have standalone sink flavors that users can directly instantiate. It's 
    // not even clear that we'd need many flavors:

    // manually emit values to at most one Subscriber => introduce UnicastFluxSink, UnicastMonoSink
    // manually emit values to several Subscribers => unicastSink.share()
    // manually emit values to several Subscribers, replaying stuff to late subscribers => unicastSink.replay(n)
    //
    //With a standalone sink, the first signal is like connect() => up to user to NOT call the sink until everything is properly 
    // subscribed.
    //That said UnicastXxxSink without a subscriber could also DROP, and we could use OnBackpressureStrategy or onBackpressure 
    //operators to tune that...

    //serialized EmitterProcessor.create()
    // Sink<String> test = Sinks.multicast()
    // .onBackpressureBuffer();

    // //non serialized DirectProcessor.create() (no need for serialization anyway)
    // Sink<Void> coordinator = Sinks.coordinator(); 

    // //non serialized MonoProcessor.create() (no need for serialization anyway)
    // Sink<T> promise = Sinks.promise();

    // //non serialized MonoProcessor.create() (no need for serialization anyway)
    // MonoProcessor<T> promise = Sinks.unsafe()
    //                 .monoProcessor()
    //                 .promise();

    // //serialized ReplayProcessor.create(timeout)
    // Sink<String> test2 = Sinks.replay()
    // .limit(Duration.ofMillis(100));

    // //non serialized ReplayProcessor.create(timeout)
    // Sink<String> test3 = Sinks.unsafe()
    //             .replay()
    // .limit(Duration.ofMillis(100));

    // //non serialized UnicastProcessor.create() as processor - honestly I don't like having thi
    // FluxProcessor<String, String> test4 = Sinks.unsafe()
    //                             .fluxProcessor()
    //                             .unicast()
    //         .onBackpressureBuffer();

    @Bean
    public Sinks.Many<BroadcastMessage> broadcasterSink() {
        // final Sinks.Many<Message<?>> sink = Sinks.many().multicast().onBackpressureBuffer(1, false);

        return  Sinks.many().multicast().onBackpressureBuffer(1,false)
                                                        // .onBackpressureBuffer(1, false)
                                                        ;
    }

    @Bean("broadcastLPublisher")
    public Flux<BroadcastMessage> broadcastLPublisher(Scheduler singScheduler ,Sinks.Many<BroadcastMessage> broadcasterSink) {

        return broadcasterSink.asFlux()
                                        .publishOn(singScheduler)
                                        .subscribeOn(singScheduler)
                                        // .onBackpressureBuffer()
                                        // .onBackpressureBuffer(1, false)
                                        // .share()
                                        ;
                                   
    }

    @Bean("broadcastListener")
    public Consumer<BroadcastMessage>  broadcastListener(Collection<BroadcastMessage> db,Flux<BroadcastMessage> broadcastLPublisher) {

        final AtomicLong id = new AtomicLong(0);

        return broadcasterMessage -> broadcastLPublisher
                                        .map(bm -> bm.setId(id.getAndIncrement()) )
                                        .log()
                                        .doOnNext(msg -> log.info("===============>>>>>>>>> {} ",msg))
                                        .log()
                                        .doOnCancel(() -> log.info("canceled"))
                                        .doOnComplete(() -> log.info("complate"))
                                        .doOnError(t ->  log.info("eror: {}",t))
                                        .doOnSubscribe(s -> log.info("subscribtion: {}",s))
                                        .doOnTerminate(() -> log.info("doOnTerminate"))
                                        .subscribe(db::add);

    }


    @Component
    public static class BroadcastEventListener implements  Consumer<Flux<BroadcastMessage>> {

        final AtomicLong id = new AtomicLong(0);

        AtomicLong count = new AtomicLong(0);

        @Autowired 
        private Collection<BroadcastMessage>  anotherDb;


        @Autowired 
        private Flux<BroadcastMessage> broadcastLPublisher;

        @PostConstruct
        public void init()  {
            accept(broadcastLPublisher);
        }

        @Override
        public void accept(Flux<BroadcastMessage> flux) {
            log.info("accept # of invocation {}",count.incrementAndGet());
            flux
                .map(bm -> bm.setId(id.getAndIncrement()) )
                .log()
                .doOnNext(msg -> log.info("anothe===============>>>>>>>>> {} ",msg))
                .log()
                .subscribe(anotherDb::add);

            
        }
        
        
    }

    @Bean
    public Scheduler singScheduler() {
        return Schedulers.single() ;
    }
    
    @Bean
    public Collection<BroadcastMessage>  db() {
        return new CopyOnWriteArrayList<>();
    }

    @Bean
    public Collection<BroadcastMessage>  anotherDb() {
        return new CopyOnWriteArrayList<>();
    }
    
}
