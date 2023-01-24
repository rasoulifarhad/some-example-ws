package com.example.democloudstreamwebfux.cloudstreammultioutput;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest(classes =  {Application.class,Config.class},
                properties = {
                    "spring.cloud.function.definition=intSupplier;scatter",
                    "spring.cloud.stream.defaultBinder=rabbit",
                    "spring.cloud.stream.function.bindings.intSupplier-out-0.destination=int-topic",
                    "spring.cloud.stream.function.bindings.scatter-in-0.destination=int-topic",
                    "spring.cloud.stream.function.bindings.scatter-in-0.group=scatter-grp",
                    "spring.cloud.stream.function.bindings.scatter-out-0.destination=even-topic",
                    "spring.cloud.stream.function.bindings.scatter-out-1.destination=odd-topic"
                }
)

@Import({TestChannelBinderConfiguration.class})
@Slf4j
public class SecondApplicationTest {
    
   @Autowired
    private InputDestination inputDestination ;

    @Autowired
    private OutputDestination outputDestination ;


    @Test
    public void testSingleInputMultipleOutput() {


            for (int  i = 0; i < 10 ; i++) {

                Message<byte[]> inputMessage =  MessageBuilder.withPayload(String.valueOf(i).getBytes()).build();
                inputDestination.send(inputMessage,"scatter-in-0");
            }

            int counter = 0;

            for (int i = 0; i < 5; i++) {
                Message<byte[]>  even = outputDestination.receive(0,"scatter-out-0" );
                
                log.info("Even received: {}",new String(even.getPayload()));
                // log.info("Counter {}",counter++);
                assertThat(even.getPayload()).isEqualTo(("Even: " + String.valueOf(counter++)).getBytes());

                Message<byte[]>  odd = outputDestination.receive(0, "scatter-out-1");
                log.info("Odd received: {}",new String(odd.getPayload()));
                // log.info("Counter {}",counter++);
                assertThat(odd.getPayload()).isEqualTo(("Odd: " + String.valueOf(counter++)).getBytes());
            }


    }

    // @Test
    // public void testSingleInputMultipleOutput() {

    //     try (ConfigurableApplicationContext context = new SpringApplicationBuilder(
    //                                         TestChannelBinderConfiguration.getCompleteConfiguration(Application.class))
    //                                                 .run("--spring.cloud.functiondefinition=scatter")) {

    //         InputDestination inputDestination = context.getBean(InputDestination.class);
            
    //         OutputDestination outputDestination = context.getBean(OutputDestination.class);

    //         for (int  i = 0; i < 10 ; i++) {
    //             inputDestination.send(MessageBuilder.withPayload(String.valueOf(i).getBytes()).build(),"int-topic");
    //         }


    //         int counter = 0;

    //         for (int i = 0; i < 10; i++) {
    //             Message<byte[]>  even = outputDestination.receive(0, 0);

    //             assertThat(even.getPayload()).isEqualTo(("Even: " + String.valueOf(counter++)).getBytes());

    //             Message<byte[]>  odd = outputDestination.receive(0, 1);
    //             assertThat(odd.getPayload()).isEqualTo(("Odd: " + String.valueOf(counter++)).getBytes());
    //         }

    //     } 

    // }

    
}

