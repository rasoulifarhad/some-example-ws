package com.example.reactivecctsbankingservice;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class TransactionService {

    private static final String USER_NOTIFICATION_SERVICE_URL = "http://localhost:8081/notify/fraudulent-transaction";

    private static final String REPORTING_SERVICE_URL = "http://localhost:8081/report";

    private static final String ACCOUNT_MANAGER_SERVICE_URL = "http://localhost:8081/banking/process";

    @Autowired
    private TransactionRepository transactionRepository ;

    @Autowired
    private UserRepository userRepository ;

    @Autowired
    private TransactionProducer producer ;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private WebClient webClient;



    // public Transaction process(Transaction transaction) {

    //     Transaction firstProcessed;
    //     Transaction secondProcessed = null;
    //     transactionRepository.save(transaction);
    //     if (transaction.getStatus().equals(TransactionStatus.INITIATED)) {

    //         User user = userRepository.findByCardId(transaction.getCardId());

    //         // Check whether the card details are valid or not
    //         if (Objects.isNull(user)) {
    //             transaction.setStatus(TransactionStatus.CARD_INVALID);
    //         }

    //         // Check whether the account is blocked or not
    //         else if (user.isAccountLocked()) {
    //             transaction.setStatus(TransactionStatus.ACCOUNT_BLOCKED);
    //         }

    //         else {

    //             // Check if it's a valid transaction or not. The Transaction
    //             // would be considered valid if it has been requested from 
    //             // the same home country of the user, else will be considered
    //             // as fraudulent
    //             if (user.getHomeCountry().equalsIgnoreCase(transaction
    //                                                     .getTransactionLocation())) {

    //                 transaction.setStatus(TransactionStatus.VALID);

    //                 // Call Reporting Service to report valid transaction to bank
    //                 // and deduct amount if funds available
    //                 firstProcessed = restTemplate.postForObject(REPORTING_SERVICE_URL,
    //                                                             transaction,
    //                                                             Transaction.class);

    //                 // Call Account Manager service to process the transaction
    //                 // and send the money
    //                 if (Objects.nonNull(firstProcessed)) {
    //                     secondProcessed = restTemplate.postForObject(ACCOUNT_MANAGER_SERVICE_URL,
    //                                                                 firstProcessed,
    //                                                                 Transaction.class);
    //                 }

    //                 if (Objects.nonNull(secondProcessed)) {
    //                     transaction = secondProcessed;
    //                 }
    //             } else {

    //                 transaction.setStatus(TransactionStatus.FRAUDULENT);

    //                 // Call User Notification service to notify for a 
    //                 // fraudulent transaction attempt from the User's card
    //                 firstProcessed = restTemplate.postForObject(USER_NOTIFICATION_SERVICE_URL,
    //                                                             transaction,
    //                                                             Transaction.class);

    //                 // Call Reporting Service to notify bank that
    //                 // there has been an attempt for fraudulent transaction
    //                 // and if this attempt exceeds 3 times then auto-block 
    //                 // the card and account  
    //                 if (Objects.nonNull(firstProcessed)) {
    //                     secondProcessed = restTemplate.postForObject(REPORTING_SERVICE_URL,
    //                                                                 firstProcessed,
    //                                                                 Transaction.class);
    //                 }

    //                 if (Objects.nonNull(secondProcessed)) {
    //                     transaction = secondProcessed;
    //                 }
    //             }
    //         }
    //     } else {

    //         // For any other case, the transaction will be considered failure
    //         transaction.setStatus(TransactionStatus.FAILURE);
    //     }
    //     return transactionRepository.save(transaction);

    // }

    @Transactional
    public Mono<Transaction> process(Transaction transaction) {

        return Mono.just(transaction)
                .flatMap(transactionRepository::save)
                .flatMap(t -> userRepository.findByCardId(t.getCardId())
                        .map(u -> {
                            log.info("User details: {}", u);
                            if (t.getStatus().equals(TransactionStatus.INITIATED)) {
                                // Check whether the card details are valid or not
                                if (Objects.isNull(u)) {
                                    t.setStatus(TransactionStatus.CARD_INVALID);
                                }

                                // Check whether the account is blocked or not
                                else if (u.isAccountLocked()) {
                                    t.setStatus(TransactionStatus.ACCOUNT_BLOCKED);
                                }

                                else {
                                    // Check if it's a valid transaction or not. The Transaction would be considered valid
                                    // if it has been requested from the same home country of the user, else will be considered
                                    // as fraudulent
                                    if (u.getHomeCountry().equalsIgnoreCase(t.getTransactionLocation())) {
                                        t.setStatus(TransactionStatus.VALID);

                                        // Call Reporting Service to report valid transaction to bank and deduct amount if funds available
                                        return webClient.post()
                                                .uri(REPORTING_SERVICE_URL)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .body(BodyInserters.fromValue(t))
                                                .retrieve()
                                                .bodyToMono(Transaction.class)
                                                .zipWhen(t1 ->
                                                                // Call Account Manager service to process the transaction and send the money
                                                                webClient.post()
                                                                    .uri(ACCOUNT_MANAGER_SERVICE_URL)
                                                                    .contentType(MediaType.APPLICATION_JSON)
                                                                    .body(BodyInserters.fromValue(t))
                                                                    .retrieve()
                                                                    .bodyToMono(Transaction.class)
                                                                    .log(),
                                                                    (t1, t2) -> t2
                                                )
                                                .log()
                                                .share()
                                                .block();
                                    } else {
                                        t.setStatus(TransactionStatus.FRAUDULENT);

                                        // Call User Notification service to notify for a fraudulent transaction
                                        // attempt from the User's card
                                        return webClient.post()
                                                .uri(USER_NOTIFICATION_SERVICE_URL)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .body(BodyInserters.fromValue(t))
                                                .retrieve()
                                                .bodyToMono(Transaction.class)
                                                .zipWhen(t1 ->
                                                                // Call Reporting Service to notify bank that there has been an attempt for fraudulent transaction
                                                                // and if this attempt exceeds 3 times then auto-block the card and account
                                                                webClient.post()
                                                                    .uri(REPORTING_SERVICE_URL)
                                                                    .contentType(MediaType.APPLICATION_JSON)
                                                                    .body(BodyInserters.fromValue(t))
                                                                    .retrieve()
                                                                    .bodyToMono(Transaction.class)
                                                                    .log(),
                                                                    (t1, t2) -> t2
                                                )
                                                .log()
                                                .share()
                                                .block();
                                    }
                                }
                            } else {
                                // For any other case, the transaction will be considered failure
                                t.setStatus(TransactionStatus.FAILURE);
                            }
                            return t;
                        }));
    }

    public void asyncProcess(Transaction transaction) {
        userRepository.findByCardId(transaction.getCardId())
                .map(u -> {
                    if (transaction.getStatus().equals(TransactionStatus.INITIATED)) {
                        log.info("Consumed message for processing: {}", transaction);
                        log.info("User details: {}", u);
                        // Check whether the card details are valid or not
                        if (Objects.isNull(u)) {
                            transaction.setStatus(TransactionStatus.CARD_INVALID);
                        }

                        // Check whether the account is blocked or not
                        else if (u.isAccountLocked()) {
                            transaction.setStatus(TransactionStatus.ACCOUNT_BLOCKED);
                        }

                        else {
                            // Check if it's a valid transaction or not. The Transaction would be considered valid
                            // if it has been requested from the same home country of the user, else will be considered
                            // as fraudulent
                            if (u.getHomeCountry().equalsIgnoreCase(transaction.getTransactionLocation())) {
                                transaction.setStatus(TransactionStatus.VALID);
                            } else {
                                transaction.setStatus(TransactionStatus.FRAUDULENT);
                            }
                        }
                        producer.sendMessage(transaction);
                    }
                    return transaction;
                })
                .filter(t -> t.getStatus().equals(TransactionStatus.VALID)
                        || t.getStatus().equals(TransactionStatus.FRAUDULENT)
                        || t.getStatus().equals(TransactionStatus.CARD_INVALID)
                        || t.getStatus().equals(TransactionStatus.ACCOUNT_BLOCKED)
                )
                .flatMap(transactionRepository::save)
                .subscribe();
    }
}
