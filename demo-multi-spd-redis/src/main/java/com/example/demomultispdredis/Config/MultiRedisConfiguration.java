package com.example.demomultispdredis.Config;

import java.util.Map;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import com.google.common.collect.Maps;

import io.lettuce.core.resource.ClientResources;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(MultiRedisProperties.class)
@ConditionalOnProperty(prefix = "spring.redis", value = "enable-multi", matchIfMissing = false)
public class MultiRedisConfiguration {

    @Bean
    public MultiRedisLettuceConnectionFactory multiRedisLettuceConnectionFactory(
                ObjectProvider<LettuceClientConfigurationBuilderCustomizer> buillderCustomizers,
                ClientResources clientResources,
                MultiRedisProperties multiRedisProperties,
                ObjectProvider<RedisStandaloneConfiguration> standaloneConfigurationProvider,
                ObjectProvider<RedisSentinelConfiguration> senntinelConfigurationProvider,
                ObjectProvider<RedisClusterConfiguration> clusterConfigurationProvider
    ) {
        
        Map<String, LettuceConnectionFactory> connectionFactoryMap = Maps.newHashMap();

        Map<String,RedisProperties> multi = multiRedisProperties.getMulti();
        multi.forEach((k, p) -> {
            LettuceConnectionConfiguration lettuceConnectionConfiguration = new LettuceConnectionConfiguration(
                                                                                        p, 
                                                                                        standaloneConfigurationProvider, 
                                                                                        senntinelConfigurationProvider, 
                                  
                                                                                        clusterConfigurationProvider
                                                                                );
            LettuceConnectionFactory lettuceConnectionFactory = lettuceConnectionConfiguration.redisConnectionFactory(
                                                                                                    buillderCustomizers, 
                                                                                                    clientResources
                                                                                                ) ;
            
            connectionFactoryMap.put(k, lettuceConnectionFactory);                                                                                                 
        }); 

        
        return new MultiRedisLettuceConnectionFactory(connectionFactoryMap);
    }

    
}
