package com.example.democloudstreamwebfux.functionRouter.anotherroutefrom;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties.Streams;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;

import lombok.extern.slf4j.Slf4j;


/**
 * echo 'Hello Even' | curl -isS --location --request  POST http://localhost:8080/process/even  -H "Content-Type: application/json" -d @- ;echo
 * echo 'Hello Odd' | curl -isS --location --request  POST http://localhost:8080/process/odd  -H "Content-Type: application/json" -d @- ;echo
 * 
 * 
 */

@EnableAutoConfiguration
@Slf4j
@RestController
public class AnotherRoutingFromApplication {
    
    public static void main(String[] args) throws Exception {

        new SpringApplicationBuilder()
                .sources(AnotherRoutingFromApplication.class,Resource.class)
                .bannerMode(Banner.Mode.OFF)
                .properties("spring.config.name=functionRouter")
                .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/functionRouter/anotherroutefrom/")
                // .properties("management.endpoints.web.exposure.include='*'")
                // .properties("spring.cloude.stream.source=toStream")
                .web(WebApplicationType.SERVLET)
                .run("--management.endpoints.web.exposure.include=*"
                              ,"--spring.cloud.stream.function.routing.enabled=true"
                              ,"--spring.cloud.stream.defaultBinder=rabbit"
                              ,"--spring.cloud.function.definition=even;odd;functionRouter;toUppercaseFunction"
                            //   ,"--spring.cloud.stream.bindings.even-in-0.destination: dest/def/customerId-2"
                              );
    }

    @RestController
    public static class Resource {

        @Autowired
        StreamBridge streamBridge;
        
        // @PostMapping(value = "/withSendto")
        // @ResponseStatus(code = HttpStatus.OK)
        // public void withSendto(@RequestBody String payloadData) {
    
        //     Message<String>  message = MessageBuilder
        //                                          .withPayload(payloadData)
        //                                          .setHeader("spring.cloud.function.definition", payloadData)
        //                                          .build();
        //     streamBridge.send("functionRouter-in-0", message);
        // }  

        @PostMapping(value = "/process/{target}")
        @ResponseStatus(code = HttpStatus.OK )
        public void process(@RequestBody String body, @PathVariable("target") String target) {

            Message<String>  message = MessageBuilder
                                                 .withPayload(body)
                                                 .setHeader("spring.cloud.function.definition", "toUppercaseFunction")
                                                 .setHeader("type", target)
                                                 .build();
            streamBridge.send("functionRouter-in-0", message);
        }
    }
  

    @Bean 
    public Function<Message<String>, Message<String>> toUppercaseFunction() {
      return message  -> {
              log.info("toUppercaseFunction Received: {}",message);
              return Stream.of(message)
                            .map(m -> 
                                MessageBuilder
                                        .withPayload(m.getPayload().toUpperCase())
                                        .copyHeaders(m.getHeaders())
                                        .removeHeader("spring.cloud.function.definition")
                                        .setHeader("spring.cloud.stream.sendto.destination", 
                                                    m.getHeaders().get("type") + "-in-0"
                                        )
                                        .removeHeader("type")
                                        .build()
                            )
                            .findAny()
                            .get()
                            ;
      };
    }


    @Bean
    public Consumer<Message<String>> even() {
        return value -> log.info("EVEN: {}",value.getPayload());
    }
  
    @Bean
    public Consumer<Message<String>> odd() {
      return value -> log.info("ODD: {}",value.getPayload());
    }


}
