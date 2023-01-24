package com.example.demoasyncmethod;

import java.util.concurrent.Executor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@SpringBootApplication
@EnableAsync
public class DemoAsyncMethodApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoAsyncMethodApplication.class, args).close();;
	}

	@Bean
	public Executor taskExecutor(){
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor() ;
		executor.setCorePoolSize(5);
		executor.setMaxPoolSize(5);
		executor.setQueueCapacity(500);
		executor.setThreadNamePrefix("GithubLookup-");
		executor.initialize();
		return executor ;
	}


}
