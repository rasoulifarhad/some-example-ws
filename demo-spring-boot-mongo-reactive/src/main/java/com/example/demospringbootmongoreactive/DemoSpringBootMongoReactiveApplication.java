package com.example.demospringbootmongoreactive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.*;


import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@SpringBootApplication
public class DemoSpringBootMongoReactiveApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoSpringBootMongoReactiveApplication.class, args);
	}


	@Bean
	public RouterFunction<ServerResponse> routes(PostRepository posts,PostHandler postHandler) {

		RouterFunction<ServerResponse> postsRoutes = 
								route(accept(MediaType.APPLICATION_JSON).and(GET("/")), postHandler::all)	
								.andRoute(accept(MediaType.APPLICATION_NDJSON).and(GET("/")), postHandler::stream)
								.andRoute(POST("/"), postHandler::create)
								.andRoute(GET("/slug"), postHandler::get)
								.andRoute(PUT("/slug"), postHandler::update)	
								.andRoute(DELETE("/slug"), postHandler::delete);

		return nest(path("/posts"), postsRoutes);
		
	}

}
