
package  com.example.demomultispdredis;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@SpringBootTest(properties = {"spring.main.allow-bean-definition-overriding=true"})
public class DemoRedisSentinelRedisApiWithSpring {
    
  @Autowired
  private RedisTemplate<String,String> redisTemplate;

  /**
   *  In case of nested @Configuration class, the given configuration would be used “instead of” the 
   *  application’s primary configuration.
   */
  @Configuration
  public static class RedisConfigPrimary {

    @Bean
    public RedisConnectionFactory lettucConnectionFactory() {
      RedisSentinelConfiguration  sentinelConfig = 
                            new RedisSentinelConfiguration()
                                       .master("mymaster")
                                       .sentinel("127.0.0.1",26379)
                                       ;
      return new LettuceConnectionFactory(sentinelConfig);                                       
    }

    @Bean
    public RedisTemplate<String,String> redisTemplate() {
      RedisTemplate<String,String> redisTemplate = new RedisTemplate<>();
      redisTemplate.setConnectionFactory(lettucConnectionFactory());
      redisTemplate.setValueSerializer(new StringRedisSerializer());
      return redisTemplate ;
    }
  }

  /**
   *  IA nested @TestConfiguration class is used “in addition to” the application’s primary configuration.
   */
  @TestConfiguration
  public static class RedisConfigAddition {


    @Bean
    public RedisConnectionFactory lettucConnectionFactory() {
      RedisSentinelConfiguration  sentinelConfig = 
                            new RedisSentinelConfiguration()
                                       .master("mymaster")
                                       .sentinel("127.0.0.1",26379)
                                       ;
      return new LettuceConnectionFactory(sentinelConfig);                                       
    }

    @Bean
    public RedisTemplate<String,String> redisTemplate() {
      RedisTemplate<String,String> redisTemplate = new RedisTemplate<>();
      redisTemplate.setConnectionFactory(lettucConnectionFactory());
      redisTemplate.setValueSerializer(new StringRedisSerializer());
      return redisTemplate ;
    }
  }


  @Test
  public void testRedisSentinelRedisApi() {

    String prefix = "testRedisSentinelRedisApi:";
    String key = prefix + "key";
    String value = prefix + "value";
    redisTemplate.opsForValue().set(key, value);

    String actual = redisTemplate.opsForValue().get(key);
    System.out.println("key: " + actual);

    assertEquals(value, actual);

  }
}
