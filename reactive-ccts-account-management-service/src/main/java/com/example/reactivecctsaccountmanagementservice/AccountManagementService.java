package com.example.reactivecctsaccountmanagementservice;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class AccountManagementService {
    

    @Autowired 
    private TransactionRepository transactionRepository ;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionProducer producer ;


    // public Transaction manage(Transaction transaction) {

    //     if (transaction.getStatus().equals(TransactionStatus.VALID)) {
    //         transaction.setStatus(TransactionStatus.SUCCESS);
    //         transactionRepository.save(transaction);
      
    //         User user = userRepository.findByCardId(transaction.getCardId());
    //         user.getValidTransactions().add(transaction);
    //         userRepository.save(user);
    //       }
    //       return transaction;
    // }

    public Mono<Transaction> manage(Transaction transaction) {

        return userRepository
                    .findByCardId(transaction.getCardId())
                    .map(u -> {

                        if (transaction.getStatus().equals(TransactionStatus.VALID)) {
                            List<Transaction> newList = new ArrayList<>();
                            newList.add(transaction);
                            if (Objects.isNull(u.getValidTransactions()) || u.getValidTransactions().isEmpty()) {
                                u.setValidTransactions(newList);
                            } else {
                                u.getValidTransactions().add(transaction);
                            }   
                        } 
                        log.info("User details: {}", u);
                        return u ;                       
                    })
                    .flatMap(userRepository::save)
                    .map(u -> {
                        if (transaction.getStatus().equals(TransactionStatus.VALID)) {
                            transaction.setStatus(TransactionStatus.SUCCESS);
                        }
                        return transaction;
                    })
                    .flatMap(transactionRepository::save)
                    ;
    }

    public void asyncProcess(Transaction transaction) {
        userRepository.findByCardId(transaction.getCardId())
                .map(u -> {
                    if (transaction.getStatus().equals(TransactionStatus.VALID)) {
                        List<Transaction> newList = new ArrayList<>();
                        newList.add(transaction);
                        if (Objects.isNull(u.getValidTransactions()) || u.getValidTransactions().isEmpty()) {
                            u.setValidTransactions(newList);
                        } else {
                            u.getValidTransactions().add(transaction);
                        }
                    }
                    log.info("User details: {}", u);
                    return u;
                })
                .flatMap(userRepository::save)
                .map(u -> {
                    if (transaction.getStatus().equals(TransactionStatus.VALID)) {
                        transaction.setStatus(TransactionStatus.SUCCESS);
                        producer.sendMessage(transaction);
                    }
                    return transaction;
                })
                .filter(t -> t.getStatus().equals(TransactionStatus.VALID)
                        || t.getStatus().equals(TransactionStatus.SUCCESS)
                )
                .flatMap(transactionRepository::save)
                .subscribe();
    }

}
