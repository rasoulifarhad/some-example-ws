package com.example.reactivecctsusernotificationservice;

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
@RequestMapping("/notify")
public class UserNotificationController {

    @Autowired 
    private UserNotificationService userNotificationService ;

    // @PostMapping("/fraudulent-transaction")
    // public ResponseEntity<Transaction> notify(@RequestBody Transaction transaction) {

    //     log.info("Process transaction with details and notify user: {}",transaction);

    //     Transaction processed = userNotificationService.notify(transaction);

    //     if(processed.getStatus().equals(TransactionStatus.SUCCESS)) {
    //         return ResponseEntity.ok(processed) ;
    //     } else {
    //         return ResponseEntity.internalServerError().body(processed);
    //     }


    // }
    
    @PostMapping("/fraudulent-transaction")
    public Mono<Transaction> notify(@RequestBody Transaction transaction) {

        log.info("Process transaction with details and notify user: {}",transaction);

        return userNotificationService.notify(transaction);
    }
}
