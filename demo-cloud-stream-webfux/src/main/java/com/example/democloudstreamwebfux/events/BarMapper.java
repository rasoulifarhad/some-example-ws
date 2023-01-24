package com.example.democloudstreamwebfux.events;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class BarMapper {
    
    private final ModelMapper modelMapper ;

    public BarMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
        modelMapper.createTypeMap(BarDto.class, Bar.class).addMappings(mapping -> mapping.skip(Bar::setId) );
    }

    public BarDto entityToDto(Bar bar ) {

        log.info("");
        BarDto barDto =  modelMapper.map(bar, BarDto.class);
        log.info("");
        return barDto ;

    }  

    public Bar dtoToEntity(BarDto barDto ) {

        log.info("");
        
        Bar bar = modelMapper.map(barDto, Bar.class);
        log.info("{}",bar);
        return bar ;

    }    

}
