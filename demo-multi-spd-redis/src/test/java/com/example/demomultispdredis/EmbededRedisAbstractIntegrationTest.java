package com.example.demomultispdredis;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import redis.embedded.RedisServer;

public abstract class EmbededRedisAbstractIntegrationTest {
    
	//start two redis

	private static RedisServer redisServer;

	private static RedisServer redisServer2;

	@BeforeAll
	public static void setup() throws Exception {

		System.out.println("start redis");

		redisServer = RedisServer.builder().port(6379).build();
		redisServer2= RedisServer.builder().port(6380).build();

		redisServer.start();;
		redisServer2.start();;

		System.out.println("redis started");

								  

	}

	@AfterAll
	public static void tearDown() throws Exception {
		System.out.println("stop redis");

		redisServer.stop(); ;
		redisServer2.stop();

		System.out.println("redis stoped");
	}    
}
