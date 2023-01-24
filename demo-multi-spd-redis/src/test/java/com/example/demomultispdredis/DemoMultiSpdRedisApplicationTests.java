package com.example.demomultispdredis;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.example.demomultispdredis.Config.MultiRedisLettuceConnectionFactory;

import reactor.core.publisher.Mono;

@SpringBootTest(properties = {
						"spring.redis.enable-multi=true",
						"spring.redis.multi.default.host=127.0.0.1",
						"spring.redis.multi.default.port=6379",
						"spring.redis.multi.test.host=127.0.0.1",
     					"spring.redis.multi.test.port=6380"
})
public class DemoMultiSpdRedisApplicationTests extends EmbededRedisAbstractIntegrationTest {

	@Autowired
	private StringRedisTemplate redisTemplate ;
	
	@Autowired
	private ReactiveStringRedisTemplate reactiveRedisTemplate;

	@Autowired
	private MultiRedisLettuceConnectionFactory multiRedisLettuceConnectionFactory;

	private void testMulti(String suffix) {

		redisTemplate.opsForValue().set("testDefault" + suffix	, "testDefault");

		multiRedisLettuceConnectionFactory.setCurrentRedis("test");

		redisTemplate.opsForValue().set("testSecond" + suffix	, "testSecond");

		assertTrue(redisTemplate.hasKey("testDefault" + suffix));
		assertFalse(redisTemplate.hasKey("testSecond" + suffix));

		multiRedisLettuceConnectionFactory.setCurrentRedis("test");
		assertFalse(redisTemplate.hasKey("testDefault" + suffix));

		multiRedisLettuceConnectionFactory.setCurrentRedis("test");
		assertTrue(redisTemplate.hasKey("testSecond" + suffix));

	}

	@Test
	public void testMultiBlock() {
		testMulti("");
	}

	@Test
	public void testMultiBlockMultiThread() throws InterruptedException {	

		Thread threads[] = new Thread[50] ;
		AtomicBoolean result = new AtomicBoolean(true);

		for (int i = 0; i < threads.length; i++) {
			int finalI = i ;
			threads[i] = new Thread(() ->{
				try {
					testMulti("" + finalI);
				} catch (Exception e) {
					e.printStackTrace();
					result.set(false);
				}
			});
		}

		for (int i = 0; i < threads.length; i++) {
			threads[i].start();
		}

		for (int i = 0; i < threads.length; i++) {
			threads[i].join();
		}

		assertTrue(result.get());
	}


	private Mono<Boolean> reactiveMulti(String suffix) {

		return reactiveRedisTemplate
					.opsForValue()
					.set("testReactiveDefault" + suffix, "testReactiveDefault")
					.flatMap(b -> {
						multiRedisLettuceConnectionFactory.setCurrentRedis("test");
						return reactiveRedisTemplate.opsForValue()
													.set("testReactiveSecond" + suffix, "testReactiveSecond");
					})
					.flatMap(b -> {
						return reactiveRedisTemplate.hasKey("testReactiveDefault" + suffix);
					})
					.map(b -> {
						assertTrue(b);
						System.out.println(Thread.currentThread().getName());
						return b;
					})
					.flatMap(b -> {
						return reactiveRedisTemplate.hasKey("testReactiveSecond" + suffix);
					})
					.map(b -> {
						assertFalse(b);
						System.out.println(Thread.currentThread().getName());
						return b;
					})
					.flatMap(b -> {
						multiRedisLettuceConnectionFactory.setCurrentRedis("test");
						return reactiveRedisTemplate.hasKey("testReactiveDefault" + suffix);
					})
					.map(b -> {
						assertFalse(b);
						System.out.println(Thread.currentThread().getName());
						return b;
					})
					.flatMap(b -> {
						multiRedisLettuceConnectionFactory.setCurrentRedis("test");
						return reactiveRedisTemplate.hasKey("testReactiveSecond" + suffix);
					})
					.map(b -> {
						assertTrue(b);
						return b;
					});	
	}

	@Test
    public void testMultiReactive() throws InterruptedException {
		
		for (int i = 0; i < 10000; i++) {
			reactiveMulti("" + i).subscribe(System.out::println);
		}
 		TimeUnit.SECONDS.sleep(10);    
	}
}
