package com.example.democloudstreamwebfux.events;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class FooCreated extends ApplicationEvent {

    public FooCreated(Foo foo) {
        super(foo);
    }
    
    
}


