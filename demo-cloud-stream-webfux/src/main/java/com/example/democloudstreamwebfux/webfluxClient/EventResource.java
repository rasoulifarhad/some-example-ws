package com.example.democloudstreamwebfux.webfluxClient;

import static org.springframework.http.MediaType.APPLICATION_NDJSON_VALUE;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;

import java.time.Duration;
import java.time.ZonedDateTime;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@RestController
@Slf4j
public class EventResource {

    @GetMapping(path = "/events2/{id}")
    public Mono<Event> getNyId(@PathVariable("id") Long id) {

        Event event  = Event.of(id, ZonedDateTime.now());
        log.info("==========>Event: {}",event);
        return Mono.just(event) ;

    }

    @GetMapping(path = "/events2" , produces = TEXT_EVENT_STREAM_VALUE)
    public Flux<Event> eventStream() {
        return Flux
                    .interval(Duration.ofSeconds(1))
                                    
                    .map(t -> Event.produce())
                    ;
    }

    @GetMapping(path = "/longstream" , produces = APPLICATION_NDJSON_VALUE )
    public Flux<Long> getLongStream() {
        return Flux.interval(Duration.ofSeconds(2));
    }

    // @GetMapping(path = "/events2" , produces = TEXT_EVENT_STREAM_VALUE)
    // public Flux<Event> eventStream() {
    //     return Flux
    //                 .zip(Flux.interval(Duration.ofSeconds(1)),
    //                                 Flux.fromStream(Stream.generate(Event::produce)))
    //                 .map(Tuple2::getT2)
    //                 ;
    // }
    
}
