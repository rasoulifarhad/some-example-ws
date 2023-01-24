package com.example.demospringbootmongoreactive;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.ServerResponse.*;

import java.net.URI;

@Component
public class CommentHandler {

    private final CommentRepository comments ;

    public CommentHandler(CommentRepository comments) {
        this.comments = comments;
    }

    public Mono<ServerResponse> all(ServerRequest request){
        return ok().body(this.comments.findAll(),Comment.class);

    }

    public Mono<ServerResponse> create(ServerRequest request ) {
        return request
                    .bodyToMono(Comment.class)
                    .flatMap(comment -> this.comments.save(comment) )
                    .flatMap(comment -> created(
                                URI
                                    .create("/posts" + request.pathVariable("slug") + "/comments/" + comment.getId() ))
                                    .build());
    }

    public Mono<ServerResponse> get(ServerRequest request ) {


        return
            this.comments
                        .findById(request.pathVariable("commitId"))     
                        .flatMap(comment -> ok().body(Mono.just(comment),Comment.class) )
                        .switchIfEmpty(notFound().build())       ;
    }
    
    public Mono<ServerResponse> update(ServerRequest request ) {
        return 
                Mono 
                    .zip (
                       (data) -> {
                            Comment p = (Comment) data[0];
                            Comment p2 = (Comment) data[1];
                            p.setContent(p2.getContent());
                            return p ;
                       }  ,
                       this.comments.findById(request.pathVariable("commentId")),
                       request.bodyToMono(Comment.class)
                    )
                    .cast(Comment.class)
                    .flatMap(comment -> this.comments.save(comment))
                    .flatMap(comment -> noContent().build() )
                    ;

    }
    
    public Mono<ServerResponse> delete(ServerRequest request ) {
        return noContent().build(this.comments.deleteById(request.pathVariable("commentId")));

    }
    
    
}
