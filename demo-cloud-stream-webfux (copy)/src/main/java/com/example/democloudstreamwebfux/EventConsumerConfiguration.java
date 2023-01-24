package com.example.democloudstreamwebfux;

import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

@Configuration
@Slf4j
public class EventConsumerConfiguration {
    

	@Bean
	public Consumer<Message<String>>  consume() {
		return message ->  {
			log.info("New message received: '{}',  enqueued time: {}",
				message.getPayload(),
				message.getHeaders().get(MessageHeaders.TIMESTAMP)
				// message.getHeaders().get(KafkaMessageHeaders.)
			);

		};
	}

}
