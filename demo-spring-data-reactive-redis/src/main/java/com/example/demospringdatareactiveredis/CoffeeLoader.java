package com.example.demospringdatareactiveredis;

import java.util.UUID;

import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;


@Component
@Slf4j
public class CoffeeLoader implements CommandLineRunner{

    private final ReactiveRedisConnectionFactory connectionFactory ;
    private final ReactiveRedisOperations<String,Coffee> coffeeOps ;

    

    public CoffeeLoader(ReactiveRedisConnectionFactory connectionFactory,
            ReactiveRedisOperations<String, Coffee> coffeeOps) {
        this.connectionFactory = connectionFactory;
        this.coffeeOps = coffeeOps;
    }



    @Override
    public void run(String... args) throws Exception {
        
        connectionFactory.getReactiveConnection().serverCommands().flushAll().thenMany(
                                    Flux.just("Jet Black Redis","Darth Redis","Black Alert Redis")
                                                                    .map(name -> new Coffee(UUID.randomUUID().toString(),name))
                                                                    .flatMap(coffee -> coffeeOps.opsForValue().set(coffee.getId(), coffee)))
                                    .thenMany(
                                        coffeeOps
                                                .keys("*")
                                                .flatMap(coffeeOps.opsForValue()::get)

                                    )
                                    .subscribe(coffee  -> log.info("{}" , coffee) )
                                    ;

    }
    
}
