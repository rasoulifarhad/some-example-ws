package com.example.reactivecctsusernotificationservice;

import java.util.function.Consumer;

import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class TransactionConsumer {
    

    public Consumer<Transaction> consumeTransaction(UserNotificationService userNotificationService) {

        return userNotificationService::asyncProcess;

    }
}
