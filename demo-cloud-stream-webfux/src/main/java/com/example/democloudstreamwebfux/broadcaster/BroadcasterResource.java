package com.example.democloudstreamwebfux.broadcaster;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import org.reactivestreams.Publisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@RestController
@RequiredArgsConstructor
@Slf4j
public class BroadcasterResource {
    
    // @NonNull
    // private final ApplicationEventPublisher publisher;

    @NonNull
    private final Sinks.Many<BroadcastMessage> broadcasterSink;
    @NonNull
    private final Collection<BroadcastMessage> db ;
    @NonNull
    private Flux<BroadcastMessage> broadcastLPublisher ;
    @NonNull
    private Consumer<BroadcastMessage> broadcastListener ;

    @GetMapping({"/broadcaster/last","/broadcaster/last/{amount}"})
    public Flux<BroadcastMessage>  last(@PathVariable(required = false) Long amount) {
        long n = Optional
                        .ofNullable(amount)
                        .filter(l ->  l >= 0)
                        .orElse(Long.MAX_VALUE)
                        ;

        return  Flux
                    .fromStream(  db
                                    .stream() 
                                    .sorted((o1, o2) -> o1.getId().compareTo(o2.getId()) )
                                    
                    ) 
                    .take(n)
                    ;
    }

    @GetMapping(value = "/broadcaster",
                produces = {
                    MediaType.TEXT_EVENT_STREAM_VALUE,
                    MediaType.APPLICATION_NDJSON_VALUE })
    public Flux<BroadcastMessage> stream() {
        return broadcastLPublisher;
    }

    @PostMapping("/broadcaster")
    public Mono<Void> send(@RequestBody BroadcastMessage broadcastMessage) {
        Objects.requireNonNull(broadcastMessage, "broadcastMessage must not be null");
        Objects.requireNonNull(broadcasterSink, "publisher must not be null");
        log.info("Received : {} " , broadcastMessage);

        broadcastListener.accept(broadcastMessage);
        return Mono.fromRunnable(() -> broadcasterSink.tryEmitNext(broadcastMessage));

    }

}
