package com.example.demospringbootmongoreactive;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import reactor.core.publisher.Mono;

public interface PostRepository extends ReactiveMongoRepository<Post,String> {
    
    Mono<Post> findBySlug(String slug);
}
