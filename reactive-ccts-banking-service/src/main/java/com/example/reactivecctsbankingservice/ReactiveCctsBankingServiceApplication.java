package com.example.reactivecctsbankingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@EnableMongoRepositories
@SpringBootApplication
public class ReactiveCctsBankingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReactiveCctsBankingServiceApplication.class, args);
	}

}
