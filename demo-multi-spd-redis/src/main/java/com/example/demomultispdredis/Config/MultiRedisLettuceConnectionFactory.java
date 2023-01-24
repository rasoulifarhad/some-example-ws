package com.example.demomultispdredis.Config;

import java.util.Map;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.ReactiveRedisClusterConnection;
import org.springframework.data.redis.connection.ReactiveRedisConnection;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisClusterConnection;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConnection;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.util.StringUtils;

import com.example.demomultispdredis.exception.RedisRelatedException;

public class MultiRedisLettuceConnectionFactory implements   InitializingBean 
                                                           , DisposableBean
                                                           , RedisConnectionFactory
                                                           , ReactiveRedisConnectionFactory
                                                                                {
    
    private final Map<String, LettuceConnectionFactory> connectionFactoryMap ;
    private static final ThreadLocal<String> currentRedis = new ThreadLocal<>();

    public MultiRedisLettuceConnectionFactory(Map<String, LettuceConnectionFactory> connectionFactoryMap) {
        this.connectionFactoryMap = connectionFactoryMap;
    }

    public void setCurrentRedis(String currentRedis) {

        if (!connectionFactoryMap.containsKey(currentRedis)) {
            throw new RedisRelatedException("invalid currentREdis: " 
                                                + 
                                                currentRedis
                                                +", it dos not exists in configuration");
        }
        MultiRedisLettuceConnectionFactory.currentRedis.set(currentRedis);

    }

    private LettuceConnectionFactory currenLettuceConnectionFactory() {
        String currentRedis = MultiRedisLettuceConnectionFactory.currentRedis.get();
        if (StringUtils.hasText(currentRedis))  {
            MultiRedisLettuceConnectionFactory.currentRedis.remove();
            return connectionFactoryMap.get(currentRedis);
        }
        return connectionFactoryMap.get(MultiRedisProperties.DEFAULT);
    }

    @Override
    public DataAccessException translateExceptionIfPossible(RuntimeException ex) {
        return currenLettuceConnectionFactory().translateExceptionIfPossible(ex);
    }

    @Override
    public ReactiveRedisConnection getReactiveConnection() {
        return currenLettuceConnectionFactory().getReactiveConnection();
    }

    @Override
    public ReactiveRedisClusterConnection getReactiveClusterConnection() {
        
        return currenLettuceConnectionFactory().getReactiveClusterConnection();
    }

    @Override
    public RedisConnection getConnection() {
        return currenLettuceConnectionFactory().getConnection();
    }

    @Override
    public RedisClusterConnection getClusterConnection() {
        return currenLettuceConnectionFactory().getClusterConnection();
    }

    @Override
    public boolean getConvertPipelineAndTxResults() {
        return currenLettuceConnectionFactory().getConvertPipelineAndTxResults();
    }

    @Override
    public RedisSentinelConnection getSentinelConnection() {
        return currenLettuceConnectionFactory().getSentinelConnection();
    }

    @Override
    public void destroy() throws Exception {
        connectionFactoryMap.values()
                            .forEach(LettuceConnectionFactory::destroy);
        
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        connectionFactoryMap.values()
                            .forEach(LettuceConnectionFactory::afterPropertiesSet);
        
    }

}
