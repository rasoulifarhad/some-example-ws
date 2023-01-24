package com.example.demomultispdredis;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.ReadFrom;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.masterreplica.MasterReplica;
import io.lettuce.core.masterreplica.StatefulRedisMasterReplicaConnection;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import io.lettuce.core.sentinel.api.StatefulRedisSentinelConnection;
import io.lettuce.core.sentinel.api.sync.RedisSentinelCommands;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class DemoLettuceRedisSentinelApplicationTests /* extends AbstractIntegrationTest2 */ {

    @Test
    public void testRedisUsingRedisSntinel() {
        // Syntax:
        // redis-sentinel://[password@]host[:port][,host2[:port2]][/databaseNumber]#sentinelMasterId
        // redis-sentinel :// [[username :] password@] host1[:port1] [, host2[:port2]]
        // [, hostN[:portN]] [/database]
        // [?[timeout=timeout[d|h|m|s|ms|us|ns]] [&sentinelMasterId=sentinelMasterId]

        // RedisClient redisClient =
        // RedisClient.create("redis-sentinel://localhost:26379/0#mymaster");
        // RedisClient redisClient =
        // RedisClient.create("redis-sentinel://localhost:26379/0?sentinelMasterId=mymaster");

        // RedisURI redisURI =
        // RedisURI.create("redis-sentinel://localhost:26379/0#mymaster");
        RedisURI redisURI = RedisURI.create("redis-sentinel://localhost:26379/0?sentinelMasterId=mymaster");

        RedisClient redisClient = RedisClient.create(redisURI);

        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisCommands<String, String> synCommands = connection.sync();

        String prefix = "testRedisSentiel2:";
        String key = prefix + "key";
        String value = prefix + "value";
        synCommands.set(key, value);

        String actual = synCommands.get(key);

        assertEquals(value, actual);
        // System.out.println("key: " + actual);

        connection.close();
        redisClient.shutdown();
    }

    /**
     * RedisSentinelConfiguration can also be defined with a PropertySource, which
     * lets you set the
     * following properties:
     *
     * Configuration Properties
     * spring.redis.sentinel.master: name of the master node.
     * spring.redis.sentinel.nodes: Comma delimited list of host:port pairs.
     * spring.redis.sentinel.username: The username to apply when authenticating
     * with Redis Sentinel(requires Redis 6)
     * spring.redis.sentinel.password: The password to apply when authenticating
     * with Redis Sentinel
     */
    @Test
    public void testRedisSentiel2() {

        LettuceClientConfiguration clientConfig = LettuceClientConfiguration
                .builder()
                .build();

        RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration()
                .master("mymaster")
                .sentinel("127.0.0.1", 26379)
        // .sentinel("127.0.0.1", 26380)
        ;

        LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(sentinelConfig, clientConfig);
        lettuceConnectionFactory.afterPropertiesSet();

        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(lettuceConnectionFactory);
        redisTemplate.afterPropertiesSet();

        String prefix = "testRedisSentiel2:";
        String key = prefix + "key";
        String value = prefix + "value";
        redisTemplate.opsForValue().set(key, value);

        String actual = redisTemplate.opsForValue().get(key);
        System.out.println("key: " + actual);

        assertEquals(value, actual);

        lettuceConnectionFactory.destroy();

    }

    @Test
    public void testRedisSentineRedisApiWithLettuce() {
        RedisClient redisClient = RedisClient.create();
        RedisURI redisURI = RedisURI.create("redis-sentinel://localhost:26379/0?sentinelMasterId=mymaster");

        StatefulRedisMasterReplicaConnection<String, String> connection = MasterReplica.connect(redisClient,
                StringCodec.UTF8, redisURI);

        connection.setReadFrom(ReadFrom.MASTER_PREFERRED);

        RedisCommands<String, String> synCommands = connection.sync();

        String prefix = "testRedisSentineRedisApiWithLettuce:";
        String key = prefix + "key";
        String value = prefix + "value";
        synCommands.set(key, value);

        String actual = synCommands.get(key);

        assertEquals(value, actual);
        System.out.println("key: " + actual);

        connection.close();
        redisClient.shutdown();
    }

    @Test
    public void testRedisSentineSentinelApiWithLettuce() {

        RedisURI redisURI = RedisURI.create("redis-sentinel://localhost:26379/0?sentinelMasterId=mymaster");
        RedisClient redisClient = RedisClient.create(redisURI);

        StatefulRedisSentinelConnection<String,String> sentinelConnection = redisClient.connectSentinel();

        RedisSentinelCommands<String,String> sentinelCommands =  sentinelConnection.sync();

        String masterAddr = sentinelCommands.getMasterAddrByName("mymaster").toString();
        log.info("masterAddr: {} ",masterAddr);
        Map<String,String> masterMap =  sentinelCommands.master("mymaster");

        log.info("master: {} ",masterMap);

        sentinelConnection.close();
        redisClient.shutdown();

    }

    @Test
    public void testRedisSentineSentinelApiResourceWithLettuce() {

        ClientResources clientResources =  DefaultClientResources.create();

        ClientOptions clientOptions =  ClientOptions.builder()
                                                .autoReconnect(true)
                                                .pingBeforeActivateConnection(true)
                                                .build();

        RedisURI redisURI = RedisURI.create("redis-sentinel://localhost:26379/0?sentinelMasterId=mymaster");
        RedisClient redisClient = RedisClient.create(clientResources ,redisURI);

        redisClient.setOptions(clientOptions);

        StatefulRedisSentinelConnection<String,String> sentinelConnection = redisClient.connectSentinel();

        RedisSentinelCommands<String,String> sentinelCommands =  sentinelConnection.sync();

        String masterAddr = sentinelCommands.getMasterAddrByName("mymaster").toString();
        log.info("masterAddr: {} ",masterAddr);
        Map<String,String> masterMap =  sentinelCommands.master("mymaster");

        log.info("master: {} ",masterMap);

        sentinelConnection.close();
        redisClient.shutdown();
        clientResources.shutdown();

    }

}
