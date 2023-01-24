package com.example.democloudstreamwebfux.events;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class BarCreated extends ApplicationEvent {

    public BarCreated(Bar bar) {
        super(bar);
    }

    
}
