package com.example.demomultispdredis;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(properties = {
    "spring.redis.enable-multi=true",
    "spring.redis.multi.default.host=127.0.0.1",
    "spring.redis.multi.default.port=6379",
    "spring.redis.multi.test.host=127.0.0.1",
     "spring.redis.multi.test.port=6380"
})
public class DemoMultispdRedisTestContainerTestsTestContainerTest {
    
    private static GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:5.0.3-alpine"))
    .withExposedPorts(6379);


    @BeforeAll
    public static void setupRedisServer() {
        
        redisContainer.start();

        System.setProperty("spring.redis.host", redisContainer.getHost()) ;
        System.setProperty("spring.redis.port", redisContainer.getFirstMappedPort() + "");
 
    }

    @AfterAll
    public static void teardownRedisServer() {
        redisContainer.stop();
    }
    
}
