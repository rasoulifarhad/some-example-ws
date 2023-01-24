package com.example.democloudstreamwebfux.cloudstreamfunction;

import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication()
// @PropertySource("classpath:com/example/democloudstreamwebfux/cloudFunctionStreamIntegration/application.properties")
@Slf4j
public class Application {


    public static void main(String[] args) throws Exception {

        new SpringApplicationBuilder()
                .sources(Application.class)
                .bannerMode(Banner.Mode.OFF)
                .properties("spring.config.name=cloudstreamfunction")
                .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/cloudstreamfunction/")

                .web(WebApplicationType.SERVLET)
                .run(args);


    }
}