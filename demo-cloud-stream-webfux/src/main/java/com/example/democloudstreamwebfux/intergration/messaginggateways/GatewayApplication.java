package com.example.democloudstreamwebfux.intergration.messaginggateways;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.aopalliance.aop.Advice;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.integration.annotation.AnnotationConstants;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.GatewayHeader;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.endpoint.AbstractMessageSource;
import org.springframework.integration.handler.DelayHandler;
import org.springframework.integration.handler.GenericHandler;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.handler.advice.ErrorMessageSendingRecoverer;
import org.springframework.integration.handler.advice.ExpressionEvaluatingRequestHandlerAdvice;
import org.springframework.integration.handler.advice.RequestHandlerRetryAdvice;
import org.springframework.integration.handler.advice.RetryStateGenerator;
import org.springframework.integration.handler.advice.SpelExpressionRetryStateGenerator;
import org.springframework.integration.transformer.ObjectToStringTransformer;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.example.democloudstreamwebfux.intergration.ChannelConfiguration;
import com.example.democloudstreamwebfux.intergration.ChannelConfiguration.TestStringChannels;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Message Endpoints
 * 
 * message endpoints are responsible for connecting the various messaging components to channels. 
 * 
 * Sending messages is quite straightforward. As shown earlier in Message Channels, you can send a message to a message channel. However, 
 * receiving is a bit more complicated. The main reason is that there are two types of consumers: polling consumers and event-driven 
 * consumers.
 * 
 * Of the two, event-driven consumers are much simpler. Without any need to manage and schedule a separate poller thread, they are essentially 
 * listeners with a callback method. When connecting to one of Spring Integration’s subscribable message channels, this simple option works 
 * great. 
 * 
 * However, when connecting to a buffering, pollable message channel, some component has to schedule and manage the polling threads.
 * 
 * Spring Integration provides two different endpoint implementations to accommodate these two types of consumers. Therefore, the consumers 
 * themselves need only implement the callback interface. When polling is required, the endpoint acts as a container for the consumer 
 * instance. The benefit is similar to that of using a container for hosting message-driven beans, but, since these consumers are 
 * Spring-managed objects running within an ApplicationContext, it more closely resembles Spring’s own MessageListener containers.
 * 
 */
/**
 * Message Handler
 * Spring Integration’s MessageHandler interface is implemented by many of the components within the framework. In other words, this is not 
 * part of the public API, and you would not typically implement MessageHandler directly. Nevertheless, it is used by a message consumer for 
 * actually handling the consumed messages, so being aware of this strategy interface does help in terms of understanding the overall role 
 * of a consumer. The interface is defined as follows:
 * 
 *  public interface MessageHandler {
 *  
 *      void handleMessage(Message<?> message);
 *  
 *  }
 * 
 * Despite its simplicity, this interface provides the foundation for most of the components (routers, transformers, splitters, aggregators, 
 * service activators, and others) covered in the following chapters.
 * 
 * hose components each perform very different functionality with the messages they handle, but the requirements for actually receiving a 
 * message are the same, and the choice between polling and event-driven behavior is also the same. Spring Integration provides two endpoint 
 * implementations that host these callback-based handlers and let them be connected to message channels.
 * 
 * 
 */
/**
 * Event-driven Consumer
 * 
 * The SubscribableChannel interface provides a subscribe() method and that the method accepts a MessageHandler parameter . The following 
 * listing shows the definition of the subscribe method:
 * 
 *    subscribableChannel.subscribe(messageHandler);
 * 
 * Since a handler that is subscribed to a channel does not have to actively poll that channel, this is an event-driven consumer, and the 
 * implementation provided by Spring Integration accepts a SubscribableChannel and a MessageHandler, as the following example shows:
 * 
 *      SubscribableChannel channel = context.getBean("subscribableChannel", SubscribableChannel.class);
 *      EventDrivenConsumer consumer = new EventDrivenConsumer(channel, exampleHandler);
 * 
 */
/**
 * Polling Consumer
 * 
 * Spring Integration also provides a PollingConsumer, and it can be instantiated in the same way except that the channel must implement 
 * PollableChannel, as the following example shows:
 * 
 *        PollableChannel channel = context.getBean("pollableChannel", PollableChannel.class);
 *        PollingConsumer consumer = new PollingConsumer(channel, exampleHandler);
 * 
 * There are many other configuration options for the polling consumer. For example, the trigger is a required property. The following 
 * example shows how to set the trigger:
 * 
 *          PollingConsumer consumer = new PollingConsumer(channel, handler);
 *          consumer.setTrigger(new PeriodicTrigger(30, TimeUnit.SECONDS));
 * 
 * The PeriodicTrigger is typically defined with a simple interval (in milliseconds) but also supports an initialDelay property and a 
 * boolean fixedRate property (the default is false — that is, no fixed delay). The following example sets both properties:
 * 
 *          PeriodicTrigger trigger = new PeriodicTrigger(1000);
 *          trigger.setInitialDelay(5000);
 *          trigger.setFixedRate(true);
 * 
 * The result of the three settings in the preceding example is a trigger that waits five seconds and then triggers every second.
 * 
 * In addition to the trigger, you can specify two other polling-related configuration properties: maxMessagesPerPoll and receiveTimeout. The 
 * following example shows how to set these two properties:
 * 
 *       PollingConsumer consumer = new PollingConsumer(channel, handler);
 *       consumer.setMaxMessagesPerPoll(10);
 *       consumer.setReceiveTimeout(5000);
 * 
 * The maxMessagesPerPoll property specifies the maximum number of messages to receive within a given poll operation. This means that the 
 * poller continues calling receive() without waiting, until either null is returned or the maximum value is reached. For example, if a 
 * poller has a ten-second interval trigger and a maxMessagesPerPoll setting of 25, and it is polling a channel that has 100 messages in 
 * its queue, all 100 messages can be retrieved within 40 seconds. It grabs 25, waits ten seconds, grabs the next 25, and so on. If 
 * maxMessagesPerPoll is configured with a negative value, then MessageSource.receive() is called within a single polling cycle until it 
 * returns null. Starting with version 5.5, a 0 value has a special meaning - skip the MessageSource.receive() call altogether, which may 
 * be considered as pausing for this polling endpoint until the maxMessagesPerPoll is changed to a n non-zero value at a later time, 
 * e.g. via a Control Bus.
 * 
 * The receiveTimeout property specifies the amount of time the poller should wait if no messages are available when it invokes the receive 
 * operation. For example, consider two options that seem similar on the surface but are actually quite different: The first has an interval 
 * trigger of 5 seconds and a receive timeout of 50 milliseconds, while the second has an interval trigger of 50 milliseconds and a receive 
 * timeout of 5 seconds. The first one may receive a message up to 4950 milliseconds later than it arrived on the channel (if that message 
 * arrived immediately after one of its poll calls returned). On the other hand, the second configuration never misses a message by more than 
 * 50 milliseconds. The difference is that the second option requires a thread to wait. However, as a result, it can respond much more quickly 
 * to arriving messages. This technique, known as “long polling”, can be used to emulate event-driven behavior on a polled source.
 * 
 * A polling consumer can also delegate to a Spring TaskExecutor, as the following example shows:
 *       
 *       PollingConsumer consumer = new PollingConsumer(channel, handler);
 *       
 *       TaskExecutor taskExecutor = context.getBean("exampleExecutor", TaskExecutor.class);
 *       consumer.setTaskExecutor(taskExecutor);
 *       
 * Furthermore, a PollingConsumer has a property called adviceChain. This property lets you to specify a List of AOP advices for handling 
 * additional cross cutting concerns including transactions. These advices are applied around the doPoll() method. For more in-depth 
 * information, see the sections on AOP advice chains and transaction support under Endpoint Namespace Support.
 *       
 * The earlier examples show dependency lookups. However, keep in mind that these consumers are most often configured as Spring bean 
 * definitions. In fact, Spring Integration also provides a FactoryBean called ConsumerEndpointFactoryBean that creates the appropriate 
 * consumer type based on the type of channel. Also, Spring Integration has full XML namespace support to even further hide those details. 
 * The namespace-based configuration is in this guide featured as each component type is introduced.

 * 
 * Many of the MessageHandler implementations can generate reply messages. As mentioned earlier, sending messages is trivial when compared 
 * to receiving messages. Nevertheless, when and how many reply messages are sent depends on the handler type. For example, an aggregator 
 * waits for a number of messages to arrive and is often configured as a downstream consumer for a splitter, which can generate multiple 
 * replies for each message it handles. When using the namespace configuration, you do not strictly need to know all of the details. However, 
 * it still might be worth knowing that several of these components share a common base class, the AbstractReplyProducingMessageHandler, and 
 * that it provides a setOutputChannel(..) method. 
 * 
 * For several of these annotations, when a message-handling method returns a non-null value, the endpoint tries to send a reply. This is consistent across both configuration options (namespace and annotations) in that such an endpoint’s output channel is used (if available), and the REPLY_CHANNEL message header value is used as a fallback.
 * 
 * The combination of output channels on endpoints and the reply channel message header enables a pipeline approach, where multiple components have an output channel and the final component allows the reply message to be forwarded to the reply channel (as specified in the original request message). In other words, the final component depends on the information provided by the original sender and can dynamically support any number of clients as a result. This is an example of the return address pattern. 
 * 
 * The processing of these annotations creates the same beans as the corresponding XML components — AbstractEndpoint instances and MessageHandler instances (or MessageSource instances for the inbound channel adapter). See Annotations on @Bean Methods. The bean names are generated from the following pattern: [componentName].[methodName].[decapitalizedAnnotationClassShortName]. In the preceding example the bean name is thingService.otherThing.serviceActivator for the AbstractEndpoint and the same name with an additional .handler (.source) suffix for the MessageHandler (MessageSource) bean. Such a name can be customized using an @EndpointId annotation alongside with these messaging annotations. The MessageHandler instances (MessageSource instances) are also eligible to be tracked by the message history.
 */
