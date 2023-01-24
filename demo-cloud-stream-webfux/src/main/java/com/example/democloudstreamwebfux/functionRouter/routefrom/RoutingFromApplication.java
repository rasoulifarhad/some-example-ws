package com.example.democloudstreamwebfux.functionRouter.routefrom;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.stream.binding.BinderAwareChannelResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;

import lombok.extern.slf4j.Slf4j;

/**
 * echo 'Hello boy' | curl -isS --location --request POST
 * http://localhost:8080/even -H "Content-Type: application/json" -d @- ;echo
 * echo 'Hello boy' | curl -isS --location --request POST
 * http://localhost:8080/odd -H "Content-Type: application/json" -d @- ;echo
 * 
 * tested is ok
 */

@SpringBootApplication
@Slf4j
@RestController
public class RoutingFromApplication {

  public static void main(String[] args) throws Exception {

    new SpringApplicationBuilder()
        .sources(RoutingFromApplication.class, TestSink.class, SendMessageService.class, Resource.class)
        .bannerMode(Banner.Mode.OFF)
        .properties("spring.config.name=functionRouter")
        .properties(
            "spring.config.location=optional:classpath:com/example/democloudstreamwebfux/functionRouter/routefrom/")
        // .properties("management.endpoints.web.exposure.include='*'")
        // .properties("spring.cloude.stream.source=toStream")
        .web(WebApplicationType.SERVLET)
        .run("--management.endpoints.web.exposure.include=*", "--spring.cloud.stream.function.routing.enabled=true",
            "--spring.cloud.stream.defaultBinder=rabbit",
            "--spring.cloud.function.definition=even;odd;routeFromFunction;functionRouter");
  }

  @Bean
  public Function<Message<String>, Message<String>> routeFromFunction() {
    return message -> {
      log.info("routeFromFunction: {}", message);
      return MessageBuilder.withPayload(message.getPayload().toUpperCase())
          .copyHeaders(message.getHeaders())
          .removeHeader("spring.cloud.function.definition")
          .setHeader("spring.cloud.stream.sendto.destination", (String) message.getHeaders().get("type") + "-in-0")
          .removeHeader("type")
          .build();
    };

  }

  @RestController
  public static class Resource {

    @Autowired
    SendMessageService sender;

    @PostMapping(value = "/{target}")
    @ResponseStatus(code = HttpStatus.OK)
    public void process(@RequestBody String body, @PathVariable("target") String target) {

      Map<String, Object> headers = Collections.singletonMap("spring.cloud.function.definition", target);
      sender.sendMessage(body, headers);

      headers = new HashMap<String,Object>() {
        {
          put("spring.cloud.function.definition", "routeFromFunction");
          put("type", target);
        }

      };
      Collections.singletonMap("spring.cloud.function.definition", "routeFromFunction");
      headers.put("type", target);

      sender.sendMessage(body, headers);
    }
  }

  @Component
  public static class SendMessageService {

    private static final String DEF_DEST = "functionRouter-in-0";

    private final BinderAwareChannelResolver resolver;
    private String destinationName;

    @Autowired
    public SendMessageService(BinderAwareChannelResolver resolver) {
      this(resolver, DEF_DEST);

    }

    public SendMessageService(BinderAwareChannelResolver resolver, String destinationName) {
      this.resolver = resolver;
      this.destinationName = destinationName;

    }

    public void sendMessage(String messageBody, Map<String, Object> headers) {
      Message<String> evenMessage = MessageBuilder
          .withPayload(messageBody)
          .copyHeaders(headers)
          .build();
      sendMessage(evenMessage);

    }

    public void sendMessage(Message message) {
      resolver.resolveDestination(destinationName).send(message);

    }

    public void sendMessage(String messageBody) {
      sendMessage(messageBody, null);

    }
  }

  @Bean
  ApplicationRunner sendData(SendMessageService sender) {
    return args -> {
      Message<String> evenMessage = MessageBuilder
          .withPayload("hello even direct")
          .setHeader("spring.cloud.function.definition", "even")
          .build();

      sender.sendMessage(evenMessage);

      Message<String> oddMessage = MessageBuilder
          .withPayload("hello odd direct")
          .setHeader("spring.cloud.function.definition", "odd")
          .build();

      sender.sendMessage(evenMessage);

      Message<String> evenMessageToFunction = MessageBuilder
          .withPayload("hello Function even indirect")
          .setHeader("spring.cloud.function.definition", "routeFromFunction")
          .setHeader("type", "even")
          .build();

      sender.sendMessage(evenMessageToFunction);

      Message<String> oddMessageToFunction = MessageBuilder
          .withPayload("hello Function odd indirect")
          .setHeader("spring.cloud.function.definition", "routeFromFunction")
          .setHeader("type", "odd")
          .build();

      sender.sendMessage(oddMessageToFunction);
    };

  }

  @Configuration
  @Slf4j
  public static class TestSink {

    @Bean
    public Consumer<Message<String>> even() {
      return message -> log.info("EVEN: {}", message);
    }

    @Bean
    public Consumer<Message<String>> odd() {
      return message -> log.info("ODD: {}", message);
    }

  }

}
