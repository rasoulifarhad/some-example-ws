package com.example.democloudstreamwebfux.intergration.directChannel;

import org.springframework.integration.MessageRejectedException;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public  class PremiumReader implements MessageHandler {

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {

        Object payload = message.getPayload() ;
        if (payload instanceof Book){
            receiveAndAcknowledge((Book)payload);
        } else {
            throw new MessageRejectedException(message, "Unknown data type has been received.");
        }

    }

    private void receiveAndAcknowledge(Book book) {
        log.info("Hi Librarian, this i {}. Received book - {}",System.identityHashCode(this),book.toString());
    }
}