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
public class BarCreatedEventProcessor implements ApplicationListener<BarCreated>
                                                 , Consumer<FluxSink<BarCreated>> {

    private final Executor executor ;                                                    
    private final BlockingQueue<BarCreated> queue = new LinkedBlockingQueue<>();


    
    public BarCreatedEventProcessor(@Qualifier("applicationTaskExecutor") Executor executor) {
        this.executor = executor;
    }

    @Override
    public void onApplicationEvent(BarCreated event) {
        queue.offer(event);        
    }

    @Override
    public void accept(FluxSink<BarCreated> sink) {
        
        executor.execute(() -> {

            while(true) {
                try {
                    BarCreated barCreated = queue.take() ;
                    sink.next(barCreated);
                        
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

                                                    
    
}
