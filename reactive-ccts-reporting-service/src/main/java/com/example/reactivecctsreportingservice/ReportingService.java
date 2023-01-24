package com.example.reactivecctsreportingservice;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class ReportingService {
    

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository ;

    @Autowired
    private TransactionProducer producer ;

    // public Transaction report(Transaction transaction) {

    //     if(transaction.getStatus().equals( TransactionStatus.FRAUDULENT_NOTIFY_SUCCESS)
    //             || transaction.getStatus().equals(TransactionStatus.FRAUDULENT_NOTIFY_FAILURE)) {

    //         // Report the User's account and take automatic action against
    //         // User's account or card

    //         User user = userRepository.findByCardId(transaction.getCardId());
    //         user.setFraudulentActivityAttemptCount(
    //                     user.getFraudulentActivityAttemptCount() + 1 
    //         );  
    //         user.setAccountLocked(user.getFraudulentActivityAttemptCount() > 3);
    //         user.getFraudulentTransactions().add(transaction);
    //         userRepository.save(user) ;

    //         transaction.setStatus(user.isAccountLocked()
    //                                     ? TransactionStatus.ACCOUNT_BLOCKED
    //                                     : TransactionStatus.FAILURE);  

    //     }
    //     return transactionRepository.save(transaction);
    // }

    public Mono<Transaction> report(Transaction transaction) {

        return userRepository
                    .findByCardId(transaction.getCardId())
                    .map(u -> {
                        if( transaction.getStatus().equals( TransactionStatus.FRAUDULENT)
                            || transaction.getStatus().equals( TransactionStatus.FRAUDULENT_NOTIFY_SUCCESS) 
                            || transaction.getStatus().equals(TransactionStatus.FRAUDULENT_NOTIFY_FAILURE)) {
        
                            // Report the User's account and take automatic action against User's account or card
                                        
                            u.setFraudulentActivityAttemptCount(
                                        u.getFraudulentActivityAttemptCount() + 1 
                            );  
                            u.setAccountLocked(u.getFraudulentActivityAttemptCount() > 3);
                            List<Transaction> newList = new ArrayList<>();
                            newList.add(transaction);
                            if (Objects.isNull(u.getFraudulentTransactions()) || u.getFraudulentTransactions().isEmpty()) {
                                u.setFraudulentTransactions(newList);
                            } else {
                                u.getFraudulentTransactions().add(transaction);
                            }            
            
                        }
                        log.info("User details: {}", u);
                        return u ;
                            
                    })
                    .flatMap(userRepository::save)
                    .map(u -> {
                        if (!transaction.getStatus().equals(TransactionStatus.VALID)) {
                            transaction.setStatus(u.isAccountLocked()
                                    ? TransactionStatus.ACCOUNT_BLOCKED : TransactionStatus.FAILURE);
                        }
                        return transaction;
                    })
                    .flatMap(transactionRepository::save);
    }


    public void asyncProcess(Transaction transaction) {
        userRepository.findByCardId(transaction.getCardId())
                .map(u -> {
                    if (transaction.getStatus().equals(TransactionStatus.FRAUDULENT)
                            || transaction.getStatus().equals(TransactionStatus.FRAUDULENT_NOTIFY_SUCCESS)
                            || transaction.getStatus().equals(TransactionStatus.FRAUDULENT_NOTIFY_FAILURE)) {

                        // Report the User's account and take automatic action against User's account or card
                        u.setFraudulentActivityAttemptCount(u.getFraudulentActivityAttemptCount() + 1);
                        u.setAccountLocked(u.getFraudulentActivityAttemptCount() > 3);
                        List<Transaction> newList = new ArrayList<>();
                        newList.add(transaction);
                        if (Objects.isNull(u.getFraudulentTransactions()) || u.getFraudulentTransactions().isEmpty()) {
                            u.setFraudulentTransactions(newList);
                        } else {
                            u.getFraudulentTransactions().add(transaction);
                        }
                    }
                    log.info("User details: {}", u);
                    return u;
                })
                .flatMap(userRepository::save)
                .map(u -> {
                    if (!transaction.getStatus().equals(TransactionStatus.VALID)) {
                        transaction.setStatus(u.isAccountLocked()
                                ? TransactionStatus.ACCOUNT_BLOCKED : TransactionStatus.FAILURE);
                        producer.sendMessage(transaction);
                    }
                    return transaction;
                })
                .filter(t -> t.getStatus().equals(TransactionStatus.FAILURE)
                        || t.getStatus().equals(TransactionStatus.ACCOUNT_BLOCKED)
                )
                .flatMap(transactionRepository::save)
                .subscribe();
    }
}
