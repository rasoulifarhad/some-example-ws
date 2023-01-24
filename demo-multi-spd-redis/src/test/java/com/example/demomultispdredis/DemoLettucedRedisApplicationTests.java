package com.example.demomultispdredis;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;

@SpringBootTest
public class DemoLettucedRedisApplicationTests extends EmbededRedisAbstractIntegrationTest {
    

    @Test
    public void connectToRedis() {
        RedisClient redisClient =  RedisClient.create("redis://localhost:6379/0");
        StatefulRedisConnection<String,String>  connection =  redisClient.connect();

        connection.sync().set("key1", "value1");

        String actual= connection.sync().get("key1");
        System.out.println("key: " + actual);

        connection.close();
        redisClient.shutdown();
    }

    
}
