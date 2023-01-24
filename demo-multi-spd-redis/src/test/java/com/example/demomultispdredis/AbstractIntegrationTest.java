package com.example.demomultispdredis;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.GenericContainer;

public class AbstractIntegrationTest {
    
    static GenericContainer<?> redisContainer = null;
    static {
        redisContainer = new GenericContainer<>("redis:6.2.7-alpine")
                                                        .withExposedPorts(6379) ;
            redisContainer.start(); 
            
  
        
    }

    @BeforeAll
    static void setup() {
        redisContainer.start(); 
        System.setProperty("spring.redis.host", redisContainer.getHost()) ;
        System.setProperty("spring.redis.port", redisContainer.getFirstMappedPort() + "");
    }

    @AfterAll
    static void tear() {
        redisContainer.stop();
    }

}