/**
 * 
 * When you use messaging annotations or the Java DSL, you don’t need to worry about these components, because the Framework automatically 
 * produces them with appropriate annotations and BeanPostProcessor implementations. When building components manually, you should use the 
 * ConsumerEndpointFactoryBean to help determine the target AbstractEndpoint consumer implementation to create, based on the provided 
 * inputChannel property.
 * 
 * On the other hand, the ConsumerEndpointFactoryBean delegates to an another first class citizen in the Framework - 
 * org.springframework.messaging.MessageHandler. The goal of the implementation of this interface is to handle the message consumed by 
 * the endpoint from the channel. All EIP components in Spring Integration are MessageHandler implementations (for example, 
 * AggregatingMessageHandler, MessageTransformingHandler, AbstractMessageSplitter, and others). The target protocol outbound adapters 
 * (FileWritingMessageHandler, HttpRequestExecutingMessageHandler, AbstractMqttMessageHandler, and others) are also MessageHandler 
 * implementations. When you develop Spring Integration applications with Java configuration, you should look into the Spring Integration 
 * module to find an appropriate MessageHandler implementation to use for the @ServiceActivator configuration. For example, to send an 
 * XMPP message (see XMPP Support) you should configure something like the following:
 * 
 * @Bean
 * @ServiceActivator(inputChannel = "input")
 * public MessageHandler sendChatMessageHandler(XMPPConnection xmppConnection) {
 *     ChatMessageSendingMessageHandler handler = new ChatMessageSendingMessageHandler(xmppConnection);
 * 
 *     DefaultXmppHeaderMapper xmppHeaderMapper = new DefaultXmppHeaderMapper();
 *     xmppHeaderMapper.setRequestHeaderNames("*");
 *     handler.setHeaderMapper(xmppHeaderMapper);
 * 
 *     return handler;
 * }
 * 
 * The MessageHandler implementations represent the outbound and processing part of the message flow.
 *
 * The inbound message flow side has its own components, which are divided into polling and listening behaviors. The listening (message-driven) 
 * components are simple and typically require only one target class implementation to be ready to produce messages. Listening components can 
 * be one-way MessageProducerSupport implementations, (such as AbstractMqttMessageDrivenChannelAdapter and ImapIdleChannelAdapter) or 
 * request-reply MessagingGatewaySupport implementations (such as AmqpInboundGateway and AbstractWebServiceInboundGateway).
 * 
 * Polling inbound endpoints are for those protocols that do not provide a listener API or are not intended for such a behavior, including any 
 * file based protocol (such as FTP), any data bases (RDBMS or NoSQL), and others.
 * 
 * These inbound endpoints consist of two components: the poller configuration, to initiate the polling task periodically, and a message source 
 * class to read data from the target protocol and produce a message for the downstream integration flow. The first class for the poller 
 * configuration is a SourcePollingChannelAdapter. It is one more AbstractEndpoint implementation, but especially for polling to initiate an 
 * integration flow. Typically, with the messaging annotations or Java DSL, you should not worry about this class. The Framework produces a 
 * bean for it, based on the @InboundChannelAdapter configuration or a Java DSL builder spec.
 * 
 * Message source components are more important for the target application development, and they all implement the MessageSource interface 
 * (for example, MongoDbMessageSource and AbstractTwitterMessageSource). With that in mind, our config for reading data from an RDBMS table 
 * with JDBC could resemble the following:
 * 
 *  @Bean
 *  @InboundChannelAdapter(value = "fooChannel", poller = @Poller(fixedDelay="5000"))
 *  public MessageSource<?> storedProc(DataSource dataSource) {
 *     return new JdbcPollingChannelAdapter(dataSource, "SELECT * FROM foo where status = 0");
 *  } 
 * 
 * You can find all the required inbound and outbound classes for the target protocols in the particular Spring Integration module (in most 
 * cases, in the respective package). For example, the spring-integration-websocket adapters are:
 * 
 *     >> o.s.i.websocket.inbound.WebSocketInboundChannelAdapter: Implements MessageProducerSupport to listen for frames on the 
 *        socket and produce message to the channel.
 * 
 *     >> o.s.i.websocket.outbound.WebSocketOutboundMessageHandler: The one-way AbstractMessageHandler implementation to convert 
 *        incoming messages to the appropriate frame and send over websocket.
 * 
 */
public class GatewayApplication {
    
    @EnableAutoConfiguration
    @Configuration
    @IntegrationComponentScan    
    public static class MessagingGateWayApplication {
        public static void main(String[] args) throws Exception {
            new SpringApplicationBuilder()
                    .sources(MessagingGateWayApplication.class,TestStringChannels.class,ChannelConfiguration.class
                                        ,MyGateway.class ,GatewayConfig.class)
                    .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/intergration/messaginggateways/")
                    .run("--management.endpoints.web.exposure.include=*","--spring.main.lazy-initialization=false" 
                                ,"--logging.level.root=INFO" );
        }
    }

    /**
     * 
     * Mapping Method Arguments
     * 
     *  void payloadAndHeaderMapWithoutAnnotations(String s, Map<String, Object> map);
     * 
     *  void payloadAndHeaderMapWithAnnotations(@Payload String s, @Headers Map<String, Object> map);
     * 
     *  void headerValuesAndPayloadWithAnnotations(@Header("k1") String x, @Payload String s, @Header("k2") String y);
     * 
     *  void mapOnly(Map<String, Object> map); // the payload is the map and no custom headers are added
     * 
     *  void twoMapsAndOneAnnotatedWithPayload(@Payload Map<String, Object> payload, Map<String, Object> headers);
     * 
     *  @Payload("#args[0] + #args[1] + '!'")
     *  void payloadAnnotationAtMethodLevel(String a, String b);
     * 
     *  @Payload("@someBean.exclaim(#args[0])")
     *  void payloadAnnotationAtMethodLevelUsingBeanResolver(String s);
     * 
     *  void payloadAnnotationWithExpression(@Payload("toUpperCase()") String s);
     * 
     *  Note that, in this example, the SpEL variable, #this, refers to the argument — in this case, the value of s.
     *  void payloadAnnotationWithExpressionUsingBeanResolver(@Payload("@someBean.sum(#this)") String s); //
     * 
     */

    @MessagingGateway(name = "MyGateway" , defaultRequestChannel = "routingChannel",
                    defaultHeaders = @GatewayHeader(name = "calledMethod" , expression = "#gatewayMethod.name"))
    public interface MyGateway {

        public String uppercaseViaDefault(String value) ;

        @Gateway(requestChannel = "stringChannel1" ,headers = @GatewayHeader(name = "header-h1" ,value = "headerValue-h1"))
        public String uppercase(String value) ;

        @Gateway(requestChannel = "stringChannel2")
        public String lowercase(String value) ;

        @Gateway(requestChannel = "stringChannel3")
        public String lowercase(String value, @Header("AddedHeader") String addedHeaderValue ) ;

        @Gateway(requestChannel = "stringChannel4" ,payloadExpression = "#args[0]" 
                 , requestTimeoutExpression = "#args[1]" , replyTimeoutExpression = "#args[2]")
        public String lowercaseWithTimeout(String payload, long requestTimeout , long replayTimeout ) ;

    }

    @Slf4j
    @Configuration
    public static class GatewayConfig {

        @ServiceActivator(inputChannel = "routingChannel")
        public String uppercaseViaDefaultHandler(String value,@Headers Map<String , Object> headres ) {
            log.info("Headers: {} ",headres);
            return value.toUpperCase();
        }

        @ServiceActivator(inputChannel = "stringChannel1")
        public String uppercaseHandler(String value,@Headers Map<String , Object> headres ) {
            log.info("Headers: {} ",headres);
            return value.toUpperCase();
        }

        @ServiceActivator(inputChannel = "stringChannel2")
        public String lowercaseHandler(String value,@Headers Map<String , Object> headres ) {
            log.info("Headers: {} ",headres);
            return value.toLowerCase();
        }

        @ServiceActivator(inputChannel = "stringChannel3")
        public String lowercaseHandler2(String value,@Headers Map<String , Object> headres ) {
            log.info("Headers: {} ",headres);
            return value.toLowerCase();
        }

        @ServiceActivator(inputChannel = "stringChannel4")
        public String lowercaseWithTimeoutHandler(String value , @Headers Map<String , Object> headres ) {
            log.info("Headers: {} ",headres);
            return value.toLowerCase();
        }

        @Bean
        @Order(20)
        public ApplicationRunner uppercaseRunner(MyGateway gateway) {
            return args -> {
                log.info(" {} ",gateway.uppercase("_____uppercase!_____")) ;

                TimeUnit.MILLISECONDS.sleep(50);
                log.info(" {} ",gateway.lowercase("_____LOWERCASE!_____"));

                TimeUnit.MILLISECONDS.sleep(50);
                log.info(" {} ",gateway.lowercase("_____LOWERCASE_____","Adddedddddd"));

                TimeUnit.MILLISECONDS.sleep(50);
                log.info(" {} ",gateway.lowercaseWithTimeout("_____LOWERCASE_WITH_TIMEOUT_____",50L,60L));

                TimeUnit.MILLISECONDS.sleep(50);
                log.info(" {} ",gateway.uppercaseViaDefault("_____uppercaseViaDefault_____"));

            };
        }

    }

    /**
     * 
     * Invoking No-Argument Methods
     * 
     * When invoking methods on a Gateway interface that do not have any arguments, the default behavior is to receive a Message from a 
     * PollableChannel.
     * 
     * Sometimes, however, you may want to trigger no-argument methods so that you can interact with other components downstream that do 
     * not require user-provided parameters, such as triggering no-argument SQL calls or stored procedures.
     * 
     * To achieve send-and-receive semantics, you must provide a payload. To generate a payload, method parameters on the interface are 
     * not necessary. You can either use the @Payload annotation or the payload-expression attribute in XML on the method element. The 
     * following list includes a few examples of what the payloads could be:
     * 
     *    a literal string
     * 
     *    #gatewayMethod.name
     * 
     *    new java.util.Date()
     * 
     *    @someBean.someMethod()'s return value
     * 
     * If both annotations are present (and the payloadExpression is provided), @Gateway wins.
     * 
     * If a method has no argument and no return value but does contain a payload expression, it is treated as a send-only operation.
     * 
     */

