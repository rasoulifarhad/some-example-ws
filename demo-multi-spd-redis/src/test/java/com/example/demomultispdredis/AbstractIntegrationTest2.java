package com.example.demomultispdredis;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;

@Testcontainers
@SpringBootTest
public class AbstractIntegrationTest2 {
    

    // @Container
    // static final  GenericContainer redisContainer = 
    //        new GenericContainer<>("redis:5.0.3-alpine")
    //             .withExposedPorts(6379);

    // static final List<File> SENTINRL_COMPOSE_FILES =  
    //         Lists.newArrayList(
    //                Files.newFile("/home/farhad/apps/redis/docker-setup/sentinel-docker-compose/docker-compose.yml"));

    // @Container
    // static final  DockerComposeContainer<?> redisSentinelContainer = 
    //         new DockerComposeContainer<>(SENTINRL_COMPOSE_FILES)
    //                .withExposedService("redis-sentinel", 26379)
    //                .withExposedService("redis", 6379)
    //                ;

    
    @Container
    public GenericContainer<?> redisContainer = new GenericContainer<>("redis:6.2.7-alpine")
                                                                                .withExposedPorts(6379);

    
                                                                                                                
    // @DynamicPropertySource
    // static void redisProperties(DynamicPropertyRegistry registry)  {

    //     // registry.add("spring.redis.host", redisContainer::getHost);
    //     // registry.add("spring.redis.port", redisContainer::getFirstMappedPort);
    //     // registry.add("spring.redis.sentinel.master",() -> "mymaster");
    //     // registry.add("pring.redis.sentinel.nodes",() -> "127.0.0.1:26379,127.0.0.1:26380") ;//#deliminated list of sentinels.

    // }

    @BeforeEach
    public void setupBeforeEach() {
        // String redisAddress = redisContainer.getHost();
        // int redisPort = redisContainer.getFirstMappedPort() ;
    }

    @Test
    public void connectToRedis() {
        RedisURI redisURI = RedisURI.builder()
                                            .withHost(redisContainer.getHost())
                                            .withPort(redisContainer.getFirstMappedPort())
                                            .build();
        RedisClient redisClient =  RedisClient.create(redisURI);
        StatefulRedisConnection<String,String>  connection =  redisClient.connect();

        String key = "AbstractIntegrationTest2:key1";
        String value = "AbstractIntegrationTest2:value1";
        connection.sync().set(key,value);

        String expected = value ;
        String actual= connection.sync().get(key);
        System.out.println("key: " + actual);
        assertEquals(expected, actual);

        connection.close();
        redisClient.shutdown();
    }    
}
