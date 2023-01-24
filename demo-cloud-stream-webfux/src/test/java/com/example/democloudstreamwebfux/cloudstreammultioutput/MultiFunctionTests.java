package com.example.democloudstreamwebfux.cloudstreammultioutput;

import java.util.function.Function;

import org.junit.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import static org.assertj.core.api.Assertions.assertThat;


public class MultiFunctionTests {


    @Test
    public void  testMultipleFunction() {

        try ( ConfigurableApplicationContext context = new SpringApplicationBuilder( 
                                        TestChannelBinderConfiguration.getCompleteConfiguration(
                                            MultiFunctionApplication.class
                                        ))
                                        .run("--spring.cloud.function.definition=uppercase;reverse")) {

            InputDestination inputDestinationc = context.getBean(InputDestination.class);
            OutputDestination outputDestination = context.getBean(OutputDestination.class);

            Message<byte[]> inputMessage =  MessageBuilder.withPayload("Hello".getBytes()).build();
            inputDestinationc.send(inputMessage, "uppercase-in-0");
            
            inputDestinationc.send(inputMessage, "reverse-in-0");

            Message<byte[]> outputMessage = outputDestination.receive(0, "uppercase-out-0");
            assertThat(outputMessage.getPayload()).isEqualTo("HELLO".getBytes());

            outputMessage = outputDestination.receive(0, "reverse-out-0");
            assertThat(outputMessage.getPayload()).isEqualTo("olleH".getBytes());

        } 
    }
    
    @SpringBootApplication
    public static class MultiFunctionApplication {


        @Bean
        public Function<String,String> uppercase() {
            return val -> val.toUpperCase() ; 
        }


        @Bean
        public Function<String,String> reverse() {

            return val -> new StringBuilder(val).reverse().toString();
        }
    }

}
