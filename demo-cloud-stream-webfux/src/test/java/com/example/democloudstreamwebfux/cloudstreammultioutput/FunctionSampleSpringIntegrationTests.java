package com.example.democloudstreamwebfux.cloudstreammultioutput;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.Function;

import org.junit.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FunctionSampleSpringIntegrationTests {
    


    @Test
    public void testIntegrationFlowAsFunctionForCloudStream() {

        try (ConfigurableApplicationContext context = new  SpringApplicationBuilder(
                                                                TestChannelBinderConfiguration.getCompleteConfiguration(
                                                                    FunctionSampleSpringIntegration.class
                                                                )
                                                            ).run("--spring.cloud.function.definition=uppercase") ) {

            InputDestination inputDestination = context.getBean(InputDestination.class) ;
            OutputDestination outputDestination = context.getBean(OutputDestination.class);

            Message<byte[]> inputMessage = MessageBuilder.withPayload("Hello".getBytes()).build();
            inputDestination.send(inputMessage, "uppercase-in-0");

            Message<byte[]> outputMessage =  outputDestination.receive(0, "uppercase-out-0");

            log.info("============> {}",new String(outputMessage.getPayload()));
            assertThat(outputMessage).isNotNull();
            assertThat(outputMessage.getPayload()).isEqualTo("HELLO".getBytes());
            
        } 
    }

    public interface MessageFunction extends Function<Message<String>,Message<String>> {

    }


    @EnableAutoConfiguration
    public static class FunctionSampleSpringIntegration {

        @Bean
        public IntegrationFlow uppercaseFlow() {
            return IntegrationFlows
                                .from(MessageFunction.class ,gateway -> gateway.beanName("uppercase"))
                                .<String,String>transform(String::toUpperCase)
                                .<String>logAndReply(m -> {
                                    log.info("{}",m);
                                    return m;
                                })
                                ;
                                
        }

                
    
    }

}
