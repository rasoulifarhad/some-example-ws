package com.example.demomultispdredis;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;

public class DemoLettucedRedisContainerApplicationTests extends AbstractIntegrationTest{
    

    
    @Test
    public void connectToRedis() {
        RedisURI redisURI = RedisURI.builder()
                                            .withHost(redisContainer.getHost())
                                            .withPort(redisContainer.getFirstMappedPort())
                                            .build();
        RedisClient redisClient =  RedisClient.create(redisURI);
        StatefulRedisConnection<String,String>  connection =  redisClient.connect();

        connection.sync().set("key1", "value1");

        String expected = "value1" ;
        String actual= connection.sync().get("key1");
        System.out.println("key: " + actual);
        assertEquals(expected, actual);

        connection.close();
        redisClient.shutdown();
    }    
}
