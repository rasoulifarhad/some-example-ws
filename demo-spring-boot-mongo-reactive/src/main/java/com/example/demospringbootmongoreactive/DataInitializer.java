package com.example.demospringbootmongoreactive;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final  PostRepository posts ;

    public DataInitializer(PostRepository posts) {
        this.posts = posts;
    }

    @Override
    public void run(String... args) throws Exception {

        log.info("Start data initialization ......");
        this.posts
                    .deleteAll()
                    .thenMany(
                        Flux
                            .just("Post One" , "Post two")
                            .flatMap(title -> 
                                    this.posts.save(
                                         Post.builder().title(title).content("Content of " + title).build()
                                    )
                            )
                    )
                    .log()
                    .subscribe(
                        null,
                        null,
                        () -> log.info("Done posts initialization ....")
                    )
                    ;
        
    }


    
}
