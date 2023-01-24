package com.example.democloudstreamwebfux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class DemoCloudStreamWebfuxApplication {

	public static void main(String[] args) throws Exception {

        ConfigurableApplicationContext applicationContext = new SpringApplicationBuilder(DemoCloudStreamWebfuxApplication.class)
                .properties(
                        "spring.config.location:optional:classpath:/demoCloudStreamWebfuxApplication.yml")
                .build().run(args);
 
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
 
        log.info(environment.getProperty("cmdb.resource-url"));

			SpringApplication.run(DemoCloudStreamWebfuxApplication.class, args);
	}


}
