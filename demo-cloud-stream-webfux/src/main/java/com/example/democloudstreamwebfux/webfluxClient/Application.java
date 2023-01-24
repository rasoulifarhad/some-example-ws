package com.example.democloudstreamwebfux.webfluxClient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    
	public static void main(String[] args) throws Exception {

        SpringApplication app = new SpringApplication(Application.class);

        app.setWebApplicationType(WebApplicationType.REACTIVE);
        app.run(args);
        // SpringApplication.run(Application.class, args);
    }    
}
