package com.example.reactivecctsreportingservice;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TransactionProducer {
    
    @Autowired
    private StreamBridge  streamBridge ;

    public void sendMessage(Transaction transaction) {

        Message<Transaction> msg = MessageBuilder
                                        .withPayload(transaction)
                                        .setHeader(KafkaHeaders.MESSAGE_KEY,transaction.getTransactionId().getBytes(StandardCharsets.UTF_8))
                                        .build() ;

        log.info("Transaction processed to dispatch: {}; Message dispatch successful: {}",
                            msg,
                            streamBridge.send("transaction-out-0", msg));

    }
}
