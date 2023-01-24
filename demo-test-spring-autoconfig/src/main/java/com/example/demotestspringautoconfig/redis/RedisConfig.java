package com.example.demotestspringautoconfig.redis;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;

@Configuration
// @EnableConfigurationProperties(RedisProperties.class)
public class RedisConfig { 
    
    @Bean
    @Profile("simple")
    public LettuceConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(new RedisStandaloneConfiguration("localhoste",6379));
    }

    @Bean
    @Profile("simple-standalone")
    public LettuceConnectionFactory redisStandaloneConnectionFactory() {
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration
                                                                            .builder()
                                                                            .build();
        RedisStandaloneConfiguration serverConfig = new RedisStandaloneConfiguration("localhoste",6379) ;

        return new LettuceConnectionFactory(serverConfig,clientConfig);
    }

    @Bean
    @Profile("simple-sentinel")
    // Sometimes, direct interaction with one of the Sentinels is required. Using 
    // RedisConnectionFactory.getSentinelConnection() or RedisConnection.getSentinelCommands() gives you access to 
    // the first active Sentinel configured
    public RedisConnectionFactory lettucSentinelConnectionFactory() {
        RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration()
                                                                .master("mymaster")
                                                                .sentinel("127.0.0.1",26379)
                                                                ;
        return new LettuceConnectionFactory(sentinelConfig);

    }

    @Bean
    public StringRedisTemplate  stringRedisTemplate(RedisConnectionFactory redisConnectionFactory){
        StringRedisTemplate stringRedisTemplate =  new StringRedisTemplate();
        stringRedisTemplate.setConnectionFactory(redisConnectionFactory);
        return stringRedisTemplate ;

    }

    @Bean
    public RedisTemplate<?,?> integerRedisTemplate(RedisConnectionFactory connectionFactory){
        RedisTemplate<?,?> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        // template.setKeySerializer(new StringRedisSerializer());
	    // template.setValueSerializer( RedisSerializer.byteArray());
        // template.setHashKeySerializer(new StringRedisSerializer());
	    // template.setHashValueSerializer( RedisSerializer.byteArray());
        return template;
    }

    // @Bean
    // public RedisTemplate<String, Serializable> serializableRedisTemplate(RedisConnectionFactory connectionFactory){
    //     RedisTemplate<String, Serializable> template = new RedisTemplate<>();
    //     template.setKeySerializer(RedisSerializer.string());
    //     template.setValueSerializer(RedisSerializer.json());
    //     return template;
    // }

    // @Bean
    // public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory() {
    //     return new LettuceConnectionFactory(new RedisStandaloneConfiguration("server",6379));
    // }
    // @Bean
    // public ReactiveRedisTemplate<String,String> stringReactiveRedisTemplate(ReactiveRedisConnectionFactory connectionFactory) {
        
    //     return new ReactiveRedisTemplate<>(connectionFactory, RedisSerializationContext.string());

    // }
    
    @Bean
    ReactiveRedisTemplate<String, Long> reactiveRedisTemplate(ReactiveRedisConnectionFactory factory) {
      JdkSerializationRedisSerializer jdkSerializationRedisSerializer = new JdkSerializationRedisSerializer();
      StringRedisSerializer stringRedisSerializer = StringRedisSerializer.UTF_8;
      GenericToStringSerializer<Long> longToStringSerializer = new GenericToStringSerializer<>(Long.class);
      ReactiveRedisTemplate<String, Long> template = new ReactiveRedisTemplate<>(factory,
          RedisSerializationContext.<String, Long>newSerializationContext(jdkSerializationRedisSerializer)
              .key(stringRedisSerializer).value(longToStringSerializer).build());
      return template;
    }
    
    // @Autowired
    // private ReactiveRedisTemplate<String, Long> reactiveRedisTemplate;    

    @Bean(destroyMethod = "shutdown")
    ClientResources clientResources() {
        return DefaultClientResources.create();
    }

    @Bean
    public RedisStandaloneConfiguration redisStandaloneConfiguration() {
        return new RedisStandaloneConfiguration("127.0.0.1", 6379);
    }

    @Bean
    public ClientOptions clientOptions(){
        return ClientOptions.builder()
                .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                .autoReconnect(true)
                .build();
    }
    
    @SuppressWarnings("rawtypes")
    @Bean
    LettucePoolingClientConfiguration lettucePoolConfig(ClientOptions options, ClientResources dcr){
        return LettucePoolingClientConfiguration.builder()
                .poolConfig(new GenericObjectPoolConfig())
                .clientOptions(options)
                .clientResources(dcr)
                .build();
    }    

    @Bean
    public RedisConnection redisConnection(RedisConnectionFactory redisConnectionFactory) {
        RedisConnection connection = redisConnectionFactory.getConnection();
        String keyspaceNotificationsConfigParameter = "KEA";
        connection.setConfig("notify-keyspace-events", keyspaceNotificationsConfigParameter);
        return connection;
    }
}
