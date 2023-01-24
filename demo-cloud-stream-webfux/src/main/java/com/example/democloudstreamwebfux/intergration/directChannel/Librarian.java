package com.example.democloudstreamwebfux.intergration.directChannel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public  class Librarian {

    @Autowired
    @Qualifier("bookChannel")
    private DirectChannel channel ;

    public void sendPremiumReaders(Book book) {

        log.info("Dear Premium Reader, Just Arrived - " + book.toString());
        channel.send(MessageBuilder.withPayload(book).build());
    }
}
