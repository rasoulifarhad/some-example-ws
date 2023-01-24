package com.example.democloudstreamwebfux.events;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class FooMapper {

    private final ModelMapper modelMapper ;

 
    public FooMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
        modelMapper.createTypeMap(FooDto.class, Foo.class).addMappings(mapping -> mapping.skip(Foo::setId) );

    }

    public FooDto entityToDto(Foo foo ) {

        log.info("");
        FooDto fooDto =  modelMapper.map(foo, FooDto.class);
        log.info("");
        return fooDto ;

    }    

    public Foo dtoToEntity(FooDto fooDto ) {

        log.info("");
        Foo foo = modelMapper.map(fooDto, Foo.class);
        log.info("");
        return foo ;

    }    


}
