package com.example.democloudstreamwebfux.intergration.reply;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.RendezvousChannel;
import org.springframework.integration.config.EnableIntegrationManagement;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.http.config.EnableIntegrationGraphController;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import com.example.democloudstreamwebfux.intergration.reply.Application.ExceptionalIntegrationConfig.ExceptionalUpCaser;
import com.example.democloudstreamwebfux.intergration.reply.Application.IntegrationConfig.UpCaser;
import com.example.democloudstreamwebfux.intergration.reply.Application.NullChannelIntegrationConfig.NullUpCaser;

import lombok.extern.slf4j.Slf4j;

/**
 * “any problem in computer science can be solved by another layer of indirection.but that usually will create another problem."
 * 
 * CHANNEL ADAPTER
 * 
 * A Channel Adapter connects an application to the messaging system.
 * In Spring Integration we chose to constrict the definition to include only connections that are unidirectional, so a unidirectional message 
 * flow begins and ends in a channel adapter.
 * 
 * MESSAGING GATEWAY
 * 
 * In Spring Integration, a Messaging Gateway is a connection that’s specific to bidirectional messaging. 
 * If an incoming request needs to be serviced by multiple threads but the invoker needs to remain unaware of the messaging system, an inbound 
 * gateway provides the solution. On the outbound side, an incoming message can be used in a synchronous invocation, and the result is sent on 
 * the reply channel.
 * 
 * SERVICE ACTIVATOR
 * 
 * A Service Activator is a component that invokes a service based on an incoming message and sends an outbound message based on the return value 
 * of this service invocation. 
 * 
 * In Spring Integration, the definition is constrained to local method calls, so you can think of a service activator as a method-invoking outbound 
 * gateway. The method that’s being invoked is defined on an object that’s referenced within the same Spring 
 * application context.
 * 
 * ROUTER
 * 
 * A Router determines the next channel a message should be sent to based on the incoming message. 
 * This can be useful to send messages with different payloads to different, specialized consumers (Content-Based Router). 
 * The router doesn’t change anything in the message and is aware of channels.
 * 
 * 
 * SPLITTER
 * 
 * A Splitter receives one message and splits it into multiple messages that are sent to its output channel. 
 * This is useful whenever the act of processing message content can be split into multiple steps and executed by different consumers at the 
 * same time.
 * 
 * AGGREGATOR
 * 
 * An Aggregator waits for a group of correlated messages and merges them together when the group is complete. 
 * The correlation of the messages typically is based on a correlation ID, and the completion is typically related to the size of the group. 
 * A splitter and an aggregator are often used in a symmetric setup, where some work is done in parallel after a splitter, and the aggregated 
 * result is sent back to the upstream gateway.
 * 
 * COUPLING 
 * 
 * Coupling is an abstract concept used to measure how tightly connected the parts of a system are and how many assumptions they 
 * make about each other.
 * 
 *  - Type-level coupling: Loosening type-level coupling with dependency injection
 * 
 *  - System-level coupling
 * 
 * Spring Integration uses the concept of a 'message group' which holds all the messages that belong together in the context of an aggregator. 
 * These groups are stored with their 'correlation key' in a message store. 
 * The correlation key is determined by looking at the message, and it may differ between endpoints.
 * 
 * preparing and serving a meal 
 * A recipe is split into ingredients that are aggregated to a shopping list. 
 * This shopping list is converted into bags filled with products from the supermarket.
 * The bags are then split into products, which are aggregated to a 'mise en place', which is finally transformed into a meal. 
 *        recipe -> RecipeSplitter -> ShoppingListAggregator -> Supermarket -> ShoppingBagUnpacker -> MiseEnPlace -> dinner
 * 
 * Involved in the dinner are the host (that’s you), the kitchen, the guests, and the shops.
 * 
 * You orchestrate the whole event; the guests consume the end product and turn it into entertaining small talk and other things 
 * irrelevant to our story. 
 * The kitchen is the framework you use to transform the ingredients you get from the shop into the dinner. 
 * 
 * We’re interested in the sequence of events that take place after a date is set.
 * It starts with your selecting a menu and gathering the relevant recipes from your cookbook or the internet. The ingredients for the recipes 
 * must be bought at various shops, but to buy them one at a time, making a round trip to the shop for each product, is unreasonably 
 * inefficient, so you must find a smarter way to handle this process.
 * 
 * The trick is to create a shopping list for each shop you must visit. 
 * You pick the ingredients from each recipe, one by one, and put them on the appropriate shopping list.
 * You then make a trip to each shop, select the ingredients, and deliver the ingredients back to your kitchen.
 * With the ingredients now in a central location, each shopping bag must be unpacked and the ingredients sorted according to recipe. 
 * Having all the right ingredi0ents (and implements) gathered together is what professional chefs call the 'mise en place'.
 * 
 * With all the necessary elements at hand, each dish can be prepared, which usually involves putting the ingredients together in 
 * some way in a large container. 
 * When the dish is done, it’s divided among plates to be served.
 * 
 * Let’s say the recipe is a message. This message is split into ingredients, which can be sent by themselves as messages.  
 * The ingredients (messages) reach an aggregator that aggregates them to the appropriate shopping lists.
 * The shopping lists (messages) then travel to the supermarket where the shopper turns them into bags filled with groceries 
 * (messages), which travel back to the kitchen (endpoint).
 * 
 * The shopping bags are unpacked (split) into products that are put together in different configurations on the counter 
 * during the mise en place. 
 * 
 * These groups of ingredients are then transformed into a course in a pan. 
 * The dish in the pan is split onto different plates (messages), which then travel to the endpoints that consume them at the table. 
 * 
 * What we see here is a lot of breaking apart and putting together of payloads.
 *   The recipes are split and the ingredients aggregated to grocery lists. 
 *   The bags are unpacked and the products regrouped for the different courses.
 *   The dishes are split and assembled on plates.
 * 
 * (channel) flightNotifications -> (header enrivher) relatedTripsForFlight -> (splitter) FlightToTripNotifSplitter -> (channel) tripNotifications
 * 
 *  - The art of dividing: the splitter
 *  - How to get the big picture: the aggregator
 *  - Doing things in the right order: the resequencer
 * 
 * 
 * - Grouping messages based on timing
 *  
 *  In many aggregator use cases, completion is based not only on the group of messages but also on external factors such as time. 
 * (Note on timeouts: Timing out means a separate trigger is fired when the timeout point is reached.)
 * 
 * - Scatter-gather
 * 
 * Scatter-gather is a name commonly used to refer to a system that scatters a piece of information over nodes that all perform a certain 
 * operation on it; then another node gathers the results and aggregates them into the end result. 
 * 
 * “Scatter-Gather routes a request message to a number of recipients. It then uses an Aggregator to collect 
 * the responses and distill them into a single response message.”
 * 
 *  <aggregator
 *     id="kitchen"
 *     input-channel="products"
 *     output-channel="meals"
 *     ref="cook"
 *     method="prepareMeal"
 *     correlation-strategy="cook"
 *     correlation-strategy-method="correlatingRecipeFor"
 *     release-strategy="cook"
 *     release-strategy-method="canCookMeal"/>
 * 
 * The aggregator called kitchen refers to a cook for the assembly of the meal. 
 * The cook has a method to aggregate the products:
 * 
 *  @Aggregator
 *  public Meal prepareMeal(List<Message<Product>> products) {
 *    Recipe recipe = (Recipe) products.get(0).getHeaders().get("recipe");
 *    Meal meal = new Meal(recipe);
 *    for (Message<Product> message : products) {
 *       meal.cook(message.getPayload());
 *    }
 *    return meal;
 *  }
 * 
  * The correlation strategy relates products according to their recipe:
 *  
 *  @CorrelationStrategy
 *  public Object correlatingRecipeFor(Message<Product> message) {
 *    return message.getHeaders().get("recipe");
 *  }  
 * 
 * The release strategy delegates to the recipe to determine if all the ingredient requirements are met by products  
 * 
 *  public boolean canCookMeal(List<Message<?>> products) {
 *    Recipe recipe = (Recipe) products.get(0).getHeaders().get("recipe");
 *    return recipe.isSatisfiedBy(productsFromMessages(products));
 *  } 
 * 
 * The AggregatingMessageHandler can process a group of messages in two ways: message in and message out. When the message 
 * comes in, it’s correlated and stored. When a message group might go out, it’s released, processed, and finally marked 
 * as completed.
 * 
 * In summary, one central component, AbstractCorrelatingMessageHandler, uses several strategies to delegate its work:
 * 
 * CorrelationStrategy is used to find the correlation key of the message group, and MessageGroupStore is used to store the message group.
 * To decide when to release the group for processing, a ReleaseStrategy is used. 
 * A MessageGroupProcessor finally deals with the messages. 
 * Implementations of these strategies together form the different correlating endpoints.
 * 
 */
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
                        IntegrationConfig.class,UpCaser.class
                                            )
                        .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/intergration/reply/")
                        .run("--management.endpoints.web.exposure.include=*","--spring.main.lazy-initialization=false" 
                                    ,"--logging.level.root=INFO" 
                                    ,"--logging.level.org.springframework.retry.support.RetryTemplate=DEBUG"
                                    ,"--spring.cloud.stream.defaultBinder=rabbit");
        }
    }

    @Configuration
    @Slf4j
    public static class IntegrationConfig {

        @Bean(name = "myReplyChannel")
        public RendezvousChannel myReplyChannel() {
            return MessageChannels.rendezvous().get();
        }

        @Bean(name = "myRequestChannel")
        public DirectChannel myRequestChannel() {
            return MessageChannels.direct().get();
        }

        @MessageEndpoint
        public static class UpCaser {

            @ServiceActivator(inputChannel = "myRequestChannel")
            public String upCase(String in) {
                log.info("Upcase called: {}",in);
                return in.toUpperCase() ;
            }
        }

        @Bean
        @Order(30)
        public ApplicationRunner runner(RendezvousChannel myReplyChannel , DirectChannel myRequestChannel)  {

            return args -> {

                Message<String> requestMessage = MessageBuilder.withPayload("Hiiiiiiiiiiiiiii")
                                                                .setReplyChannel(myReplyChannel)
                                                                .build();
                Executors.newSingleThreadExecutor().submit(
                    () -> {
                        myRequestChannel.send(requestMessage);

                    }
                );
                

                Message<?> replyMessage = myReplyChannel.receive();
                log.info("Received: {} ",replyMessage);
            };
        }
    
    }

        // @EnableIntegration
        @EnableAutoConfiguration
        @Configuration
        @IntegrationComponentScan
        @EnableIntegrationGraphController
        @EnableIntegrationManagement
        @Slf4j
        public static class NullChannelIntegrationApplication {
            public static void main(String[] args) throws Exception {
    
                log.info("--");
                new SpringApplicationBuilder()
                            .sources(NullChannelIntegrationApplication.class,
                            NullChannelIntegrationConfig.class,NullUpCaser.class
                                                )
                            .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/intergration/reply/")
                            .run("--management.endpoints.web.exposure.include=*","--spring.main.lazy-initialization=false" 
                                        ,"--logging.level.root=INFO" 
                                        ,"--logging.level.org.springframework.retry.support.RetryTemplate=DEBUG"
                                        ,"--spring.cloud.stream.defaultBinder=rabbit");
            }
        }
    
        @Configuration
        @Slf4j
        public static class NullChannelIntegrationConfig {
    
            @Bean(name = "myRequestChannel")
            public DirectChannel myRequestChannel() {
                return MessageChannels.direct().get();
            }
    
            @MessageEndpoint
            public static class NullUpCaser {
    
                @ServiceActivator(inputChannel = "myRequestChannel",outputChannel = "nullChannel")
                public String upCase(String in) {
                    log.info("NullUpCaser.Upcase called: {}",in);
                    String out =  in.toUpperCase() ;
                    log.info("NullUpCaser.Upcase but discarded: {}",out);
                    return out ;

                }
            }
    
            @Bean
            @Order(30)
            public ApplicationRunner runner(DirectChannel myRequestChannel)  {
    
                return args -> {
    
                    Message<String> requestMessage = MessageBuilder.withPayload("null Hiiiiiiiiiiiiiii")
                                                                    .build();
                    myRequestChannel.send(requestMessage);
    
                };
            }
        
        }

        /**
         *  org.springframework.messaging.core.DestinationResolutionException: no output-channel or replyChannel header available
         */
        // @EnableIntegration
        @EnableAutoConfiguration
        @Configuration
        @IntegrationComponentScan
        @EnableIntegrationGraphController
        @EnableIntegrationManagement
        @Slf4j
        public static class ExceptionalIntegrationApplication {
            public static void main(String[] args) throws Exception {
    
                log.info("--");
                new SpringApplicationBuilder()
                            .sources(ExceptionalIntegrationApplication.class,
                            ExceptionalIntegrationConfig.class,ExceptionalUpCaser.class
                                                )
                            .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/intergration/reply/")
                            .run("--management.endpoints.web.exposure.include=*","--spring.main.lazy-initialization=false" 
                                        ,"--logging.level.root=INFO" 
                                        ,"--logging.level.org.springframework.retry.support.RetryTemplate=DEBUG"
                                        ,"--spring.cloud.stream.defaultBinder=rabbit");
            }
        }
    
        @Configuration
        @Slf4j
        public static class ExceptionalIntegrationConfig {
    
            @Bean(name = "myRequestChannel")
            public DirectChannel myRequestChannel() {
                return MessageChannels.direct().get();
            }
    
            @MessageEndpoint
            public static class ExceptionalUpCaser {
    
                @ServiceActivator(inputChannel = "myRequestChannel")
                public String upCase(String in) {
                    log.info("NullUpCaser.Upcase called: {}",in);
                    String out =  in.toUpperCase() ;
                    log.info("NullUpCaser.Upcase but discarded: {}",out);
                    log.info("Exception thrown");
                    return out ;

                }
            }
    
            @Bean
            @Order(30)
            public ApplicationRunner runner(DirectChannel myRequestChannel)  {
    
                return args -> {
    
                    Message<String> requestMessage = MessageBuilder.withPayload("exception Hiiiiiiiiiiiiiii")
                                                                    .build();
                    myRequestChannel.send(requestMessage);
    
                };
            }
        
        }
        
}
