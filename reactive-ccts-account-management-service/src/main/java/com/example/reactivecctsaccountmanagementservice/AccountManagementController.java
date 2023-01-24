package com.example.reactivecctsaccountmanagementservice;

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
@RequestMapping("/banking")
public class AccountManagementController {
    

    @Autowired
    private AccountManagementService accountManagementService ;

    // @PostMapping("/process")
    // public ResponseEntity<Transaction> manage(@RequestBody Transaction transaction) {

    //     log.info("Process transaction with details: {}", transaction);
    //     Transaction processed = accountManagementService.manage(transaction);

    //     if(processed.getStatus().equals(TransactionStatus.SUCCESS)) {
    //         return ResponseEntity.ok(processed);
    //     } else {
    //         return ResponseEntity.internalServerError().body(processed);
    //     }
    // }

    @PostMapping("/process")
    public Mono<Transaction> manage(@RequestBody Transaction transaction) {

        log.info("Process transaction with details: {}", transaction);
        return accountManagementService.manage(transaction);

    }

}
