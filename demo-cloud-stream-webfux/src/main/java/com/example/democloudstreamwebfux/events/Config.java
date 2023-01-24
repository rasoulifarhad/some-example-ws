package com.example.democloudstreamwebfux.events;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper() ;
        return mapper ;
    }
    
}
