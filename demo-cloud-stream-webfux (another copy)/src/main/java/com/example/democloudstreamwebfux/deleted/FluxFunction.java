package com.example.democloudstreamwebfux.deleted;

import java.util.function.Function;

import org.springframework.messaging.Message;

import com.example.democloudstreamwebfux.model.StringPayload;

// public interface FluxFunction {
public interface FluxFunction extends Function<Message<StringPayload>,Message<StringPayload>> {
    
}
