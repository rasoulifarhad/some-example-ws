package com.example.demotestspringautoconfig;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.hash.HashMapper;
import org.springframework.data.redis.hash.Jackson2HashMapper;
import org.springframework.data.redis.hash.ObjectHashMapper;
import org.springframework.transaction.annotation.Transactional;

import com.example.demotestspringautoconfig.util.PrintUtil;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@SpringBootApplication
public class DemoTestSpringAutoconfigApplication {

	public static void main(String[] args) {
		final ConfigurableApplicationContext context = 
				SpringApplication.run(DemoTestSpringAutoconfigApplication.class, args);
		// final List<String> candidate =  SpringFactoriesLoader.loadFactoryNames(EnableAutoConfiguration.class, null); 
		// System.out.println("----------------------------------------------------");
		// candidate.forEach(System.out::println);
		// System.out.println("----------------------------------------------------");
		

		PrintUtil.printCaption("Spring Environment Exploration");

		final ConfigurableEnvironment environment = context.getEnvironment();
		PrintUtil.printKeyValue("server.port", environment.getProperty("server.port"));
	
		// print all currently activated profiles
		PrintUtil.printKeyValue("active profiles:", environment.getActiveProfiles());
	
		// print all property sources
		final String[] sources = environment.getPropertySources()
			.stream()
			.map(Object::toString)
			.toArray(String[]::new);
		PrintUtil.printKeyValue("property sources:", sources);
	
		// close the spring application
		System.out.println("-----------------------------");
		System.exit(SpringApplication.exit(context));			}

	// inject the actual template
    @Autowired
	StringRedisTemplate stringRedisTemplate;

    @Autowired
	RedisTemplate<String,Map<String,Object>> redisTemplate;

	// inject the template as ListOperations
	@Resource(name = "stringRedisTemplate")
	private ListOperations<String,String> listOps ;
	
	// inject the template as ValueOperations
	@Resource(name = "stringRedisTemplate")
	private ValueOperations<String,String> valueOps ;


	@Bean
	@Order(1)
	public ApplicationRunner runnerSimpleDemo() {
		return (args) -> {

			String key = "key1"  ;
			String value = "value1";
			// String expected = value;
			stringRedisTemplate.opsForValue().set(key, value);
	
			String actual = stringRedisTemplate.opsForValue().get(key);
			System.out.println("#########################################");
			System.out.println(actual);
			System.out.println("#########################################");
		};
	}

	@Bean
	@Order(2)
	public ApplicationRunner runnerSimpleTemplateViewDemo() {
		return (args) -> {

			String key = "key2"  ;
			String value = "value2";
			// String expected = value;
			valueOps.set(key, value);
	
			String actual = valueOps.get(key);
			System.out.println("#########################################");
			System.out.println(actual);
			System.out.println("#########################################");
		};
	}

	@Bean
	@Order(3)
	public ApplicationRunner runnerUseCallbackSimpleDemo(){
		return (args) -> {
			final String key = "key3"  ;
			final String value = "value3";
			// String expected = value;

			stringRedisTemplate.execute(new RedisCallback<Object>() {

				@Override
				public Object doInRedis(RedisConnection connection) throws DataAccessException {
					// Long size = connection.dbSize();
					((StringRedisConnection)connection).set(key, value);
					return null;
				}
				
			});

			String actual =  stringRedisTemplate.execute(new RedisCallback<String>() {

				@Override
				public String doInRedis(RedisConnection connection) throws DataAccessException {
					// Long size = connection.dbSize();
					String val = ((StringRedisConnection)connection).get(key);
					return val;
				}
				
			});

			System.out.println("#########################################");
			System.out.println(actual);
			System.out.println("#########################################");
		};
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@ToString
	public static class Person {
		String fName;
		String lName;
		Address address;
  		Date date;
  		LocalDateTime localDateTime;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@Transactional
	public static class Address {
		String city;
  		String country;
	}

	// inject the template as HashOperations
	@Resource(name = "redisTemplate")
	private HashOperations<String,byte [] , byte [] > hashOps ;


	@Bean
	@Order(4)
	public ApplicationRunner  runnerHashMappingDemo () {
		return (args) -> {
			HashMapper<Object , byte [] , byte []>  mapper = new ObjectHashMapper() ;

			String key = "person:me";
			Person person=  new Person();
			person.setFName("farhad");
			person.setLName("rasouli");
			Address address = new Address();
			address.setCity("tehran");
			address.setCountry("iran");
			person.setAddress(address);

			// write hash
			Map<byte [], byte []> mappedHash =  mapper.toHash(person);
			hashOps.putAll(key, mappedHash);
			System.out.println("Put person: " + person.toString());
	
			// load hash
			Map<byte [], byte []> loadedHah =  hashOps.entries(key);
			Person fromHash = (Person)mapper.fromHash(loadedHah);
			System.out.println("load person : " + fromHash.toString());
	
		};

	}
	
	@Bean
	@Order(5)
	public ApplicationRunner  runnerJacksonHashMappingDemo () {
		return (args) -> {

			String key = "person:v2:me";
			Person person=  new Person();
			person.setFName("farhad");
			person.setLName("rasouli");
			Address address = new Address();
			address.setCity("tehran");
			address.setCountry("iran");
			person.setAddress(address);

			// write hash
			Jackson2HashMapper mapper = new Jackson2HashMapper(false);
			Map<String, Object> hashedMap = mapper.toHash(person);
			redisTemplate.opsForHash().putAll(key, hashedMap);

			// load hash
			Map<String, Object> loadedHah=   stringRedisTemplate.<String,Object>opsForHash().entries(key);
			Person fromHash = (Person)mapper.fromHash(loadedHah);
			System.out.println("load person : " + fromHash.toString());


		};
	}

}