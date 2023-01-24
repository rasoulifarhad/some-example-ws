package com.example.democloudstreamwebfux.publisherDemo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PublisherDemo {

    public static void main(String[] args) {

        Publisher<Integer> naivePublisher =  createNaivePublisher(10);
        naivePublisher.subscribe(createSubscriber());
        
    }

    static Publisher<Integer> createNaivePublisher(long itemCount) {

        final List<Integer> items = initIntSequence(itemCount);
        return new Publisher<Integer>() {

            @Override
            public void subscribe(Subscriber<? super Integer> subscriber) {
                subscriber.onSubscribe( new Subscription() {

                    private Iterator<Integer> iterator  = items.iterator();

                    @Override
                    public void cancel() {
                        
                    }

                    @Override
                    public void request(long n) {
                        log.info("{} data requested",n);
                        for (int i = 0; i < n ; i++) {
                            if (iterator.hasNext()) {
                                subscriber.onNext(iterator.next());

                            } else {
                                subscriber.onComplete();
                            }
                        }                        
                    }
                });
            }
        };
    }

    // static Publisher<Integer> createWithPublisherFactory(long itemCount) {

    //     final List<Integer> items = initIntSequence(itemCount);


    // }

    private static List<Integer> initIntSequence(long itemCount) {
        List<Integer> items = new ArrayList<>();

        for (int i = 0; i < itemCount; i++) {
            items.add(i);
        }
        return items ;

    }

    private static Subscriber<Integer> createSubscriber() {
        return  new Subscriber<Integer>() {

            private Subscription subscription;
            @Override
            public void onComplete() {
                log.info("onComplete");                
            }

            @Override
            public void onError(Throwable error) {
                log.info("onError: {}",error);                
            }

            @Override
            public void onNext(Integer data) {
                log.info("onNext: {}",data);     
                this.subscription.request(1);           
            }

            @Override
            public void onSubscribe(Subscription s) {
                log.info("onSubscribe");
                this.subscription = s;         
                this.subscription.request(1);       
            }
            
        };
    }
    
}
