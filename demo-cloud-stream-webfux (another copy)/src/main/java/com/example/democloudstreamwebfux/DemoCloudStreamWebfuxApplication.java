package com.example.democloudstreamwebfux;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.democloudstreamwebfux.model.StringPayload;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
// @EnableWebFlux
@Slf4j
public class DemoCloudStreamWebfuxApplication {

	public static void main(String[] args) throws Exception {
			SpringApplication.run(DemoCloudStreamWebfuxApplication.class, args);
	}

	// @Bean
	// ApplicationRunner sendMessageWebClientRunner(WebClient.Builder builder) {
	// 	WebClient webClient = builder
	// 							.baseUrl("http://localhost:8080")
	// 							.build();

	// 	return args -> {
	// 		String sendMessage="TestMessage";

	// 		webClient
	// 				.post()
	// 				.uri("/messages")
	// 				.contentType(MediaType.APPLICATION_JSON)
	// 				.body(BodyInserters.fromValue(new StringPayload(sendMessage)))
	// 				.accept(MediaType.APPLICATION_JSON)
	// 				.retrieve()
	// 				.bodyToFlux(StringPayload.class)
	// 				.map(StringPayload::getValue)
	// 				.subscribe(msg -> log.info("sendMessageWebClientRunner : {} ",msg) );

	
	// 	};
	// }

}
