package com.example.reactivecctsusernotificationservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class UserNotificationService {

    @Autowired
    private TransactionRepository transactionRepository ;

    @Autowired 
    private UserRepository userRepository ;

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private TransactionProducer producer;

    // public Transaction notify(Transaction transaction) {

    //     if(transaction.getStatus().equals(TransactionStatus.FRAUDULENT)) {

    //         User user = userRepository.findByCardId(transaction.getCardId());

    //         //notify user by sending email
    //         SimpleMailMessage message = new SimpleMailMessage() ;
    //         message.setFrom("noreply@baeldung.com");
    //         message.setTo(user.getEmail());
    //         message.setSubject("Fraudulent transaction attempt from your card");
    //         message.setText("An attempt has been made to pay " + transaction.getStoreName()
    //                 + " from card " + transaction.getCardId() + " in the country "
    //                 + transaction.getTransactionLocation() + "." +
    //                 " Please report to your bank or block your card.");
    //         emailSender.send(message);

    //         transaction.setStatus(TransactionStatus.FRAUDULENT_NOTIFY_SUCCESS);

    //     } else {
    //         transaction.setStatus(TransactionStatus.FRAUDULENT_NOTIFY_FAILURE);
    //     }
    //     return transactionRepository.save(transaction);
    // }

    public Mono<Transaction> notify(Transaction transaction) {

        return userRepository
                .findByCardId(transaction.getCardId())
                .map(u -> {
                        if(transaction.getStatus().equals(TransactionStatus.FRAUDULENT)) {

                            //notify user by sending email
                            SimpleMailMessage message = new SimpleMailMessage() ;
                            message.setFrom("noreply@baeldung.com");
                            message.setTo(u.getEmail());
                            message.setSubject("Fraudulent transaction attempt from your card");
                            message.setText("An attempt has been made to pay " + transaction.getStoreName()
                                    + " from card " + transaction.getCardId() + " in the country "
                                    + transaction.getTransactionLocation() + "." +
                                    " Please report to your bank or block your card.");
                            emailSender.send(message);
                            transaction.setStatus(TransactionStatus.FRAUDULENT_NOTIFY_SUCCESS);
                        } else {
                            transaction.setStatus(TransactionStatus.FRAUDULENT_NOTIFY_FAILURE);
                        }
                        return transaction;
                })
                .onErrorReturn(transaction)
                .flatMap(transactionRepository::save);
    }

    public void asyncProcess(Transaction transaction) {
        userRepository.findByCardId(transaction.getCardId())
                .map(u -> {
                    if (transaction.getStatus().equals(TransactionStatus.FRAUDULENT)) {

                        try {
                            // Notify user by sending email
                            SimpleMailMessage message = new SimpleMailMessage();
                            message.setFrom("noreply@baeldung.com");
                            message.setTo(u.getEmail());
                            message.setSubject("Fraudulent transaction attempt from your card");
                            message.setText("An attempt has been made to pay " + transaction.getStoreName()
                                    + " from card " + transaction.getCardId() + " in the country "
                                    + transaction.getTransactionLocation() + "." +
                                    " Please report to your bank or block your card.");
                            emailSender.send(message);
                            transaction.setStatus(TransactionStatus.FRAUDULENT_NOTIFY_SUCCESS);
                        } catch (MailException e) {
                            transaction.setStatus(TransactionStatus.FRAUDULENT_NOTIFY_FAILURE);
                        }
                    }
                    return transaction;
                })
                .onErrorReturn(transaction)
                .filter(t -> t.getStatus().equals(TransactionStatus.FRAUDULENT)
                        || t.getStatus().equals(TransactionStatus.FRAUDULENT_NOTIFY_SUCCESS)
                        || t.getStatus().equals(TransactionStatus.FRAUDULENT_NOTIFY_FAILURE)
                )
                .map(t -> {
                    producer.sendMessage(t);
                    return t;
                })
                .flatMap(transactionRepository::save)
                .subscribe();
    }
    
}
