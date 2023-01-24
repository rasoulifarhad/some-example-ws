package com.example.springbootmultiredis.messaging.redis;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
* Spring Data Redis provides all the components you need to send and receive messages with Redis. 
* Specifically, you need to configure:
*
*    - A connection factory
*    - A message listener container
*    - A Redis template
*
* You will use the Redis template to send messages.
*
* You will register the Receiver with the message listener container so that it will receive 
* messages
*
* The connection factory drives both the template and the message listener container, letting 
* them connect to the Redis server.
*/
@Configuration
public class MessagingConfig {

    @Bean(name = "redis1MessageListenerContainer")
    public RedisMessageListenerContainer redisMessageListenerContainer(@Qualifier("redis1ConnectionFactory") 
                                                                            RedisConnectionFactory redisConnectionFactory,
                                                                        @Qualifier("redis1MessageListenerAdapter")
                                                                            MessageListenerAdapter messageListenerAdapter,

                                                                        @Qualifier("allRedisMessageListenerAdapter")
                                                                            MessageListenerAdapter allMessageListenerAdapter,

                                                                        @Qualifier("topic1And2RedisMessageListenerAdapter")
                                                                            MessageListenerAdapter topic1And2RedisMessageListenerAdapter
                                                                            ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        // container.addMessageListener(messageListenerAdapter, new PatternTopic("task1"));
        container.addMessageListener(allMessageListenerAdapter, allTask1());
        container.addMessageListener(topic1And2RedisMessageListenerAdapter, topic1());
        return container ;

    }
    

    @Bean(name = "redis2MessageListenerContainer")
    public RedisMessageListenerContainer redis2MessageListenerContainer(@Qualifier("redis2ConnectionFactory") 
                                                                            RedisConnectionFactory redisConnectionFactory,
                                                                        @Qualifier("redis2MessageListenerAdapter")
                                                                            MessageListenerAdapter messageListenerAdapter,

                                                                        @Qualifier("allRedisMessageListenerAdapter")
                                                                            MessageListenerAdapter allMessageListenerAdapter,

                                                                        @Qualifier("topic1And2RedisMessageListenerAdapter")
                                                                            MessageListenerAdapter topic1And2RedisMessageListenerAdapter
                                                                            ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        // container.addMessageListener(messageListenerAdapter, new PatternTopic("task2"));
        container.addMessageListener(allMessageListenerAdapter, allTask2());
        container.addMessageListener(topic1And2RedisMessageListenerAdapter, topic2());
        return container ;

    }

    @Bean(name = "redis1MessageListenerAdapter")
    public MessageListenerAdapter redis1MessageListenerAdapter(Receiver receiver) {
        return new MessageListenerAdapter(receiver, "receiveMessageFromRedis1");
    }

    @Bean(name = "redis2MessageListenerAdapter")
    public MessageListenerAdapter redis2MessageListenerAdapter(Receiver receiver) {
        return new MessageListenerAdapter(receiver, "receiveMessageFromRedis2");
    }

    @Bean(name = "allRedisMessageListenerAdapter")
    public MessageListenerAdapter allRedisMessageListenerAdapter(Receiver receiver) {
        return new MessageListenerAdapter(receiver, "receiveMessageFromAllRedises");
    }

    @Bean(name = "topic1And2RedisMessageListenerAdapter")
    public MessageListenerAdapter topic1And2RedisMessageListenerAdapter(Receiver receiver) {
        return new MessageListenerAdapter(receiver, "receiveMessageFromTopic1And2");
    }

    @Bean
    public Receiver receiver() {
        return new Receiver();
    }

    @Bean
    public Sender sender(RedisTemplate<String, String> redis1RedisTemplate,RedisTemplate<String, String> redis2RedisTemplate, ChannelTopic topic1, ChannelTopic topic2) {
        return new Sender(redis1RedisTemplate, redis2RedisTemplate, topic1, topic2);
    }

    @Bean
    public PatternTopic allTask1() {
        return new PatternTopic("allTask1");
    }

    @Bean
    public PatternTopic allTask2() {
        return new PatternTopic("allTask2");
    }

    @Bean
    public ChannelTopic topic1() {
        return new ChannelTopic("pubsub:queue1");
    }

    @Bean
    public ChannelTopic topic2() {
        return new ChannelTopic("pubsub:queue2");
    }
}

