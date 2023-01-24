package com.example.demospringbootmongoreactive;

import static org.springframework.web.reactive.function.server.ServerResponse.created;
import static org.springframework.web.reactive.function.server.ServerResponse.noContent;
import static org.springframework.web.reactive.function.server.ServerResponse.notFound;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import java.net.URI;
import java.time.Duration;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class PostHandler {
    
    private final PostRepository posts ;

    public PostHandler(PostRepository posts) {
        this.posts = posts ;
    }

    public Mono<ServerResponse> all(ServerRequest request) {
        return ok().body(this.posts.findAll(),Post.class);
    }

    public Mono<ServerResponse> stream(ServerRequest request) {
        return ok().body(
                        Flux.interval(Duration.ofSeconds(30L))
                            .flatMap(s -> this.posts.findAll()), Post.class
                    )
                   
                   ;
    }


    public Mono<ServerResponse>  create(ServerRequest request) {

        return request
                        .bodyToMono(Post.class)
                        .flatMap(post ->  this.posts.save(post) )
                        .flatMap(p -> created(URI.create("/posts/" + p.getSlug())).build());
    }

    public Mono<ServerResponse>  get(ServerRequest request) {

        return this.posts
                            .findBySlug(request.pathVariable("slug"))
                            .flatMap(post -> ok().body(BodyInserters.fromValue(post)))
                            .switchIfEmpty(notFound().build());

    }

    public Mono<ServerResponse>  update(ServerRequest request ) {
        return
                Mono.zip(
                    (data) -> {
                        Post p = (Post) data[0] ;
                        Post p2 = (Post) data[1];
                        p.setTitle(p2.getTitle());
                        p.setContent(p2.getContent());
                        return p ;
                },
                this.posts.findById(request.pathVariable("slug")),
                request.bodyToMono(Post.class)
                )
                .cast(Post.class)
                .flatMap(post -> this.posts.save(post) )
                .flatMap(post -> noContent().build())
                ;
    }

    public Mono<ServerResponse>  delete(ServerRequest request) {

        return this.posts
                            .findBySlug(request.pathVariable("slug"))
                            .flatMap(post -> noContent().build())
                            .switchIfEmpty(notFound().build());

    }



}