    @EnableAutoConfiguration
    @Configuration
    @IntegrationComponentScan    
    public static class NoMethodArgsGatewayApplication {
        public static void main(String[] args) throws Exception {
            new SpringApplicationBuilder()
                    .sources(NoMethodArgsGatewayApplication.class,TestStringChannels.class,ChannelConfiguration.class
                                        ,NoMethodArgsGateway.class ,NoMethodArgsGatewayConfig.class)
                    .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/intergration/messaginggateways/")
                    .run("--management.endpoints.web.exposure.include=*","--spring.main.lazy-initialization=false" 
                                ,"--logging.level.root=INFO" );
        }
    }


    @MessagingGateway(name = "noMethodArgsGateway" ,defaultRequestChannel = "datePubSubChannel1")
    public interface NoMethodArgsGateway {

        @Payload("new java.util.Date()")
        public String retriveAnString() ;

        @Gateway(payloadExpression = "new java.util.Date()" ,requestChannel = "datePubSubChannel2")
        public String anotherRetriveAnString() ;

        @Gateway(payloadExpression = "new java.util.Date()" ,requestChannel = "datePubSubChannel3")
        public void sendOnlyDate() ;



    }
    
    @Slf4j
    public static class NoMethodArgsGatewayConfig {

        @ServiceActivator(inputChannel = "datePubSubChannel1")
        public String retriveAnStringHandler(Date date, @Headers Map<String , Object> headres ) {

            try {
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("Headers: {} ",headres);
            log.info("date: {} ",date.toInstant().toString());

            return date.toInstant().toString();
        }

        @ServiceActivator(inputChannel = "datePubSubChannel2")
        public String anotherRetriveAnStringHandler(Date date, @Headers Map<String , Object> headres ) {

            try {
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("Headers: {} ",headres);
            log.info("date: {} ",date.toInstant().toString());

            return date.toInstant().toString();
        }

        @ServiceActivator(inputChannel = "datePubSubChannel3")
        public void sendOnlyHandler(Date date, @Headers Map<String , Object> headres ) {

            try {
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("Headers: {} ",headres);
            log.info("******> {} <*&****** ",date.toInstant().toString());



        }

        @Bean
        @Order(20)
        public ApplicationRunner noMethodArgsGatewayRunner(@Qualifier("noMethodArgsGateway") NoMethodArgsGateway noMethodArgsGateway) {
            return args ->  {
                log.info("retriveAnStringHandler ====> {} <===" , noMethodArgsGateway.retriveAnString() );

                TimeUnit.MILLISECONDS.sleep(50);
                log.info("anotherRetriveAnStringHandler ====> {} <===" , noMethodArgsGateway.anotherRetriveAnString() );

                TimeUnit.MILLISECONDS.sleep(50);
                noMethodArgsGateway.sendOnlyDate() ;
            };

        }
    }

    /**
     * 
     * Invoking default Methods
     * 
     * An interface for gateway proxy may have default methods as well and starting with version 5.3, the framework injects a 
     * DefaultMethodInvokingMethodInterceptor into a proxy for calling default methods using a java.lang.invoke.MethodHandle approach 
     * instead of proxying. The interfaces from JDK, such as java.util.function.Function, still can be used for gateway proxy, but 
     * their default methods cannot be called because of internal Java security reasons for a MethodHandles.Lookup instantiation 
     * against JDK classes. These methods also can be proxied (losing their implementation logic and, at the same time, restoring 
     * previous gateway proxy behavior) using an explicit @Gateway annotation on the method, or proxyDefaultMethods on the 
     * @MessagingGateway annotation or <gateway> XML component.
     * 
     * 
     * Error Handling
     * 
     * The gateway invocation can result in errors. By default, any error that occurs downstream is re-thrown “as is” upon the 
     * gateway’s method invocation. For example, consider the following simple flow:
     *  
     *   gateway -> service-activator
     * 
     * f the service invoked by the service activator throws a MyException (for example), the framework wraps it in a MessagingException 
     * and attaches the message passed to the service activator in the failedMessage property. Consequently, any logging performed by 
     * the framework has full the context of the failure. By default, when the exception is caught by the gateway, the MyException is 
     * unwrapped and thrown to the caller. You can configure a throws clause on the gateway method declaration to match the particular 
     * exception type in the cause chain. For example, if you want to catch a whole MessagingException with all the messaging information 
     * of the reason of downstream error, you should have a gateway method similar to the following:
     * 
     *   public interface MyGateway {
     *
     *       void performProcess() throws MessagingException;
     *
     *   }
     * 
     * If your gateway method does not have a throws clause, the gateway traverses the cause tree, looking for a RuntimeException that is 
     * not a MessagingException. If none is found, the framework throws the MessagingException. If the MyException in the preceding discussion 
     * has a cause of SomeOtherException and your method throws SomeOtherException, the gateway further unwraps that and throws it to the 
     * caller.
     * 
     * When a gateway is declared with no service-interface, an internal framework interface RequestReplyExchanger is used.
     * 
     *   public interface RequestReplyExchanger {
     *
     *	     Message<?> exchange(Message<?> request) throws MessagingException;
     *
     *   }
     */

     /**
      * Asynchronous Gateway
      */
        
    @EnableAutoConfiguration
    @Configuration
    @IntegrationComponentScan    
    public static class AsyncGatewayApplication {
        public static void main(String[] args) throws Exception {
            new SpringApplicationBuilder()
                    .sources(AsyncGatewayApplication.class,TestStringChannels.class,ChannelConfiguration.class,
                                        AsyncGateway.class ,ExecAsyncGateway.class, CompletableFutureAsyncGateway.class,
                                        VoidAsyncGateway.class,AsyncGatewayConfig.class)
                    .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/intergration/messaginggateways/")
                    .run("--management.endpoints.web.exposure.include=*","--spring.main.lazy-initialization=false" 
                                ,"--logging.level.root=INFO" );
        }
    }

    @MessagingGateway(name = "asyncGateway", defaultRequestChannel = "intPubSubChannel2")
    public interface AsyncGateway {

        Future<Integer> multipllyByTow(int value) ;

        @Gateway(requestChannel = "intPubSubChannel1")
        ListenableFuture<Integer> multipllyByThree(int value) ;
    }

    @MessagingGateway(name = "execAsyncGateway",asyncExecutor = "exec" ,defaultRequestChannel = "intPubSubChannel3")
    public interface ExecAsyncGateway {

        @Gateway(requestChannel = "intPubSubChannel3")
        ListenableFuture<Integer> multipllyByThree(int value) ;
    }

    @MessagingGateway(name = "noExecAsyncGateway",asyncExecutor = AnnotationConstants.NULL ,defaultRequestChannel = "intPubSubChannel4")
    public interface NoExecAsyncGateway {

        @Gateway(requestChannel = "intPubSubChannel4")
        Future<Integer> multipllyBySeven(int value) ;
    }

    /**
     * CompletableFuture
     * 
     * Starting with version 4.2, gateway methods can now return CompletableFuture<?>. There are two modes of operation when returning this 
     * type:
     * 
     *     - When an async executor is provided and the return type is exactly CompletableFuture (not a subclass), the framework runs the 
     *       task on the executor and immediately returns a CompletableFuture to the caller. 
     *       CompletableFuture.supplyAsync(Supplier<U> supplier, Executor executor) is used to create the future.
     * 
     *     - When the async executor is explicitly set to null and the return type is CompletableFuture or the return type is a subclass of 
     *       CompletableFuture, the flow is invoked on the caller’s thread. In this scenario, the downstream flow is expected to return a 
     *       CompletableFuture of the appropriate type
     * 
     * 
     * 
     */
    @MessagingGateway(name = "completableFutureAsyncGateway" ,defaultRequestChannel = "intPubSubChannel5")
    public interface CompletableFutureAsyncGateway {

        @Gateway(requestChannel = "intPubSubChannel5")
        CompletableFuture<Integer> multipllyByFour(int value) ;
    }

    @MessagingGateway(name = "completableFutureAsyncGatewayWithNullExecutor" ,defaultRequestChannel = "intPubSubChannel6"
                              ,asyncExecutor = AnnotationConstants.NULL  )
    public interface CompletableFutureAsyncGatewayWithNullExecutor {

        @Gateway(requestChannel = "intPubSubChannel6")
        CompletableFuture<Integer> multipllyByFive(int value) ;
    }

    /**
     * Downstream Flows Returning an Asynchronous Type
     * 
     * As mentioned in the ListenableFuture section above, if you wish some downstream component to return a message with an async payload 
     * (Future, Mono, and others), you must explicitly set the async executor to null (or "" when using XML configuration). The flow is then 
     * invoked on the caller thread and the result can be retrieved later.
     * 
     * void Return Type
     * 
     * Unlike the return types mentioned earlier, when the method return type is void, the framework cannot implicitly determine that you wish 
     * the downstream flow to run asynchronously, with the caller thread returning immediately. In this case, you must annotate the interface 
     * method with @Async, as the following example shows:
     * 
     * Unlike the Future<?> return types, there is no way to inform the caller if some exception is thrown by the flow, unless some custom 
     * TaskExecutor (such as an ErrorHandlingTaskExecutor) is associated with the @Async annotation.
     * 
     */
    @MessagingGateway(name = "voidAsyncGateway" ,defaultRequestChannel = "intPubSubChannel7")
    public interface VoidAsyncGateway {

        @Gateway(requestChannel = "intPubSubChannel7")
        @Async
        void sendMultipllyBySix(int value) ;
    }

    @Configuration
    @Slf4j
    public static class AsyncGatewayConfig {

        @ServiceActivator(inputChannel = "intPubSubChannel2")
        Integer multiplyByTowHandler(int value) {
            return  value * 2;
        }
 
        @ServiceActivator(inputChannel = "intPubSubChannel1")
        Integer multipllyByThreeHandler(int value) {
            return  value * 3;
        }

        @ServiceActivator(inputChannel = "intPubSubChannel3")
        Integer execMultipllyByFourHandler(int value) {
            return  value * 3;
        }

        @ServiceActivator(inputChannel = "intPubSubChannel5")
        Integer multipllyByFourHandler(int value) {
            return  value * 4;
        }

        @ServiceActivator(inputChannel = "intPubSubChannel4")
        Future<Integer> noExecMultipllyBySevenHandler(int value) {
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            return executorService.submit(() -> value  * 4 );
        }

        @ServiceActivator(inputChannel = "intPubSubChannel6")
        CompletableFuture<Integer> multipllyByFiveHandler(int value) {
            return  CompletableFuture.supplyAsync(() -> value * 5) ;
        }

        @ServiceActivator(inputChannel = "intPubSubChannel7")
        void  multipllyBySixHandler(int value) {
            log.info("multipllyBySixHandler({}): {} <======",value,value * 6);
        }

        @Bean
        @Order(20)
        public ApplicationRunner asyncGatewayRunner(
                                @Qualifier("asyncGateway") AsyncGateway asyncGateway,
                                @Qualifier("execAsyncGateway") ExecAsyncGateway execAsyncGateway,
                                @Qualifier("noExecAsyncGateway") NoExecAsyncGateway noExecAsyncGateway,
                                @Qualifier("completableFutureAsyncGateway") CompletableFutureAsyncGateway completableFutureAsyncGateway,
                                @Qualifier("completableFutureAsyncGatewayWithNullExecutor") CompletableFutureAsyncGatewayWithNullExecutor completableFutureAsyncGatewayWithNullExecutor,
                                @Qualifier("voidAsyncGateway") VoidAsyncGateway voidAsyncGateway) {
            return args ->  {

                int val = 19 ;
                Future<Integer> resFuture =  asyncGateway.multipllyByTow(val);
                int res = resFuture.get(1000, TimeUnit.SECONDS);

                log.info("multipllyByTow({}): {} <===" , val ,res);


                ListenableFuture<Integer> listenableFuture =  asyncGateway.multipllyByThree(val);
                listenableFuture.addCallback(new ListenableFutureCallback<Integer>() {

                    @Override
                    public void onSuccess(Integer result) {
                        log.info("multipllyByThree({}): {} <===" , val ,result);
                    }

                    @Override
                    public void onFailure(Throwable ex) {
                        
                    }
                    
                });


                ListenableFuture<Integer> anotheListenableFuture = execAsyncGateway.multipllyByThree(val);
                anotheListenableFuture.addCallback(new ListenableFutureCallback<Integer>() {

                    @Override
                    public void onSuccess(Integer result) {
                        log.info("asyncGatewayWithExecutor-multipllyByThree({}): {} <===" , val ,result);

                    }

                    @Override
                    public void onFailure(Throwable ex) {
                        
                    }
                     
                });

                CompletableFuture<Integer> completableFuture = completableFutureAsyncGateway.multipllyByFour(val);
                completableFuture.thenAccept(t -> log.info("multipllyByFour({}): {} <======",val,t));

                CompletableFuture<Integer>  completableFuture2 = completableFutureAsyncGatewayWithNullExecutor.multipllyByFive(val);
                completableFuture2.thenAccept(t -> log.info("multipllyByFive({}): {} <======",val,t));


                voidAsyncGateway.sendMultipllyBySix(val);

                Future<Integer> multipllyBySevenres =  noExecAsyncGateway.multipllyBySeven(val) ;
                log.info("multipllyBySeven({}): {} ",val,multipllyBySevenres.get()) ;
            };
        }

    }

    /**
    * Asynchronous Gateway
    */
        
    @EnableAutoConfiguration
    @Configuration
    @IntegrationComponentScan    
    public static class ReactGatewayApplication {
        public static void main(String[] args) throws Exception {
              new SpringApplicationBuilder()
                      .sources(ReactGatewayApplication.class,TestStringChannels.class,ChannelConfiguration.class,
                                          ReactMathGateway.class ,ReactMathGatewayConfig.class
                                          )
                      .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/intergration/messaginggateways/")
                      .run("--management.endpoints.web.exposure.include=*","--spring.main.lazy-initialization=false" 
                                  ,"--logging.level.root=INFO" );
        }
    }

    @MessagingGateway(name = "reactMathGateway")
    public interface ReactMathGateway {

        @Gateway(requestChannel = "intPubSubChannel2")
        Mono<Integer> multiply(int value );

    }

    @Configuration
    @Slf4j
    public  static class ReactMathGatewayConfig {

        @ServiceActivator(inputChannel = "intPubSubChannel2")
        public Integer  multiply(Integer value ) {
            return  value * 2;

        }

        @Bean
        @Order(20)
        public ApplicationRunner reactMathGatewayRunner(@Qualifier("reactMathGateway") ReactMathGateway reactMathGateway) {
            return args -> {

                Flux.just("19","29","39")
                            .map(Integer::parseInt)
                            .flatMap(reactMathGateway::multiply)
                            .collectList()
                            .subscribe(list -> {
                                list.forEach(t -> log.info("reactMathGateway(): {} <======",t));
                            })
                            
                            ;


            } ;
        }

        /**
         * #######       Gateway Behavior When No response Arrives
         * 
         * 
         * # Long-running Process Downstream - Sync Gateway, single-threaded
         * 
         * If a component downstream is still running (perhaps because of an infinite loop or a slow service), setting a reply-timeout has 
         * no effect, and the gateway method call does not return until the downstream service exits (by returning or throwing an exception).
         * 
         * # Long-running Process Downstream - Sync Gateway, multi-threaded
         * 
         * If a component downstream is still running (perhaps because of an infinite loop or a slow service) in a multi-threaded message 
         * flow, setting the reply-timeout has an effect by allowing gateway method invocation to return once the timeout has been reached, 
         * because the GatewayProxyFactoryBean polls on the reply channel, waiting for a message until the timeout expires. However, if 
         * the timeout has been reached before the actual reply was produced, it could result in a 'null' return from the gateway method. 
         * You should understand that the reply message (if produced) is sent to a reply channel after the gateway method invocation might 
         * have returned, so you must be aware of that and design your flow with it in mind.
         * 
         * # Downstream Component Returns 'null' - Sync Gateway — single-threaded
         * 
         * If a component downstream returns 'null' and no reply-timeout has been configured, the gateway method call hangs indefinitely, 
         * unless a reply-timeout has been configured or the requires-reply attribute has been set on the downstream component (for 
         * example, a service activator) that might return 'null'. In this case, an exception would be thrown and propagated to the 
         * gateway.
         * 
         * # Downstream Component Returns 'null' - Sync Gateway — multi-threaded
         * 
         * The behavior is the same as the previous case.
         * 
         * # Downstream Component Return Signature is 'void' While Gateway Method Signature Is Non-void - Sync Gateway, single-threaded
         * 
         * If a component downstream returns 'void' and no reply-timeout has been configured, the gateway method call hangs indefinitely 
         * unless a reply-timeout has been configured.
         * 
         * # Downstream Component Return Signature is 'void' While Gateway Method Signature Is Non-void - Sync Gateway, multi-threaded
         * 
         * The behavior is the same as the previous case.
         * 
         * # Downstream Component Results in Runtime Exception - Sync Gateway, single-threaded
         * 
         * If a component downstream throws a runtime exception, the exception is propagated through an error message back to the 
         * gateway and re-thrown.
         * 
         * # Downstream Component Results in Runtime Exception - Sync Gateway, multi-threaded
         * 
         * The behavior is the same as the previous case.
         * 
         */


    /**
    * delayer
    */
        
    @EnableAutoConfiguration
    @Configuration
    @IntegrationComponentScan    
    public static class DelayerApplication {
        public static void main(String[] args) throws Exception {
              new SpringApplicationBuilder()
                      .sources(DelayerApplication.class,TestStringChannels.class,ChannelConfiguration.class,
                                           DelayerConfig.class
                                          )
                      .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/intergration/messaginggateways/")
                      .run("--management.endpoints.web.exposure.include=*","--spring.main.lazy-initialization=false" 
                                  ,"--logging.level.root=INFO" );
        }
    }

    @Configuration
    public static class DelayerConfig {


        @ServiceActivator(inputChannel = "longPubSubChannel2")
        @Bean("delayer")
        public DelayHandler delayer() {
            DelayHandler handler =  new DelayHandler("delayer.messageGroupId");
            // handler.setDefaultDelay(3_000L);
            handler.setDelayExpressionString("headers['delay']");
            handler.setOutputChannelName("longPubSubChannel3");
            return handler ;

        }

        @Bean
        @Order(20)
        public ApplicationRunner delayerRunner(@Qualifier("longPubSubChannel2") PublishSubscribeChannel longPubSubChannel2) {

            return args -> {

                longPubSubChannel2.send(
                    MessageBuilder.withPayload(Instant.now().toEpochMilli())
                                    .setHeader("delay", Long.valueOf(10000))
                                    .setHeader("trest", "test")
                                    .build()
                    );
                
                longPubSubChannel2.send(
                        MessageBuilder.withPayload(Instant.now().toEpochMilli())
                                        .setHeader("noDelay", "noDelay")
                                        .build()
                        );
                                                

                longPubSubChannel2.send(
                            MessageBuilder.withPayload(Instant.now().toEpochMilli())
                                            .setHeader("delay", Long.valueOf(10000))
                                            .build()
                            );
    
            };
        }
    }

   
    }

    /**
     * Adding Behavior to Endpoints
     * 
     * In addition to providing the general mechanism to apply AOP advice classes, Spring Integration provides these out-of-the-box advice implementations:
     * 
     *    - RequestHandlerRetryAdvice (described in Retry Advice)
     *
     *    - RequestHandlerCircuitBreakerAdvice (described in Circuit Breaker Advice)
     *    -
     *    - ExpressionEvaluatingRequestHandlerAdvice (described in Expression Evaluating Advice)
     *    -
     *    - RateLimiterRequestHandlerAdvice (described in Rate Limiter Advice)
     *    -
     *    - CacheRequestHandlerAdvice (described in Caching Advice)
     *    -
     *    - ReactiveRequestHandlerAdvice (described in Reactive Advice)
     * 
     * 
     */
    /**
    * Retry Advice
    */
        
    @EnableAutoConfiguration
    @Configuration
    @IntegrationComponentScan    
    @EnableRetry
    
    public static class RetryAdviceApplication {
        public static void main(String[] args) throws Exception {
              new SpringApplicationBuilder()
                      .sources(RetryAdviceApplication.class,TestStringChannels.class,ChannelConfiguration.class,
                      FailingService.class  ,RetryAdviceConfig.class
                                          )
                      .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/intergration/messaginggateways/")
                      .run("--management.endpoints.web.exposure.include=*","--spring.main.lazy-initialization=false" 
                                  ,"--logging.level.root=INFO" 
                                  ,"--logging.level.org.springframework.retry.support.RetryTemplate=DEBUG");
        }
    }


    @MessageEndpoint
    public static class FailingService {
        
        @ServiceActivator(inputChannel = "longPubSubChannel2",adviceChain = "retryAdvice")
        public void failingMethod(long msg) {
            throw new RuntimeException("Error from FailingService.failingMethod");
        }
 
        @ServiceActivator(inputChannel = "longPubSubChannel3",adviceChain = "statefulRetryAdvice")
        public void failingMethod2(long msg) {
            throw new RuntimeException("Error from FailingService.failingMethod2");
        }
    }
    
    @Configuration
    
    public static class RetryAdviceConfig {

        @Bean
        public RetryStateGenerator retryStateGenerator() {
             SpelExpressionRetryStateGenerator st = new SpelExpressionRetryStateGenerator("headers['id']") ;
             return st ;
        }

        // @Bean(name = "myRetryTemplate")
        // public RetryTemplate myRetryTemplate() {

        //     return RetryTemplate
        //                     .builder()
        //                         .maxAttempts(4)
        //                         // .exponentialBackoff(100, 2, 10000)
        //                         // .uniformRandomBackoff(1000, 3000)
        //                         .fixedBackoff(1000)
        //                         .retryOn(RuntimeException.class)
        //                         .build();
        // }

        // @Bean(name = "statefulRetryAdvice")
        // public RequestHandlerRetryAdvice statefulRetryAdvice(@Qualifier("myRetryTemplate") RetryTemplate myRetryTemplate , 
        //                                     RetryStateGenerator retryStateGenerator,
        //                                     DirectChannel directRecoveryChannel ,
        //                                     QueueChannel recoveryChannel) {

        //     RequestHandlerRetryAdvice r =  new RequestHandlerRetryAdvice() ;

        //     r.setRetryStateGenerator(retryStateGenerator);
        //     ErrorMessageSendingRecoverer e = new ErrorMessageSendingRecoverer(directRecoveryChannel) ;
            
        //     r.setRecoveryCallback(e);
        //     r.setRetryTemplate(myRetryTemplate);
        //     return r;

        // }

        @Bean(name = "anotherRetryTemplate")
        public RetryTemplate anotherRetryTemplate() {

            return RetryTemplate
                            .builder()
                                .maxAttempts(4)
                                // .exponentialBackoff(100, 2, 10000)
                                // .uniformRandomBackoff(1000, 3000)
                                .fixedBackoff(1000)
                                .retryOn(RuntimeException.class)
                                .build();
        }

        @Bean(name = "retryAdvice")
        public RequestHandlerRetryAdvice retryAdvice(@Qualifier("anotherRetryTemplate") RetryTemplate anotherRetryTemplate, 
                                                PublishSubscribeChannel pubSubRecoveryChannel 
                                                ) {

            RequestHandlerRetryAdvice r =  new RequestHandlerRetryAdvice() ;
   

            ErrorMessageSendingRecoverer e = new ErrorMessageSendingRecoverer(pubSubRecoveryChannel) ;
            
            r.setRecoveryCallback(e);
            r.setRetryTemplate(anotherRetryTemplate);
            return r;

        }

        @Bean
        @Order(20)
        public ApplicationRunner retryRunner(@Qualifier("longPubSubChannel2") PublishSubscribeChannel longPubSubChannel2) {
            
            return args -> {
                longPubSubChannel2.send(
                    MessageBuilder.withPayload(Long.valueOf(Instant.now().toEpochMilli()))
                                    .build()
                    );

            } ;

        }

        // @Bean
        // @Order(22)
        // public ApplicationRunner statefulRetryRunner(@Qualifier("longPubSubChannel3") PublishSubscribeChannel longPubSubChannel3) {
            
        //     return args -> {
        //         longPubSubChannel3.send(
        //             MessageBuilder.withPayload(Long.valueOf(Instant.now().toEpochMilli()))
        //                             .setHeader("jms_messageId", "jms_messageId")
        //                             .build()
        //             );

        //     } ;

        // }


    }

 
    /**
     * Expression Evaluating Advice
     * 
     * It provides a mechanism to evaluate an expression on the original inbound message sent to the endpoint. 
     * 
     * Separate expressions are available to be evaluated, after either success or failure. Optionally, a message containing the evaluation 
     * result, together with the input message, can be sent to a message channel.
     * 
     * A typical use case for this advice might be with an <ftp:outbound-channel-adapter/>, perhaps to move the file to one directory if the 
     * transfer was successful or to another directory if it fails:
     * 
     * The advice has properties to set an expression when successful, an expression for failures, and corresponding channels for each. For 
     * the successful case, the message sent to the successChannel is an AdviceMessage, with the payload being the result of the expression 
     * evaluation. An additional property, called inputMessage, contains the original message sent to the handler. A message sent to the 
     * failureChannel (when the handler throws an exception) is an ErrorMessage with a payload of 
     * MessageHandlingExpressionEvaluatingAdviceException. Like all MessagingException instances, this payload has failedMessage and cause 
     * properties, as well as an additional property called evaluationResult, which contains the result of the expression evaluation.
     * 
     * When an exception is thrown in the scope of the advice, by default, that exception is thrown to the caller after any failureExpression 
     * is evaluated. If you wish to suppress throwing the exception, set the trapException property to true. The following advice shows how 
     * to configure an advice with Java DSL: 
     * 
     * 
     * 
     * */       
    @EnableAutoConfiguration
    @Configuration
    @IntegrationComponentScan    
    
    public static class ExpressionEvaluatingApplication {
        public static void main(String[] args) throws Exception {
              new SpringApplicationBuilder()
                      .sources(ExpressionEvaluatingApplication.class,TestStringChannels.class,ChannelConfiguration.class,
                      ExpressionEvaluatingConfig.class
                                          )
                      .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/intergration/messaginggateways/")
                      .run("--management.endpoints.web.exposure.include=*","--spring.main.lazy-initialization=false" 
                                  ,"--logging.level.root=INFO" 
                                  ,"--logging.level.org.springframework.retry.support.RetryTemplate=DEBUG");
        }
    }

    @Configuration
    @Slf4j
    public static class ExpressionEvaluatingConfig {

        @Bean
        public IntegrationFlow advised() {
            return flow -> flow
                                .handle((GenericHandler<String>) (payload, headers) -> {
                                            if (payload.equals("good"))
                                                return null ;
                                            else {
                                                throw new RuntimeException("some failure");
                                            }
                                },c -> c.advice(expressionAdvice()));
        }

        @Bean
        public Advice expressionAdvice() {
            ExpressionEvaluatingRequestHandlerAdvice advice = new ExpressionEvaluatingRequestHandlerAdvice();
            advice.setSuccessChannelName("success.input");
            advice.setOnSuccessExpressionString("payload + ' was successful'");
            advice.setFailureChannelName("failure.input");
            advice.setOnFailureExpressionString("payload + ' was bad, with reason: ' + #exception.cause.message");
            advice.setTrapException(true);
            return advice ;
        }

        @Bean
        public IntegrationFlow success() {
            return flow -> flow
                                .handle(message -> log.info("{}",message));
        }

        @Bean 
        public IntegrationFlow failure() {

            return flow -> flow
                                .handle(message -> log.info("{}",message));
        }

        @Bean
        @Order(30)
        public ApplicationRunner runner(@Qualifier("advised.input") MessageChannel input ) {

            return args -> {
                input.send(new GenericMessage<String>("good"));
                input.send(new GenericMessage<String>("bad"));

            };

        }

    }

    /**
     * Logging Channel Adapter
     * 
     * he <logging-channel-adapter> is often used in conjunction with a wire tap, as discussed in Wire Tap. However, it can also be used as 
     * the ultimate consumer of any flow. For example, consider a flow that ends with a <service-activator> that returns a result, but you 
     * wish to discard that result. To do that, you could send the result to NullChannel. Alternatively, you can route it to an INFO level 
     * <logging-channel-adapter>. That way, you can see the discarded message when logging at INFO level but not see it when logging at 
     * (for example) the WARN level. With a NullChannel, you would see only the discarded message when logging at the DEBUG level. 
     * 
     */

    @EnableAutoConfiguration
    @Configuration
    @IntegrationComponentScan    
    
    public static class LoggingChannelAdapterApplication {
        public static void main(String[] args) throws Exception {
              new SpringApplicationBuilder()
                      .sources(LoggingChannelAdapterApplication.class,TestStringChannels.class,ChannelConfiguration.class,
                      LoggingChannelAdapterConfig.class
                                          )
                      .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/intergration/messaginggateways/")
                      .run("--management.endpoints.web.exposure.include=*","--spring.main.lazy-initialization=false" 
                                  ,"--logging.level.root=INFO" 
                                  ,"--logging.level.org.springframework.retry.support.RetryTemplate=DEBUG");
        }
    }

    @Configuration
    
    public static class LoggingChannelAdapterConfig {

        @Bean
        @ServiceActivator(inputChannel = "logChannel")
        public LoggingHandler logging() {
            LoggingHandler adapter = new LoggingHandler(LoggingHandler.Level.DEBUG);
            adapter.setLoggerName("TEST_LOGGER");
            adapter.setLogExpressionString("headers.id + ': ' + payload");
            return adapter ;
        }

        @Bean
        @Order(30)
        public  ApplicationRunner runner(LoggerGateway loggerGateway) {
            return args ->  {
                loggerGateway.sendToLogger("FOoooooooo");
            };
        }

        @Bean
        @Order(35)
        public  ApplicationRunner runner2(LoggerGatewayFlow loggerGatewayFlow) {
            return args ->  {
                loggerGatewayFlow.sendToLogger("FOooooooooFlow");
            };
        }
        @Bean
        public IntegrationFlow loggingFlow() {
            return IntegrationFlows
                            .from(LoggerGatewayFlow.class) 
                            .log(LoggingHandler.Level.DEBUG, "TEST_LOGGER_FLOW",
                                m -> m.getHeaders().getId() + ": " + m.getPayload()
                              ).get()
                              ;

        }

    }

    @MessagingGateway(defaultRequestChannel = "logChannel")
    public interface LoggerGateway {

        public void sendToLogger(String data );
    }

    @MessagingGateway(defaultRequestChannel = "logChannel")
    public interface LoggerGatewayFlow {

        public void sendToLogger(String data );
    }


    /**
     * java.util.function Interfaces Support
     * 
     * Starting with version 5.1, Spring Integration provides direct support for interfaces in the java.util.function package. All messaging 
     * endpoints, (Service Activator, Transformer, Filter, etc.) can now refer to Function (or Consumer) beans. The Messaging Annotations can 
     * be applied directly on these beans similar to regular MessageHandler definitions. 
     * 
     *  @Bean
     *  @Transformer(inputChannel = "functionServiceChannel")
     *  public Function<String, String> functionAsService() {
     *      return String::toUpperCase;
     *  }
     * 
     * When the function returns an array, Collection (essentially, any Iterable), Stream or Reactor Flux, @Splitter can be used on such a 
     * bean to perform iteration over the result content.
     * 
     * The java.util.function.Consumer interface can be used for an <int:outbound-channel-adapter> or, together with the @ServiceActivator 
     * annotation, to perform the final step of a flow:
     * 
     *  @Bean
     *  @ServiceActivator(inputChannel = "messageConsumerServiceChannel")
     *  public Consumer<Message<?>> messageConsumerAsService() {
     *      // Has to be an anonymous class for proper type inference
     *      return new Consumer<Message<?>>() {
     *  
     *          @Override
     *          public void accept(Message<?> e) {
     *              collector().add(e);
     *          }
     *  
     *      };
     *  }
     * 
     * if you would like to deal with the whole message in your Function/Consumer you cannot use a lambda definition. Because of Java type 
     * erasure we cannot determine the target type for the apply()/accept() method call.
     * 
     * The java.util.function.Supplier interface can simply be used together with the @InboundChannelAdapter annotation, or as a ref in an 
     * <int:inbound-channel-adapter>
     * 
     *  @Bean
     *  @InboundChannelAdapter(value = "inputChannel", poller = @Poller(fixedDelay = "1000"))
     *  public Supplier<String> pojoSupplier() {
     *      return () -> "foo";
     *  }
     * 
     * With the Java DSL we just need to use a reference to the function bean in the endpoint definitions. Meanwhile an implementation of the 
     * Supplier interface can be used as regular MessageSource definition:
     * 
     *  @Bean
     *  public Function<String, String> toUpperCaseFunction() {
     *      return String::toUpperCase;
     *  }
     *  
     *  @Bean
     *  public Supplier<String> stringSupplier() {
     *      return () -> "foo";
     *  }
     *  
     *  @Bean
     *  public IntegrationFlow supplierFlow() {
     *      return IntegrationFlows.from(stringSupplier())
     *                  .transform(toUpperCaseFunction())
     *                  .channel("suppliedChannel")
     *                  .get();
     *  }
     * 
     * This function support is useful when used together with the Spring Cloud Function framework, where we have a function catalog and 
     * can refer to its member functions from an integration flow definition.
     * 
     */

    // @SpringBootApplication(exclude = {KafkaAutoConfiguration.class, KafkaStreamsBinderSupportAutoConfiguration.class,
    //     KafkaStreamsTopologyEndpointAutoConfiguration.class, KafkaStreamsFunctionAutoConfiguration.class})     
    @EnableAutoConfiguration
    @Configuration
    @IntegrationComponentScan    
    
    public static class FunctionIntegrationApplication {
        public static void main(String[] args) throws Exception {
              new SpringApplicationBuilder()
                      .sources(FunctionIntegrationApplication.class,TestStringChannels.class,ChannelConfiguration.class,
                      FunctionIntegrationConfig.class
                                          )
                      .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/intergration/messaginggateways/")
                      .run("--management.endpoints.web.exposure.include=*","--spring.main.lazy-initialization=false" 
                                  ,"--logging.level.root=INFO" 
                                  ,"--logging.level.org.springframework.retry.support.RetryTemplate=DEBUG"
                                  ,"--spring.cloud.stream.defaultBinder=rabbit");
        }
    }

    @Configuration
    @Slf4j
    public static class FunctionIntegrationConfig {

        @Bean(name = "functionServiceChannel")
        public MessageChannel   functionServiceChannel() {
            return MessageChannels
                            .queue()
                            .datatype(String.class)
                            .get();
        }

        @Bean(name = "messageConsumerServiceChannel")
        public QueueChannel messageConsumerServiceChannel() {
            return MessageChannels
                            .queue()
                            .datatype(String.class)
                            .get() ;
        }

        @Bean
        @Transformer(inputChannel = "functionServiceChannel" ,outputChannel = "messageConsumerServiceChannel")
        public Function<String,String> toUpperCaseFunctionService() {

            return String::toUpperCase;
            
        }

        @ServiceActivator(inputChannel = "messageConsumerServiceChannel")
        @Bean
        public Consumer<String> messageConsumerAsService() {
            return m ->  log.info("messageConsumerAsService: {} ", m);
        }

        @Bean
        @Order(30)
        public ApplicationRunner toUpperCaseFunctionServiceRunner(@Qualifier("functionServiceChannel") QueueChannel functionServiceChannel) {
            return args ->  {

                functionServiceChannel.send(new GenericMessage<String>("Hwlooooooooooooo"));

                log.info("toUpperCaseFunctionServiceRunner:{}", functionServiceChannel.receive());
            };
        }


        @Bean
        public Function<String,String> toUpperCaseFunctionServiceFlow() {
            return String::toUpperCase;
        }

        @Bean(name = "integerIncrementor")
        public AtomicInteger integerIncrementor() {

            return new AtomicInteger() ;

        }

        @Bean
        public Supplier<String> stringSupplier() {
            return () -> "foooooooooooooooooooooo" + integerIncrementor().getAndIncrement();
        }

        @Bean
        public IntegrationFlow supplierFlow () {
            return IntegrationFlows
                                .fromSupplier(stringSupplier())
                                .transform(toUpperCaseFunctionServiceFlow())
                                .log()
                                .channel("stringChannel3")
                                .get();

        }

    }

    @EnableAutoConfiguration
    @Configuration
    @IntegrationComponentScan    
    
    public static class AnnotationIntegrationApplication {
        public static void main(String[] args) throws Exception {
              new SpringApplicationBuilder()
                      .sources(AnnotationIntegrationApplication.class,TestStringChannels.class,ChannelConfiguration.class,
                      AnnotationIntegrationConfig.class,AnnotationMessageEndpoint.class
                                          )
                      .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/intergration/messaginggateways/")
                      .run("--management.endpoints.web.exposure.include=*","--spring.main.lazy-initialization=false" 
                                  ,"--logging.level.root=INFO" 
                                  ,"--logging.level.org.springframework.retry.support.RetryTemplate=DEBUG"
                                  ,"--spring.cloud.stream.defaultBinder=rabbit");
        }
    }

    
    @MessageEndpoint
    public static class AnnotationMessageEndpoint {

        private static AtomicInteger atomicInteger = new AtomicInteger() ;

        @InboundChannelAdapter(value = "counterChannel")
        public Integer count() {
            return atomicInteger.incrementAndGet();

        }

        @InboundChannelAdapter(value = "foChannel",poller = @Poller(fixedRate = "5000"))
        public String foo() {
            return "foooooo";
        }


    }

    @Configuration
    @Slf4j
    public static class AnnotationIntegrationConfig {

        @Bean
        public QueueChannel counterChannel() {
            return MessageChannels.queue().datatype(Integer.class).get();
        }

        @Bean
        public QueueChannel foChannel() {
            return MessageChannels.queue().datatype(String.class).get();
        }

        @ServiceActivator(inputChannel = "counterChannel")
        @Bean
        public Consumer<Integer> countConsumerAsService() {
            return m ->  log.info("counterChannel: {} ", m);
        }

        @ServiceActivator(inputChannel = "foChannel")
        @Bean
        public Consumer<String> fooConsumerAsService() {
            return m ->  log.info("fooConsumerAsService: {} ", m);
        }


    }

    /**
     * Annotations on @Bean Methods
     * 
     * Starting with version 4.0, you can configure messaging annotations on @Bean method definitions in @Configuration classes, to produce 
     * message endpoints based on the beans, not the methods. It is useful when @Bean definitions are “out-of-the-box” MessageHandler instances 
     * (AggregatingMessageHandler, DefaultMessageSplitter, and others), Transformer instances (JsonToObjectTransformer, 
     * ClaimCheckOutTransformer, and others), and MessageSource instances (FileReadingMessageSource, RedisStoreMessageSource, and others). 
     * 
     *  @Configuration
     *  @EnableIntegration
     *  public class MyFlowConfiguration {
     *  
     *      @Bean
     *      @InboundChannelAdapter(value = "inputChannel", poller = @Poller(fixedDelay = "1000"))
     *      public MessageSource<String> consoleSource() {
     *          return CharacterStreamReadingMessageSource.stdin();
     *      }
     *  
     *      @Bean
     *      @Transformer(inputChannel = "inputChannel", outputChannel = "httpChannel")
     *      public ObjectToMapTransformer toMapTransformer() {
     *          return new ObjectToMapTransformer();
     *      }
     *  
     *      @Bean
     *      @ServiceActivator(inputChannel = "httpChannel")
     *      public MessageHandler httpHandler() {
     *      HttpRequestExecutingMessageHandler handler = new HttpRequestExecutingMessageHandler("https://foo/service");
     *          handler.setExpectedResponseType(String.class);
     *          handler.setOutputChannelName("outputChannel");
     *          return handler;
     *      }
     *  
     *      @Bean
     *      @ServiceActivator(inputChannel = "outputChannel")
     *      public LoggingHandler loggingHandler() {
     *          return new LoggingHandler("info");
     *      }
     *  
     *  }
     * 
     * 
     * Version 5.0 introduced support for a @Bean annotated with @InboundChannelAdapter that returns java.util.function.Supplier, which can 
     * produce either a POJO or a Message. 
     * 
     *  @Configuration
     *  @EnableIntegration
     *  public class MyFlowConfiguration {
     *  
     *      @Bean
     *      @InboundChannelAdapter(value = "inputChannel", poller = @Poller(fixedDelay = "1000"))
     *      public Supplier<String> pojoSupplier() {
     *          return () -> "foo";
     *      }
     *  
     *      @Bean
     *      @InboundChannelAdapter(value = "inputChannel", poller = @Poller(fixedDelay = "1000"))
     *      public Supplier<Message<String>> messageSupplier() {
     *          return () -> new GenericMessage<>("foo");
     *      }
     *  }
     * 
     * When you use these annotations on consumer @Bean definitions, if the bean definition returns an appropriate MessageHandler (depending on 
     * the annotation type), you must set attributes (such as outputChannel, requiresReply, order, and others), on the MessageHandler @Bean 
     * definition itself. Only the following annotation attributes are used: adviceChain, autoStartup, inputChannel, phase, and poller. All 
     * other attributes are for the handler. 
     * 
     * The bean names are generated with the following algorithm: 
     * 
     *      - The MessageHandler (MessageSource) @Bean gets its own standard name from the method name or name attribute on the @Bean. This 
     *        works as though there were no messaging annotation on the @Bean method.
     * 
     *      - The AbstractEndpoint bean name is generated with the following pattern: 
     *        [configurationComponentName].[methodName].[decapitalizedAnnotationClassShortName]. For example, the SourcePollingChannelAdapter 
     *        endpoint for the consoleSource() definition shown earlier gets a bean name of 
     *        myFlowConfiguration.consoleSource.inboundChannelAdapter. See also Endpoint Bean Names.
     * 
     * When using these annotations on @Bean definitions, the inputChannel must reference a declared bean. Channels are not automatically 
     * declared in this case. 
     * 
     */

    /**
     * Creating a Bridge with Annotations
     * 
     * Starting with version 4.0, Java configuration provides the @BridgeFrom and @BridgeTo @Bean method annotations to mark MessageChannel beans in @Configuration classes. These really exists for completeness, providing a convenient mechanism to declare a BridgeHandler and its message endpoint configuration:
     * 
     *  @Bean
     *  public PollableChannel bridgeFromInput() {
     *      return new QueueChannel();
     *  }
     *  
     *  @Bean
     *  @BridgeFrom(value = "bridgeFromInput", poller = @Poller(fixedDelay = "1000"))
     *  public MessageChannel bridgeFromOutput() {
     *      return new DirectChannel();
     *  }
     *  @Bean
     *  public QueueChannel bridgeToOutput() {
     *      return new QueueChannel();
     *  }
     *  
     *  @Bean
     *  @BridgeTo("bridgeToOutput")
     *  public MessageChannel bridgeToInput() {
     *      return new DirectChannel();
     *  }
     * 
     */
    @EnableAutoConfiguration
    @Configuration
    @IntegrationComponentScan    
    
    public static class AnnotationOnBeanIntegrationApplication {
        public static void main(String[] args) throws Exception {
              new SpringApplicationBuilder()
                      .sources(AnnotationOnBeanIntegrationApplication.class,TestStringChannels.class,ChannelConfiguration.class,
                      AnnotationOnBeanIntegrationConfig.class
                                          )
                      .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/intergration/messaginggateways/")
                      .run("--management.endpoints.web.exposure.include=*","--spring.main.lazy-initialization=false" 
                                  ,"--logging.level.root=INFO" 
                                  ,"--logging.level.org.springframework.retry.support.RetryTemplate=DEBUG"
                                  ,"--spring.cloud.stream.defaultBinder=rabbit");
        }
    }

    @Configuration
        
    public static class AnnotationOnBeanIntegrationConfig {

        @Bean
        public QueueChannel counterChannel() {
            return MessageChannels.queue().datatype(Integer.class).get();
        }

        @Bean
        public QueueChannel loggingChannel() {
            return MessageChannels.queue().datatype(String.class).get();
        }

        @Bean
        @InboundChannelAdapter(channel = "counterChannel" ,poller = @Poller(fixedDelay = "1000")) 
        public MessageSource<Integer> intSource () {

            MessageSource<Integer> mSource = new AbstractMessageSource<Integer>() {

                final AtomicInteger counter = new AtomicInteger();
                @Override
                protected Object doReceive() {
                    return counter.incrementAndGet();
                }

                @Override
                public String getComponentType() {
                    return "inbound-channel-adapter";
                }
                
            };
            return mSource ;
            
        }

        // @Bean
        // @Transformer(inputChannel = "counterChannel" ,outputChannel = "loggingChannel")
        // public ObjectToJsonTransformer toJsonTransformer() {
        //     return new ObjectToJsonTransformer() ;
        // }

        @Bean
        @Transformer(inputChannel = "counterChannel" ,outputChannel = "loggingChannel")
        public ObjectToStringTransformer toStringTransformer() {
            return new ObjectToStringTransformer() ;
        }
        

        @Bean
        @ServiceActivator(inputChannel = "loggingChannel")
        public LoggingHandler loggingHandler() {
            return new LoggingHandler("info");
        }
    }

    /**
     * Message Channel Implementations
     * 
     * PublishSubscribeChannel
     * 
     * The PublishSubscribeChannel implementation broadcasts any Message sent to it to all of its subscribed handlers. 
     * This is most often used for sending event messages, whose primary role is notification (as opposed to document messages, which are 
     * generally intended to be processed by a single handler). 
     * Note that the PublishSubscribeChannel is intended for sending only. Since it broadcasts to its subscribers directly when its 
     * send(Message) method is invoked, consumers cannot poll for messages (it does not implement PollableChannel and therefore has no 
     * receive() method).
     * Instead, any subscriber must itself be a MessageHandler, and the subscriber’s handleMessage(Message) method is invoked in turn.
     * 
     * Prior to version 3.0, invoking the send method on a PublishSubscribeChannel that had no subscribers returned false. When used in 
     * conjunction with a MessagingTemplate, a MessageDeliveryException was thrown. Starting with version 3.0, the behavior has changed 
     * such that a send is always considered successful if at least the minimum subscribers are present (and successfully handle the 
     * message). This behavior can be modified by setting the minSubscribers property, which defaults to 0.
     * 
     * QueueChannel
     * 
     * The QueueChannel implementation wraps a queue. Unlike the PublishSubscribeChannel, the QueueChannel has point-to-point semantics. In 
     * other words, even if the channel has multiple consumers, only one of them should receive any Message sent to that channel. It provides 
     * a default no-argument constructor (providing an essentially unbounded capacity of Integer.MAX_VALUE) as well as a constructor that 
     * accepts the queue capacity, as the following listing shows:
     * 
     *      public QueueChannel(int capacity)
     * 
     * A channel that has not reached its capacity limit stores messages in its internal queue, and the send(Message<?>) method returns 
     * immediately, even if no receiver is ready to handle the message. If the queue has reached capacity, the sender blocks until room 
     * is available in the queue. Alternatively, if you use the send method that has an additional timeout parameter, the queue blocks 
     * until either room is available or the timeout period elapses, whichever occurs first. Similarly, a receive() call returns 
     * immediately if a message is available on the queue, but, if the queue is empty, then a receive call may block until either a message 
     * is available or the timeout, if provided, elapses. In either case, it is possible to force an immediate return regardless of the 
     * queue’s state by passing a timeout value of 0. Note, however, that calls to the versions of send() and receive() with no timeout 
     * parameter block indefinitely.
     * 
     * PriorityChannel
     * 
     * Whereas the QueueChannel enforces first-in-first-out (FIFO) ordering, the PriorityChannel is an alternative implementation that allows 
     * for messages to be ordered within the channel based upon a priority. By default, the priority is determined by the priority header within 
     * each message. However, for custom priority determination logic, a comparator of type Comparator<Message<?>> can be provided to the 
     * PriorityChannel constructo
     * 
     * RendezvousChannel
     * 
     * The RendezvousChannel enables a “direct-handoff” scenario, wherein a sender blocks until another party invokes the channel’s receive() 
     * method. The other party blocks until the sender sends the message. Internally, this implementation is quite similar to the QueueChannel, 
     * except that it uses a SynchronousQueue (a zero-capacity implementation of BlockingQueue). This works well in situations where the 
     * sender and receiver operate in different threads, but asynchronously dropping the message in a queue is not appropriate. In other words, 
     * with a RendezvousChannel, the sender knows that some receiver has accepted the message, whereas with a QueueChannel, the message would 
     * have been stored to the internal queue and potentially never received.
     * 
     * The RendezvousChannel is also useful for implementing request-reply operations. The sender can create a temporary, anonymous instance 
     * of RendezvousChannel, which it then sets as the 'replyChannel' header when building a Message. After sending that Message, the sender 
     * can immediately call receive (optionally providing a timeout value) in order to block while waiting for a reply Message. This is very 
     * similar to the implementation used internally by many of Spring Integration’s request-reply components.
     * 
     * 
     * Keep in mind that all of these queue-based channels are storing messages in-memory only by default. When persistence is required, you 
     * can either provide a 'message-store' attribute within the 'queue' element to reference a persistent MessageStore implementation or you 
     * can replace the local channel with one that is backed by a persistent broker, such as a JMS-backed channel or channel adapter. The 
     * latter option lets you take advantage of any JMS provider’s implementation for message persistence, as discussed in JMS Support. 
     * However, when buffering in a queue is not necessary, the simplest approach is to rely upon the DirectChannel, discussed in the next 
     * section. 
     * 
     * DirectChannel
     * 
     * The DirectChannel has point-to-point semantics but otherwise is more similar to the PublishSubscribeChannel than any of the queue-based 
     * channel implementations described earlier. It implements the SubscribableChannel interface instead of the PollableChannel interface, so 
     * it dispatches messages directly to a subscriber. As a point-to-point channel, however, it differs from the PublishSubscribeChannel in that 
     * it sends each Message to a single subscribed MessageHandler.
     * 
     * In addition to being the simplest point-to-point channel option, one of its most important features is that it enables a single thread to 
     * perform the operations on “both sides” of the channel. For example, if a handler subscribes to a DirectChannel, then sending a Message to 
     * that channel triggers invocation of that handler’s handleMessage(Message) method directly in the sender’s thread, before the send() 
     * method invocation can return.
     * 
     * The key motivation for providing a channel implementation with this behavior is to support transactions that must span across the channel 
     * while still benefiting from the abstraction and loose coupling that the channel provides. If the send call is invoked within the scope of 
     * a transaction, the outcome of the handler’s invocation (for example, updating a database record) plays a role in determining the ultimate 
     * result of that transaction (commit or rollback).
     * 
     * The DirectChannel internally delegates to a message dispatcher to invoke its subscribed message handlers, and that dispatcher can have a 
     * load-balancing strategy exposed by load-balancer or load-balancer-ref attributes (mutually exclusive). The load balancing strategy is 
     * used by the message dispatcher to help determine how messages are distributed amongst message handlers when multiple message handlers 
     * subscribe to the same channel. As a convenience, the load-balancer attribute exposes an enumeration of values pointing to pre-existing 
     * implementations of LoadBalancingStrategy. A round-robin (load-balances across the handlers in rotation) and none (for the cases where one 
     * wants to explicitly disable load balancing) are the only available values. Other strategy implementations may be added in future versions. 
     * However, since version 3.0, you can provide your own implementation of the LoadBalancingStrategy and inject it by using the 
     * load-balancer-ref attribute, which should point to a bean that implements LoadBalancingStrategy, as the following example shows:
     * 
     * A FixedSubscriberChannel is a SubscribableChannel that only supports a single MessageHandler subscriber that cannot be unsubscribed. This 
     * is useful for high-throughput performance use-cases when no other subscribers are involved and no channel interceptors are needed.
     * 
     * The load-balancing also works in conjunction with a boolean failover property. If the failover value is true (the default), the 
     * dispatcher falls back to any subsequent handlers (as necessary) when preceding handlers throw exceptions. The order is determined by 
     * an optional order value defined on the handlers themselves or, if no such value exists, the order in which the handlers subscribed.
     * 
     * If a certain situation requires that the dispatcher always try to invoke the first handler and then fall back in the same fixed order 
     * sequence every time an error occurs, no load-balancing strategy should be provided. In other words, the dispatcher still supports the 
     * failover boolean property even when no load-balancing is enabled. Without load-balancing, however, the invocation of handlers always 
     * begins with the first, according to their order. For example, this approach works well when there is a clear definition of primary, 
     * secondary, tertiary, and so on. When using the namespace support, the order attribute on any endpoint determines the order.
     * 
     * Keep in mind that load-balancing and failover apply only when a channel has more than one subscribed message handler. When using the 
     * namespace support, this means that more than one endpoint shares the same channel reference defined in the input-channel attribute. 
     * 
     * Starting with version 5.2, when failover is true, a failure of the current handler together with the failed message is logged under debug 
     * or info if configured respectively.
     * 
     * ExecutorChannel
     * 
     * The ExecutorChannel is a point-to-point channel that supports the same dispatcher configuration as DirectChannel (load-balancing strategy 
     * and the failover boolean property). The key difference between these two dispatching channel types is that the ExecutorChannel delegates 
     * to an instance of TaskExecutor to perform the dispatch. This means that the send method typically does not block, but it also means that 
     * the handler invocation may not occur in the sender’s thread. It therefore does not support transactions that span the sender and 
     * receiving handler.
     * 
     * the sender can sometimes block. For example, when using a TaskExecutor with a rejection policy that throttles the client (such as the 
     * ThreadPoolExecutor.CallerRunsPolicy), the sender’s thread can execute the method any time the thread pool is at its maximum capacity and 
     * the executor’s work queue is full. Since that situation would only occur in a non-predictable way, you should not rely upon it for 
     * transactions. 
     * 
     * FluxMessageChannel
     * 
     * The FluxMessageChannel is an org.reactivestreams.Publisher implementation for "sinking" sent messages into an internal 
     * reactor.core.publisher.Flux for on demand consumption by reactive subscribers downstream. This channel implementation is neither a 
     * SubscribableChannel, nor a PollableChannel, so only org.reactivestreams.Subscriber instances can be used to consume from this 
     * channel honoring back-pressure nature of reactive streams. On the other hand, the FluxMessageChannel implements a 
     * ReactiveStreamsSubscribableChannel with its subscribeTo(Publisher<Message<?>>) contract allowing receiving events from reactive 
     * source publishers, bridging a reactive stream into the integration flow. To achieve fully reactive behavior for the whole integration 
     * flow, such a channel must be placed between all the endpoints in the flow.
     * 
     */
    
    /**
     * Message Handler Chain
     * 
     * The MessageHandlerChain is an implementation of MessageHandler that can be configured as a single message endpoint while actually 
     * delegating to a chain of other handlers, such as filters, transformers, splitters, and so on. 
     * 
     * When several handlers need to be connected in a fixed, linear progression, this can lead to a much simpler configuration. 
     * 
     * For example, it is fairly common to provide a transformer before other components. Similarly, when you provide a filter before some 
     * other component in a chain, you essentially create a selective consumer. 
     * 
     * In either case, the chain requires only a single input-channel and a single output-channel, eliminating the need to define channels 
     * for each individual component.
     * 
     * The MessageHandlerChain is mostly designed for an XML configuration. For Java DSL, an IntegrationFlow definition can be treated as a 
     * chain component, but it has nothing to do with concepts and principles described in this chapter below. See Java DSL for more 
     * information. 
     * 
     * Spring Integration’s Filter provides a boolean property: throwExceptionOnRejection. When you provide multiple selective consumers on the same point-to-point channel with different acceptance criteria, you should set this value 'true' (the default is false) so that the dispatcher knows that the message was rejected and, as a result, tries to pass the message on to other subscribers. If the exception were not thrown, it would appear to the dispatcher that the message had been passed on successfully even though the filter had dropped the message to prevent further processing. If you do indeed want to “drop” the messages, the filter’s 'discard-channel' might be useful, since it does give you a chance to perform some operation with the dropped message (such as sending it to a JMS queue or writing it to a log). 
     * 
     * The handler chain simplifies configuration while internally maintaining the same degree of loose coupling between components, and it is 
     * trivial to modify the configuration if at some point a non-linear arrangement is required.
     * 
     * Internally, the chain is expanded into a linear setup of the listed endpoints, separated by anonymous channels.
     * 
     * The reply channel header is not taken into account within the chain.
     * 
     * Only after the last handler is invoked is the resulting message forwarded to the reply channel or the chain’s output channel. 
     * 
     * Because of this setup, all handlers except the last must implement the MessageProducer interface (which provides a 'setOutputChannel()' 
     * method). If the outputChannel on the MessageHandlerChain is set, the last handler needs only an output channel.
     * 
     * As with other endpoints, the output-channel is optional. If there is a reply message at the end of the chain, the output-channel takes 
     * precedence. However, if it is not available, the chain handler checks for a reply channel header on the inbound message as a fallback. 
     * 
     *  */ 

    /**
     * Thread Barrier
     * 
     * Sometimes, we need to suspend a message flow thread until some other asynchronous event occurs. For example, consider an HTTP request 
     * that publishes a message to RabbitMQ. We might wish to not reply to the user until the RabbitMQ broker has issued an acknowledgment 
     * that the message was received
     * 
     * In version 4.2, Spring Integration introduced the <barrier/> component for this purpose. The underlying MessageHandler is the 
     * BarrierMessageHandler. This class also implements MessageTriggerAction, in which a message passed to the trigger() method releases a 
     * corresponding thread in the handleRequestMessage() method (if present).
     * 
     * The suspended thread and trigger thread are correlated by invoking a CorrelationStrategy on the messages. When a message is sent to 
     * the input-channel, the thread is suspended for up to requestTimeout milliseconds, waiting for a corresponding trigger message. The 
     * default correlation strategy uses the IntegrationMessageHeaderAccessor.CORRELATION_ID header. When a trigger message arrives with 
     * the same correlation, the thread is released. The message sent to the output-channel after release is constructed by using a 
     * MessageGroupProcessor. By default, the message is a Collection<?> of the two payloads, and the headers are merged by using a 
     * DefaultAggregatingMessageGroupProcessor.
     * 
     * he requires-reply property determines the action to take if the suspended thread times out before the trigger message arrives. By 
     * default, it is false, which means the endpoint returns null, the flow ends, and the thread returns to the caller. When true, a 
     * ReplyRequiredException is thrown.
     * 
     * You can call the trigger() method programmatically (obtain the bean reference by using the name, barrier.handler — where barrier is 
     * the bean name of the barrier endpoint). Alternatively, you can configure an <outbound-channel-adapter/> to trigger the release
     * 
     * The following example shows how to use a custom header for correlation:
     *   
     *   
     *   @ServiceActivator(inputChannel="in")
     *   @Bean
     *   public BarrierMessageHandler barrier(MessageChannel out, MessageChannel lateTriggerChannel) {
     *       BarrierMessageHandler barrier = new BarrierMessageHandler(10000);
     *       barrier.setOutputChannel(out());
     *       barrier.setDiscardChannel(lateTriggerChannel);
     *       return barrier;
     *   }
     *   
     *   @ServiceActivator (inputChannel="release")
     *   @Bean
     *   public MessageHandler releaser(MessageTriggerAction barrier) {
     *       return barrier::trigger(message);
     *   }
     *   
     * Depending on which one has a message arrive first, either the thread sending a message to in or the thread sending a message to 
     * release waits for up to ten seconds until the other message arrives. When the message is released, the out channel is sent a message 
     * that combines the result of invoking the custom MessageGroupProcessor bean, named myOutputProcessor. If the main thread times out 
     * and a trigger arrives later, you can configure a discard channel to which the late trigger is sen
     * 
     * */ 

    /**
     * To recap:
     *   -
     *   - Inbound channel adapters are used for one-way integration to bring data into the messaging application.
     *   -
     *   - Outbound channel adapters are used for one-way integration to send data out of the messaging application.
     *   
     *   - Inbound gateways are used for a bidirectional integration flow, where some other system invokes the messaging application and receives a reply.
     *   
     *   - Outbound Gateways are used for a bidirectional integration flow, where the messaging application invokes some external service or entity and expects a result.
     * 
     *  */ 

}
