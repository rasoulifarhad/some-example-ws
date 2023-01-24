package com.example.democloudstreamwebfux.events;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import reactor.core.publisher.FluxSink;

@Component
public class FooCreatedEventProcessor   implements ApplicationListener<FooCreated>,Consumer<FluxSink<FooCreated>> {


    private final Executor executor ;
    private final BlockingQueue<FooCreated> queue = new LinkedBlockingQueue<>();

    

    public FooCreatedEventProcessor(@Qualifier("applicationTaskExecutor") Executor executor) {
        this.executor = executor;
    }

    @Override
    public void onApplicationEvent(FooCreated event) {
        queue.offer(event)        ;
    }

    @Override
    public void accept(FluxSink<FooCreated> sink) {
        this.executor.execute(() -> {
            while(true) {
                try {
                    FooCreated fooCreated =  queue.take();
                    sink.next(fooCreated);
                    
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        } );
        

    }

    
}
