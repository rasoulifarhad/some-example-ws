package com.example.democloudstreamwebfux;



import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import com.example.democloudstreamwebfux.cloudstream.StringPayload;

// @SpringBootTest
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient(timeout = "10000")
class DemoCloudStreamWebfuxApplicationTests {


	@LocalServerPort
	int port ;

	@Autowired
	private WebTestClient webTestClient;
	

	@Test
	void contextLoads() {
	  // empty test that would fail if our Spring configuration does not load correctly
	}
  
	// @Test
	// void contextLoads2(ApplicationContext context) {
	//   assertThat(context).isNotNull();
	// }
  
	@BeforeEach
	public void setup() {
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 *  curl -X POST http://localhost:port(8080)/messages?message=hello
	 */
	@Test
	void testSendMessage() { 

		String msg = "Testmsg1" ;
		
		webTestClient
					.post()
                  
					.uri("/messages")
					// .contentType(MediaType.APPLICATION_JSON)
					// .accept(MediaType.APPLICATION_JSON)
					.body(BodyInserters.fromValue(new StringPayload(msg)))
					// .bodyValue(StringPayload.builder().value(msg).build())
					.exchange()
					.expectStatus().isOk()
					.expectBody(StringPayload.class).value(
						sp  -> {
							assertThat(sp.getValue()).isEqualTo(msg.toUpperCase());
						} );					
	}


	@Test
	void testSendMessage2() { 

		final String  msg = "Testmsg1" ;
		
		webTestClient
					.post()
                  
					.uri("/messagesNoReactive")
					// .contentType(MediaType.APPLICATION_JSON)
					// .accept(MediaType.APPLICATION_JSON)
					.body(BodyInserters.fromValue(new StringPayload(msg)))
					// .bodyValue(StringPayload.builder().value(msg).build())
					.exchange()
					.expectStatus().isOk()
					.expectBody(StringPayload.class).value(
						sp  -> {
							assertThat(sp.getValue()).isEqualTo(msg.toUpperCase());
						} );					

		final String  msg2 = "Farhad";
		webTestClient
						.post()
					  
						.uri("/messagesNoReactive")
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
						.body(BodyInserters.fromValue(new StringPayload(msg2)))
						// .bodyValue(StringPayload.builder().value(msg).build())
						.exchange()
						.expectStatus().isOk()
						.expectBody(StringPayload.class).value(
							sp  -> {
								assertThat(sp.getValue()).isEqualTo(msg2.toUpperCase());
							} );					
	
	}

}
