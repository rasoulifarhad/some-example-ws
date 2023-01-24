package com.example.democloudstreamwebfux.webfluxClient;

import java.time.ZonedDateTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(staticName = "of")
public class Event {

    private long id ;
    private ZonedDateTime when ;

    static Event produce() {
        return Event.of(System.currentTimeMillis(), ZonedDateTime.now());
    }
    
}
