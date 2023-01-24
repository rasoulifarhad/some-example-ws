package com.example.democloudstreamwebfux.events;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class BarService {

    private final ApplicationEventPublisher publisher;
    private final BarRepository barRepository;
    private final BarMapper barMapper;

    public BarService(ApplicationEventPublisher applicationEventPublisher,
                        BarRepository barRepository,
                        BarMapper barMapper) {
                            
        this.barRepository = barRepository ;
        this.publisher = applicationEventPublisher;
        this.barMapper = barMapper;
    }

    public Bar createBar(BarDto barDto) {

        Bar savedBar = barRepository.save(barMapper.dtoToEntity(barDto));
        publisher.publishEvent(new BarCreated(savedBar));
        return savedBar;

    }
    
}
