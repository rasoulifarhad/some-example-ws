package com.example.democloudstreamwebfux.intergration.directChannel;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegrationManagement;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.http.config.EnableIntegrationGraphController;

import lombok.extern.slf4j.Slf4j;

public class Application {
    
    // @EnableIntegration
    @EnableAutoConfiguration
    @Configuration
    @IntegrationComponentScan
    @EnableIntegrationGraphController
    @EnableIntegrationManagement
    @Slf4j
    public static class IntegrationApplication {
        public static void main(String[] args) throws Exception {

            log.info("--");
            new SpringApplicationBuilder()
                        .sources(IntegrationApplication.class,
                        IntegrationConfig.class,BookPublisher.class,Librarian.class,PremiumReader.class,Boot.class
                                            )
                        .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/intergration/directChannel/")
                        .run("--management.endpoints.web.exposure.include=*","--spring.main.lazy-initialization=false" 
                                    ,"--logging.level.root=INFO" 
                                    ,"--logging.level.org.springframework.retry.support.RetryTemplate=DEBUG"
                                    ,"--spring.cloud.stream.defaultBinder=rabbit");
        }
    }

    @Configuration
    public static  class IntegrationConfig {

        @Bean(name = "bookChannel")
        public DirectChannel bookChannel() {
            return MessageChannels.direct().get();
        }

    }

    @Configuration
    @Slf4j
    public static class Boot {


        @Bean
        @Order(30)
        public ApplicationRunner applicationRunner(BookPublisher bookPublisher ,
                                                    Librarian librarian,
                                                    @Qualifier("bookChannel") DirectChannel library
        ) {
            return args -> {
                log.info("------------");
                library.subscribe(new PremiumReader());
                library.subscribe(new PremiumReader());
                library.subscribe(new PremiumReader());

                List<Book> books = bookPublisher.getBooks();

                for (Book book : books) {
                    librarian.sendPremiumReaders(book);
                    
                }
             };
        }
    }

}
