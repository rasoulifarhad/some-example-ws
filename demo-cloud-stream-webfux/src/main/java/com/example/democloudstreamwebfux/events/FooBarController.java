package com.example.democloudstreamwebfux.events;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@Slf4j
@Configuration
public class FooBarController {

    private final FooMapper fooMapper ;
    private final BarMapper barMapper ;
    private final BarService barService ;
    private final FooService fooService ;
    private final Flux<FooCreated> fooCreatedEvents;
    private final Flux<BarCreated> barCreatedEvents ;


    @Autowired
    public FooBarController(FooMapper fooMapper ,
                            BarMapper barMapper ,
                            BarService barService,
                            FooService fooService,
                            FooCreatedEventProcessor fooCreatedEventProcessor,
                            BarCreatedEventProcessor barCreatedEventProcessor) {

        this.fooMapper = fooMapper ;
        this.barMapper = barMapper ;
        this.fooService = fooService;
        this.barService = barService ;
        fooCreatedEvents = Flux.create(fooCreatedEventProcessor).share() ;
        barCreatedEvents = Flux.create(barCreatedEventProcessor).share();
    }


    @PostMapping(value="/foos")
    public ResponseEntity<FooDto> sendFoo(@RequestBody @Valid FooDto foo) {
        Foo createdFoo = fooService.createFoo(foo);
        return ResponseEntity.ok().body(fooMapper.entityToDto(createdFoo));
    }
    
    @PostMapping(value="/bars")
    public ResponseEntity<BarDto> sendBar(@RequestBody @Valid BarDto bar) {
        Bar createdBar =  barService.createBar(bar);
        return ResponseEntity.ok().body(barMapper.entityToDto(createdBar));
    }

    @GetMapping( value = "/foo/sse" , produces = MediaType.TEXT_EVENT_STREAM_VALUE )
    public Flux<FooDto> fooStream() {
        log.info("start listening to foo collections");
        return fooCreatedEvents.map(fooCreated -> fooMapper.entityToDto( (Foo)fooCreated.getSource() ));

    }
    
    @GetMapping( value = "/bar/sse" , produces = MediaType.TEXT_EVENT_STREAM_VALUE )
    public Flux<BarDto> barStream() {
        log.info("start listening to bar collections");
        return barCreatedEvents.map( barCreated -> barMapper.entityToDto( (Bar)barCreated.getSource() ));

    }

}
