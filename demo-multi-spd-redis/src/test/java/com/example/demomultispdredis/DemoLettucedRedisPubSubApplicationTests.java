package com.example.demomultispdredis;

// import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CountDownLatch;
// import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import io.lettuce.core.RedisClient;
// import io.lettuce.core.RedisFuture;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;
import io.lettuce.core.pubsub.api.reactive.RedisPubSubReactiveCommands;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@Slf4j
public class DemoLettucedRedisPubSubApplicationTests {
    
    private static final String CHANNEL = "pubsub::test";

    // private final BlockingDeque<String> bag = new LinkedBlockingDeque<>(99);

    @Test
    public void testResisSub() throws InterruptedException {

        // redis :// [password@] host [: port] [/ database]
        //  [? [timeout=timeout[d|h|m|s|ms|us|ns]]
        //  [&_database=database_]]
        //
        // There are four URI schemes:
        //     - redis – a standalone Redis server
        //     - rediss – a standalone Redis server via an SSL connection
        //     - redis-socket – a standalone Redis server via a Unix domain socket
        //     - redis-sentinel – a Redis Sentinel server

        RedisURI redisURI = RedisURI.create("redis://localhost:6379/0");
        RedisClient redisClient = RedisClient.create(redisURI);
        StatefulRedisPubSubConnection<String,String> connection = redisClient.connectPubSub();
        connection.addListener(new RedisPubSubAdapter<String,String>(){

            @Override
            public void message(String channel, String message) {
                log.info("Received: {} ,from Channel: {}" ,message,channel);
            }
            

        });

        RedisPubSubCommands<String,String> pubSubCommands =  connection.sync();
        pubSubCommands.subscribe(CHANNEL);

        // sender 
        StatefulRedisConnection<String,String> senderConn =  redisClient.connect();
        RedisCommands<String,String> senderCommands =  senderConn.sync() ;
        for (int i = 0; i < 10 ; i++) {
            senderCommands.publish(CHANNEL, "message:" + i);
        }

        TimeUnit.SECONDS.sleep(10);
        senderConn.close();
        connection.close();
        redisClient.shutdown();

    }

    @Test
    public void testPubSubAsyn() throws InterruptedException {

        final int MESSAGE_COUNT = 10 ;
        final CountDownLatch latch = new CountDownLatch(MESSAGE_COUNT);

        // redis :// [password@] host [: port] [/ database]
        //  [? [timeout=timeout[d|h|m|s|ms|us|ns]]
        //  [&_database=database_]]
        //
        // There are four URI schemes:
        //     - redis – a standalone Redis server
        //     - rediss – a standalone Redis server via an SSL connection
        //     - redis-socket – a standalone Redis server via a Unix domain socket
        //     - redis-sentinel – a Redis Sentinel server

        RedisURI redisURI = RedisURI.create("redis://localhost:6379/0");
        RedisClient redisClient = RedisClient.create(redisURI);
        StatefulRedisPubSubConnection<String,String> connection = redisClient.connectPubSub();
        connection.addListener(new RedisPubSubAdapter<String,String>(){

            @Override
            public void message(String channel, String message) {
                log.info("Received: {} ,from Channel: {}" ,message,channel);
                latch.countDown();
            }
            

        });
        
        RedisPubSubAsyncCommands<String,String> async = connection.async();
        // RedisFuture<Void> future =  
               async.subscribe(CHANNEL);

        // sender 
        StatefulRedisConnection<String,String> senderConn =  redisClient.connect();
        RedisCommands<String,String> senderCommands =  senderConn.sync() ;
        for (int i = 0; i < MESSAGE_COUNT ; i++) {
            senderCommands.publish(CHANNEL, "message:" + i);
        }
        senderConn.close();

        latch.await();

        // TimeUnit.SECONDS.sleep(10);
        connection.close();
        redisClient.shutdown();
        
    }

    @Test
    public void testPubSubReactive() throws InterruptedException {

        final int MESSAGE_COUNT = 10 ;
        final CountDownLatch latch = new CountDownLatch(MESSAGE_COUNT);

        // redis :// [password@] host [: port] [/ database]
        //  [? [timeout=timeout[d|h|m|s|ms|us|ns]]
        //  [&_database=database_]]
        //
        // There are four URI schemes:
        //     - redis – a standalone Redis server
        //     - rediss – a standalone Redis server via an SSL connection
        //     - redis-socket – a standalone Redis server via a Unix domain socket
        //     - redis-sentinel – a Redis Sentinel server

        RedisURI redisURI = RedisURI.create("redis://localhost:6379/0");
        RedisClient redisClient = RedisClient.create(redisURI);
        StatefulRedisPubSubConnection<String,String> connection = redisClient.connectPubSub();
        
        RedisPubSubReactiveCommands<String,String> reactiveCommands = connection.reactive();
        reactiveCommands.subscribe(CHANNEL).subscribe();

        reactiveCommands.observeChannels()
                                        .doOnNext(channlMessage -> {
                                            log.info("Received: {} ,from : {} " , channlMessage.getMessage() ,channlMessage.getChannel());
                                            latch.countDown();
                                        })
                                        .subscribe();
                                        ;
        
        
        // sender 
        StatefulRedisConnection<String,String> senderConn =  redisClient.connect();
        RedisCommands<String,String> senderCommands =  senderConn.sync() ;
        for (int i = 0; i < 10 ; i++) {
            senderCommands.publish(CHANNEL, "message:" + i);
        }
        senderConn.close();

        latch.await();

        // TimeUnit.SECONDS.sleep(10);

        connection.close();
        redisClient.shutdown();
                                        
    }
}
