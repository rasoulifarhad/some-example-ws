package com.example.democloudstreamwebfux.events;

import org.springframework.boot.Banner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class Application {
    public static void main(String[] args) throws Exception {

        new SpringApplicationBuilder()
                .sources(Application.class)
                .bannerMode(Banner.Mode.OFF)
                .properties("spring.config.name=app")
                .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/events/")
                .web(WebApplicationType.REACTIVE)
                .run(args);


    }    
}
