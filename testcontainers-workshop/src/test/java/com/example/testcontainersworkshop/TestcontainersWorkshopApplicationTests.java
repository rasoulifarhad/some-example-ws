package com.example.testcontainersworkshop;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import lombok.extern.slf4j.Slf4j;

@Testcontainers
@SpringBootTest
@Slf4j
class TestcontainersWorkshopApplicationTests {

	@Container
    private GenericContainer<?> redisContainer = new GenericContainer<>("redis:6.2.7-alpine")
                                                                                .withExposedPorts(6379);
																				
	@Test
	void testContainers() {
		log.info("-------------------------------------------------------------------------");
		log.info("container id : {}" , redisContainer.getContainerId());
		
	}

}
