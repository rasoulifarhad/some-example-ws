package com.example.reactivecctsreportingservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/report")
public class ReportingController {
    

    @Autowired
    private ReportingService reportingService ;

    @PostMapping("/")
    public Mono<Transaction> report(@RequestBody Transaction transaction) {

        log.info("Process transaction with details: {}", transaction);
        
        return reportingService.report(transaction);

    }

}
