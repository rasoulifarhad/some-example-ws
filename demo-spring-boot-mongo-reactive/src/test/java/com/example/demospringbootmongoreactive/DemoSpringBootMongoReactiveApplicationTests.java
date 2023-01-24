package com.example.demospringbootmongoreactive;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RouterFunction;

import lombok.extern.slf4j.Slf4j;

// @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
class DemoSpringBootMongoReactiveApplicationTests {

	
	@Autowired
	private WebTestClient client; // available with Spring WebFlux
   
	// @Autowired
	// private TestRestTemplate restTemplate; // available with Spring WebFlux
	
	// @LocalServerPort
	// private Integer port;

	@Autowired
	RouterFunction<?> routerFunction ;

	// private  WebTestClient client ;

	@BeforeEach
	public  void setup() {
		// client  = WebTestClient
		// 					.bindToRouterFunction(routerFunction)
		// 					.configureClient()
		// 					.build();
	}

	@Test
	public void getNonExistedPosts() {
		client
			.get()
			.uri("/posts/xxxx")
			.exchange()
			.expectStatus().isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	public void getAllPostsWillBeOk() {
		client
				.get()
				.uri("/posts/")
				.exchange()
				.expectStatus().isOk();
	}

	@Test
	public void deletePostsReturnNoContent() {
		client
				.delete()
				.uri("/posts/xxxx")
				.exchange()
				.expectStatus().isNoContent();
	}

	@Test
	public void postsCrudOperations() {
		int randonInt = new Random().nextInt(Integer.MAX_VALUE);
		String title = "Post test " + randonInt ;

		FluxExchangeResult<Void> postResult =  
										client
												.post()
												.uri("/posts/")
												.body(BodyInserters.fromValue(Post.builder().title(title).content("Content of " + title).build()))
												.exchange()
												.expectStatus().isCreated() 
												.returnResult(Void.class);
												;

		URI location =  postResult.getResponseHeaders().getLocation();												

		log.info("Post header location: {}" , location);

		assertNotNull(location);

		EntityExchangeResult<byte[]> getResult = 
												client
														.get()
														.uri(location)
														.exchange()
														.expectStatus().isOk()
														.expectBody().jsonPath("$.title").isEqualTo(title)
														.returnResult();

		String getPost = new String(getResult.getResponseBody()) ;
		assertTrue(getPost.contains(title));

		client
				.delete()
				.uri(location)
				.exchange()
				.expectStatus().isNoContent();
	}



}
