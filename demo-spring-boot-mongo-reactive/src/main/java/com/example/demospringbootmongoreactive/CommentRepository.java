package com.example.demospringbootmongoreactive;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface CommentRepository extends ReactiveMongoRepository<Comment,String>{
    
}
