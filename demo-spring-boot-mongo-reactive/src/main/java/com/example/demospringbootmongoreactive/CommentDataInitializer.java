package com.example.demospringbootmongoreactive;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CommentDataInitializer  implements CommandLineRunner {
    
    final CommentRepository commentRepository;

        public CommentDataInitializer(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }


    @Override
    public void run(String... args) throws Exception {
        log.info("start comment data initialization ...");
        
    }
    
}
