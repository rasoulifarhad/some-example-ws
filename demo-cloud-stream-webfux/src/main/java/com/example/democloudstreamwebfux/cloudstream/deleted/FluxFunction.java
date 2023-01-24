package com.example.democloudstreamwebfux.cloudstream.deleted;

import java.util.function.Function;

import org.springframework.messaging.Message;

import com.example.democloudstreamwebfux.cloudstream.StringPayload;

// public interface FluxFunction {
public interface FluxFunction extends Function<Message<StringPayload>,Message<StringPayload>> {
    
}
