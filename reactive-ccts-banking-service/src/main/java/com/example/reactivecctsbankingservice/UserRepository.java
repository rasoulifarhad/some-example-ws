package com.example.reactivecctsbankingservice;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveMongoRepository<User,String> /*MongoRepository<User,String>*/ {

    // User findByCardId(String cardId);
    
    Mono<User> findByCardId(String cardId);
}
