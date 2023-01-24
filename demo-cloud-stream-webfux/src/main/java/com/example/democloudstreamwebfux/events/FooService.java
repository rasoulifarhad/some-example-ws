package com.example.democloudstreamwebfux.events;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class FooService {
    
    private final ApplicationEventPublisher publisher;
    private final FooRepository fooRepository ;
    private final FooMapper fooMapper;

    public FooService(ApplicationEventPublisher applicationEventPublisher,FooRepository fooRepository,FooMapper fooMapper) {
        this.publisher = applicationEventPublisher;
        this.fooRepository = fooRepository;
        this.fooMapper = fooMapper;
    } 

    public Foo createFoo(FooDto fooDto) {
        
        Foo savedFoo = fooRepository.save(fooMapper.dtoToEntity(fooDto));
        publisher.publishEvent(new FooCreated(savedFoo));
        return savedFoo ;
    } 


    
}
