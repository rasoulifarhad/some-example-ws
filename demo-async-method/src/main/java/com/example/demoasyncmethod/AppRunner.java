package com.example.demoasyncmethod;

import java.util.concurrent.CompletableFuture;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AppRunner implements CommandLineRunner{

    private final GitHubLookupService gitHubLookupService  ;

    public AppRunner(GitHubLookupService gitHubLookupService){
        this.gitHubLookupService = gitHubLookupService ;
    }

    @Override
    public void run(String... args) throws Exception {

        long start = System.currentTimeMillis() ;

        CompletableFuture<User> page1 = gitHubLookupService.findUser("PivotalSoftware");

        CompletableFuture<User> page2 = gitHubLookupService.findUser("CloudFoundry");
        
        CompletableFuture<User> page3 = gitHubLookupService.findUser("Spring-Projects");        


        CompletableFuture.allOf(page1,page2,page3).join() ;

        log.info("Elapsed time: {} ", (System.currentTimeMillis() - start));
        log.info("---> {} ", page1.get());
       
        log.info("---> {} ", page2.get());
        log.info("---> {} ", page3.get());
    }
    
}
