package com.example.democloudstreamwebfux.externalSystems;

import org.springframework.stereotype.Service;

import io.netty.util.internal.ThreadLocalRandom;

// fake service
@Service
public class ExternalService {
    
    public boolean isHealthy() {
        // we are check health of service
        return ThreadLocalRandom.current().nextBoolean() ;
    }

    public void doWork(String message) {
        //
    }
}
