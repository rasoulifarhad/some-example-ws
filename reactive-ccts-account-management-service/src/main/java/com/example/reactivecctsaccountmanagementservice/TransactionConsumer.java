package com.example.reactivecctsaccountmanagementservice;

import java.util.function.Consumer;

import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class TransactionConsumer {
    

    public Consumer<Transaction> consumeTransaction(AccountManagementService accountManagementService) {

        return accountManagementService::asyncProcess;

    }

    
}
