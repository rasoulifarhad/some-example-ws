package com.example.springbootmultiredis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SpringBootMultiredisApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootMultiredisApplication.class, args);
	}

}
