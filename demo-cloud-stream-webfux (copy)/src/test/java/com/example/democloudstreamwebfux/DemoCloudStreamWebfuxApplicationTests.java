package com.example.democloudstreamwebfux;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;


// @SpringBootTest
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class DemoCloudStreamWebfuxApplicationTests {


	@LocalServerPort
	int port ;

	private WebTestClient webTestClient;
	
	@BeforeEach
	public void setup() {
        this.webTestClient = WebTestClient.bindToServer()
            .baseUrl("http://localhost:" + this.port)
            .build();
	}
	
	/**
	 *  curl -X POST http://localhost:port(8080)/messages?message=hello
	 */
	@Test
	void testSendMessage() { 

		String msg = "Test_msg" ;
		webTestClient
					.post()

					.uri(uriBuilder -> 
									uriBuilder.path("/messages")
									.queryParam("message", msg)
									.build()
					)
					.accept(MediaType.TEXT_PLAIN)
					// .bodyValue()
					.exchange()
					.expectStatus().isOk()
					.expectBody(String.class).isEqualTo(msg);
					;
	}

}
