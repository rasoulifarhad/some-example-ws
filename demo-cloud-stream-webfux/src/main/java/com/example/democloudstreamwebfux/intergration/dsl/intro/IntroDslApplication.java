package com.example.democloudstreamwebfux.intergration.dsl.intro;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.channel.interceptor.WireTap;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.dsl.Transformers;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;

import com.example.democloudstreamwebfux.intergration.ChannelConfiguration;
import com.example.democloudstreamwebfux.intergration.ChannelConfiguration.TestStringChannels;

import lombok.extern.slf4j.Slf4j;
    /**
     * Java DSL
     * 
     * The Spring Integration Java configuration and DSL provides a set of convenient builders and a fluent API that lets you configure Spring 
     * Integration message flows from Spring @Configuration classes.
     * 
     * The Java DSL for Spring Integration is essentially a facade for Spring Integration.
     * 
     * The DSL provides a simple way to embed Spring Integration Message Flows into your application by using the fluent Builder pattern together 
     * with existing Java configuration from Spring Framework and Spring Integration. We also use and support lambdas (available with Java 8) to 
     * further simplify Java configuration.
     * 
     * The DSL is presented by the IntegrationFlows factory for the IntegrationFlowBuilder.
     * 
     * This produces the IntegrationFlow component, which should be registered as a Spring bean (by using the @Bean annotation). 
     * The builder pattern is used to express arbitrarily complex structures as a hierarchy of methods that can accept lambdas as arguments.
     * 
     * The IntegrationFlowBuilder only collects integration components (MessageChannel instances, AbstractEndpoint instances, and so on) in the 
     * IntegrationFlow bean for further parsing and registration of concrete beans in the application context by the 
     * IntegrationFlowBeanPostProcessor.
     * 
     * The Java DSL uses Spring Integration classes directly and bypasses any XML generation and parsing.
     * However, the DSL offers more than syntactic sugar on top of XML.
     * 
     * One of its most compelling features is the ability to define inline lambdas to implement endpoint logic, eliminating the need for 
     * external classes to implement custom logic. In some sense, Spring Integration’s support for the Spring Expression Language (SpEL) and 
     * inline scripting address this, but lambdas are easier and much more powerful.
     * 
     * The following example shows how to use Java Configuration for Spring Integration:
     * 
     *   @Configuration
     *   @EnableIntegration
     *   public class MyConfiguration {
     *      @Bean
     *      public AtomicInteger integerSource() {
     *      return new AtomicInteger();
     *      }
     *      @Bean
     *      public IntegrationFlow myFlow() {
     *         return IntegrationFlows.fromSupplier(integerSource()::getAndIncrement,
     *                                       c -> c.poller(Pollers.fixedRate(100)))
     *                     .channel("inputChannel")
     *                     .filter((Integer p) -> p > 0)
     *                     .transform(Object::toString)
     *                     .channel(MessageChannels.queue())
     *                     .get();
     *      }
     *   }
     * 
     * The result of the preceding configuration example is that it creates, after ApplicationContext start up, Spring Integration endpoints 
     * and message channels.
     * 
     * 
     */
    /**
     * DSL Basics
     * 
     * The org.springframework.integration.dsl package contains the IntegrationFlowBuilder API mentioned earlier and a number of 
     * IntegrationComponentSpec implementations, which are also builders and provide the fluent API to configure concrete endpoints.
     * 
     * The IntegrationFlowBuilder infrastructure provides common enterprise integration patterns (EIP) for message-based applications, such 
     * as channels, endpoints, pollers, and channel interceptors.
     * 
     * Endpoints are expressed as verbs in the DSL to improve readability.
     * The following list includes the common DSL method names and the associated EIP endpoint:
     * 
     *    • transform → Transformer
     *    • filter → Filter
     *    • handle → ServiceActivator
     *    • split → Splitter
     *    • aggregate → Aggregator
     *    • route → Router
     *    • bridge → Bridge
     * 
     * Conceptually, integration processes are constructed by composing these endpoints into one or more message flows. Note that EIP does not 
     * formally define the term 'message flow', but it is useful to think of it as a unit of work that uses well known messaging patterns. The 
     * DSL provides an IntegrationFlow component to define a composition of channels and endpoints between them, but now IntegrationFlow plays 
     * only the configuration role to populate real beans in the application context and is not used at runtime. However the bean for 
     * IntegrationFlow can be autowired as a Lifecycle to control start() and stop() for the whole flow which is delegated to all the Spring 
     * Integration components associated with this IntegrationFlow. The following example uses the IntegrationFlows factory to define an 
     * IntegrationFlow bean by using EIP-methods from IntegrationFlowBuilder:
     * 
     *   @Bean
     *   public IntegrationFlow integerFlow() {
     *       return IntegrationFlows.from("input")
     *               .<String, Integer>transform(Integer::parseInt)
     *               .get();
     *   }
     * 
     * The transform method accepts a lambda as an endpoint argument to operate on the message payload. The real argument of this method is 
     * GenericTransformer<S, T>. Consequently, any of the provided transformers (ObjectToJsonTransformer, FileToStringTransformer, and other) 
     * can be used here.
     * 
     * Under the covers, IntegrationFlowBuilder recognizes the MessageHandler and the endpoint for it, with MessageTransformingHandler and 
     * ConsumerEndpointFactoryBean, respectively. Consider another example:
     * 
     *   @Bean
     *   public IntegrationFlow myFlow() {
     *       return IntegrationFlows.from("input")
     *                   .filter("World"::equals)
     *                   .transform("Hello "::concat)
     *                   .handle(System.out::println)
     *                   .get();
     *   }
     * 
     * The preceding example composes a sequence of Filter → Transformer → Service Activator. The flow is "'one way'". That is, it does not 
     * provide a reply message but only prints the payload to STDOUT. The endpoints are automatically wired together by using direct channels.
     *
     * Lambdas And Message<?> Arguments
     * 
     * When using lambdas in EIP methods, the "input" argument is generally the message payload. If you wish to access the entire message, use 
     * one of the overloaded methods that take a Class<?> as the first parameter. For example, this won’t work:
     * 
     *      .<Message<?>, Foo>transform(m -> newFooFromMessage(m))
     * 
     * This will fail at runtime with a ClassCastException because the lambda doesn’t retain the argument type and the framework will attempt 
     * to cast the payload to a Message<?>.
     * 
     * Instead, use:
     * 
     *       .(Message.class, m -> newFooFromMessage(m))
     * 
     * The Java DSL can register beans for the object defined in-line in the flow definition, as well as can reuse existing, injected beans. In 
     * case of the same bean name defined for in-line object and existing bean definition, a BeanDefinitionOverrideException is thrown 
     * indicating that such a configuration is wrong. However when you deal with prototype beans, there is no way to detect from the integration 
     * flow processor an existing bean definition because every time we call a prototype bean from the BeanFactory we get a new instance. This
     *  way a provided instance is used in the IntegrationFlow as is without any bean registration and any possible check against existing 
     * prototype bean definition. However BeanFactory.initializeBean() is called for this object if it has an explicit id and bean definition 
     * for this name is in prototype scope.
     *  
     * 
     */

    /**
     * Message Channels
     * 
     * In addition to the IntegrationFlowBuilder with EIP methods, the Java DSL provides a fluent API to configure MessageChannel instances. For 
     * this purpose the MessageChannels builder factory is provided. The following example shows how to use it:
     * 
     *   @Bean
     *   public MessageChannel priorityChannel() {
     *       return MessageChannels.priority(this.mongoDbChannelMessageStore, "priorityGroup")
     *                           .interceptor(wireTap())
     *                           .get();
     *   }
     *   
     * 
     * The same MessageChannels builder factory can be used in the channel() EIP method from IntegrationFlowBuilder to wire endpoints, similar 
     * to wiring an input-channel/output-channel pair in the XML configuration. By default, endpoints are wired with DirectChannel instances 
     * where the bean name is based on the following pattern: [IntegrationFlow.beanName].channel#[channelNameIndex]. This rule is also applied 
     * for unnamed channels produced by inline MessageChannels builder factory usage. However all MessageChannels methods have a variant that is 
     * aware of the channelId that you can use to set the bean names for MessageChannel instances. The MessageChannel references and beanName 
     * can be used as bean-method invocations. The following example shows the possible ways to use the channel() EIP method
     * 
     *   @Bean
     *   public MessageChannel queueChannel() {
     *       return MessageChannels.queue().get();
     *   }
     *   
     *   @Bean
     *   public MessageChannel publishSubscribe() {
     *       return MessageChannels.publishSubscribe().get();
     *   }
     *   
     *   @Bean
     *   public IntegrationFlow channelFlow() {
     *       return IntegrationFlows.from("input")
     *                   .fixedSubscriberChannel()
     *                   .channel("queueChannel")
     *                   .channel(publishSubscribe())
     *                   .channel(MessageChannels.executor("executorChannel", this.taskExecutor))
     *                   .channel("output")
     *                   .get();
     *   }
     * 
     *    - from("input") means "'find and use the MessageChannel with the "input" id, or create one'".
     *    - 
     *    - fixedSubscriberChannel() produces an instance of FixedSubscriberChannel and registers it with a name of channelFlow.channel#0.
     *    - 
     *    - channel("queueChannel") works the same way but uses an existing queueChannel bean.
     *    - 
     *    - channel(publishSubscribe()) is the bean-method reference.
     *    - 
     *    - channel(MessageChannels.executor("executorChannel", this.taskExecutor)) is the IntegrationFlowBuilder that exposes 
     *      IntegrationComponentSpec to the ExecutorChannel and registers it as executorChannel.
     *    - 
     *    - channel("output") registers the DirectChannel bean with output as its name, as long as no beans with this name already exist.
     *    -
     * 
     * Note: The preceding IntegrationFlow definition is valid, and all of its channels are applied to endpoints with BridgeHandler instances.
     * 
     * Be careful to use the same inline channel definition through MessageChannels factory from different IntegrationFlow instances. Even if 
     * the DSL parser registers non-existent objects as beans, it cannot determine the same object (MessageChannel) from different 
     * IntegrationFlow containers. The following example is wrong: 
     * 
     *   @Bean
     *   public IntegrationFlow startFlow() {
     *       return IntegrationFlows.from("input")
     *                   .transform(...)
     *                   .channel(MessageChannels.queue("queueChannel"))
     *                   .get();
     *   }
     *   
     *   @Bean
     *   public IntegrationFlow endFlow() {
     *       return IntegrationFlows.from(MessageChannels.queue("queueChannel"))
     *                   .handle(...)
     *                   .get();
     *   }
     * 
     * The result of that bad example is the following exception:
     *   
     *   Caused by: java.lang.IllegalStateException:
     *   Could not register object [queueChannel] under bean name 'queueChannel':
     *        there is already object [queueChannel] bound
	 *           at o.s.b.f.s.DefaultSingletonBeanRegistry.registerSingleton(DefaultSingletonBeanRegistry.java:129)
     * 
     * To make it work, you need to declare @Bean for that channel and use its bean method from different IntegrationFlow instances.

     * 
     * 
     * 
     * 
     * 
     *  */ 


@Slf4j
public class IntroDslApplication {
    

    @EnableAutoConfiguration
    @Configuration
    @IntegrationComponentScan
    @Slf4j
    public static class DslApplication {
        public static void main(String[] args) throws Exception {
              new SpringApplicationBuilder()
                      .sources(DslApplication.class,TestStringChannels.class,ChannelConfiguration.class,
                      DslConfig.class
                                          )
                      .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/intergration/dsl/intro/")
                      .run("--management.endpoints.web.exposure.include=*","--spring.main.lazy-initialization=false" 
                                  ,"--logging.level.root=INFO" 
                                  ,"--logging.level.org.springframework.retry.support.RetryTemplate=DEBUG"
                                  ,"--spring.cloud.stream.defaultBinder=rabbit");
        }
    }

    @Configuration
    @Slf4j
    public static class DslConfig {

        @Bean
        QueueChannel inputChannel() {
            return MessageChannels
                              .queue()
                              .datatype(String.class)
                              .get()
                              ;
        }

        @Bean
        public DirectChannel loggerChannel() {
            return MessageChannels.direct().datatype(String.class).get();
        }

        @Bean
        public WireTap wireTap() {
            return new WireTap(loggerChannel());
        }

        @Bean
        @ServiceActivator(inputChannel = "loggerChannel")
        public LoggingHandler loggingHandler() {
            return new LoggingHandler("info");
        }


        @Bean
        public IntegrationFlow helloWorldFlow() {

            return IntegrationFlows
                              .from(inputChannel())
                              .intercept(wireTap())
                              .filter("World"::equals)
                              .transform("Hello "::concat)
                              .handle(message -> log.info("helloWorldFlow: {}",message) )
                              .get()
                              ;
        }

        /**
         * The result of this definition is the same set of integration components that are wired with an implicit direct channel. The only 
         * limitation here is that this flow is started with a named direct channel - lambdaFlow.input .
         * 
         * Also, a Lambda flow cannot start from MessageSource or MessageProducer.
         * 
         * Starting with version 5.1, this kind of IntegrationFlow is wrapped to the proxy to expose lifecycle control and provide access to 
         * the inputChannel of the internally associated StandardIntegrationFlow.
         * 
         * Starting with version 5.0.6, the generated bean names for the components in an IntegrationFlow include the flow bean followed by a dot (.) 
         * as a prefix. For example, the ConsumerEndpointFactoryBean for the .transform("Hello "::concat) in the preceding sample results in a bean 
         * name of lambdaFlow.o.s.i.config.ConsumerEndpointFactoryBean#0. (The o.s.i is a shortened from org.springframework.integration to fit on 
         * the page.) The Transformer implementation bean for that endpoint has a bean name of lambdaFlow.transformer#0 (starting with version 5.1), 
         * where instead of a fully qualified name of the MethodInvokingTransformer class, its component type is used. The same pattern is applied 
         * for all the NamedComponent s when the bean name has to be generated within the flow. These generated bean names are prepended with the 
         * flow ID for purposes such as parsing logs or grouping components together in some analysis tool, as well as to avoid a race condition when 
         * we concurrently register integration flows at runtime. 
         * 
         * @return
         */
        @Bean
        public IntegrationFlow helloWorldLambdaFlow() {
            return flow -> flow                        // inputChannel name : helloWorldLambdaFlow.input
                              .intercept(wireTap())
                              .filter("World"::equals)
                              .transform("Hello "::concat)
                              .handle(message -> log.info("helloWorldLambdaFlow: {}",message));
        }

        @Bean
        public MessagingTemplate messagingTemplate() {
            return new MessagingTemplate();
        }

        @Bean
        @Order(30)
        public ApplicationRunner helloWorldFlowRunner(QueueChannel inputChannel, MessagingTemplate messagingTemplate) {
            return args -> {
                          inputChannel.send(new GenericMessage<String>("Hiiii"));
                          inputChannel.send(new GenericMessage<String>("World"));
                          inputChannel.send(new GenericMessage<String>("Farhad"));
                          inputChannel.send(new GenericMessage<String>("Good"));

                          messagingTemplate.send("helloWorldLambdaFlow.input", new GenericMessage<String>("Hiiii Lambda"));;
                          messagingTemplate.send("helloWorldLambdaFlow.input", new GenericMessage<String>("World"));;
                          messagingTemplate.send("helloWorldLambdaFlow.input", new GenericMessage<String>("Farhad Lambda"));;
                          messagingTemplate.send("helloWorldLambdaFlow.input", new GenericMessage<String>("Good Lambda"));;
            };
        }

    }

    @EnableAutoConfiguration
    @Configuration
    @IntegrationComponentScan
    @Slf4j
    public static class DslApplication2 {
        public static void main(String[] args) throws Exception {
              new SpringApplicationBuilder()
                      .sources(DslApplication2.class,TestStringChannels.class,ChannelConfiguration.class,
                      DslConfig2.class
                                          )
                      .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/intergration/dsl/intro/")
                      .run("--management.endpoints.web.exposure.include=*","--spring.main.lazy-initialization=false" 
                                  ,"--logging.level.root=INFO" 
                                  ,"--logging.level.org.springframework.retry.support.RetryTemplate=DEBUG"
                                  ,"--spring.cloud.stream.defaultBinder=rabbit");
        }
    }

    @Configuration
    @Slf4j
    public static class DslConfig2 {

        @Bean
        public QueueChannel queueChannel1() {
            return MessageChannels.queue().datatype(Integer.class).get();
        }

        @Bean
        public AtomicInteger integerSource() {
            return new AtomicInteger() ;
        }
        @Bean
        public IntegrationFlow sendFlow() {
            return IntegrationFlows
                              .fromSupplier(integerSource()::getAndIncrement
                                                           ,e -> e.poller(Pollers.fixedRate(1000)))
                              .channel(queueChannel1())
                              .get();
        }

        @Bean
        public IntegrationFlow myFlow() {

            return IntegrationFlows
                              .from(queueChannel1() )
                              .bridge(e -> e.poller(Pollers.fixedRate(1, TimeUnit.SECONDS,5)))
                            //   .<byte[],String>transform(source -> new String(source))
                              .filter((Integer source) -> source < 10  )
                              .handle(Integer.class, (payload, headers) ->  payload * 2 )
                              .handle(Integer.class, (payload, headers) -> {
                                log.info("myFlow: {}---{}",payload , headers);
                                return payload;
                              } 
                              )
                              .channel("nullChannel")
                              .get();
        }

        @Bean
        @Order(20)
        public ApplicationRunner registerChannels() {
            return args -> {
            };
        }

    }

    /**
     * Operator gateway()
     * 
     * The gateway() operator in an IntegrationFlow definition is a special service activator implementation, to call some other endpoint or 
     * integration flow via its input channel and wait for reply. Technically it plays the same role as a nested <gateway> component in a 
     * <chain> definition (see Calling a Chain from within a Chain) and allows a flow to be cleaner and more straightforward. Logically, and 
     * from business perspective, it is a messaging gateway to allow the distribution and reuse of functionality between different parts of the 
     * target integration solution (see Messaging Gateways). This operator has several overloads for different goals:
     * 
     *     - gateway(String requestChannel) to send a message to some endpoint’s input channel by its name;
     * 
     *     - gateway(MessageChannel requestChannel) to send a message to some endpoint’s input channel by its direct injection;
     * 
     *     - gateway(IntegrationFlow flow) to send a message to the input channel of the provided IntegrationFlow.
     * 
     * All of these have a variant with the second Consumer<GatewayEndpointSpec> argument to configure the target GatewayMessageHandler and 
     * respective AbstractEndpoint. Also the IntegrationFlow-based methods allows calling existing IntegrationFlow bean or declare the flow as 
     * a sub-flow via an in-place lambda for an IntegrationFlow functional interface or have it extracted in a private method cleaner code
     * style:
     * 
     *   @Bean
     *   IntegrationFlow someFlow() {
     *           return IntegrationFlows
     *                   .from(...)
     *                   .gateway(subFlow())
     *                   .handle(...)
     *                   .get();
     *   }
     *   
     *   private static IntegrationFlow subFlow() {
     *           return f -> f
     *                   .scatterGather(s -> s.recipientFlow(...),
     *                           g -> g.outputProcessor(MessageGroup::getOne))
     *   }
     * 
     * If the downstream flow does not always return a reply, you should set the requestTimeout to 0 to prevent hanging the calling thread 
     * indefinitely. In that case, the flow will end at that point and the thread released for further work. 
     * 
     * 
     */
    /**
     * Operator log
     * 
     * For convenience, to log the message journey through the Spring Integration flow (<logging-channel-adapter>), a log() operator is 
     * presented. Internally, it is represented by the WireTap ChannelInterceptor with a LoggingHandler as its subscriber. It is responsible 
     * for logging the incoming message into the next endpoint or the current channel. The following example shows how to use LoggingHandler:
     * 
     *   .filter(...)
     *   .log(LoggingHandler.Level.ERROR, "test.category", m -> m.getHeaders().getId())
     *   .route(...)
     * 
     * In the preceding example, an id header is logged at the ERROR level onto test.category only for messages that passed the filter and 
     * before routing.
     * 
     * When this operator is used at the end of a flow, it is a one-way handler and the flow ends. To make it as a reply-producing flow, you 
     * can either use a simple bridge() after the log() or, starting with version 5.1, you can use a logAndReply() operator instead. 
     * logAndReply can only be used at the end of a flow.
     * 
     */
    /**
     * Operator intercept()
     * 
     * Starting with version 5.3, the intercept() operator allows to register one or more ChannelInterceptor instances at the current 
     * MessageChannel in the flow. This is an alternative to creating an explicit MessageChannel via the MessageChannels API. The following 
     * example uses a MessageSelectingInterceptor to reject certain messages with an exception:
     * 
     *   .transform(...)
     *   .intercept(new MessageSelectingInterceptor(m -> m.getPayload().isValid()))
     *   .handle(...)
     */
    /**
     * MessageChannelSpec.wireTap()
     * 
     * Spring Integration includes a .wireTap() fluent API MessageChannelSpec builders. The following example shows how to use the wireTap 
     * method to log input:
     * 
     *   @Bean
     *   public QueueChannelSpec myChannel() {
     *       return MessageChannels.queue()
     *               .wireTap("loggingFlow.input");
     *   }
     *   
     *   @Bean
     *   public IntegrationFlow loggingFlow() {
     *   return f -> f.log();
     *   }
     * 
     * If the MessageChannel is an instance of InterceptableChannel, the log(), wireTap() or intercept() operators are applied to the 
     * current MessageChannel. Otherwise, an intermediate DirectChannel is injected into the flow for the currently configured endpoint. In 
     * the following example, the WireTap interceptor is added to myChannel directly, because DirectChannel implements InterceptableChannel:
     * 
     *   @Bean
     *   MessageChannel myChannel() {
     *       return new DirectChannel();
     *   }
     *   
     *   ...
     *       .channel(myChannel())
     *       .log()
     *   }
     * 
     * When the current MessageChannel does not implement InterceptableChannel, an implicit DirectChannel and BridgeHandler are injected 
     * into the IntegrationFlow, and the WireTap is added to this new DirectChannel. The following example does not have any channel 
     * declaration:
     * 
     *   .handle(...)
     *   .log()
     *   }
     * 
     * In the preceding example (and any time no channel has been declared), an implicit DirectChannel is injected in the current position 
     * of the IntegrationFlow and used as an output channel for the currently configured ServiceActivatingHandler (from the .handle(), 
     * described earlier).
     * 
     * 
     */
    @EnableAutoConfiguration
    @Configuration
    @IntegrationComponentScan
    @Slf4j
    public static class DslGatewayApplication {
        public static void main(String[] args) throws Exception {
              new SpringApplicationBuilder()
                      .sources(DslGatewayApplication.class,TestStringChannels.class,ChannelConfiguration.class,
                      DslGatewayConfig.class
                                          )
                      .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/intergration/dsl/intro/")
                      .run("--management.endpoints.web.exposure.include=*","--spring.main.lazy-initialization=false" 
                                  ,"--logging.level.root=INFO" 
                                  ,"--logging.level.org.springframework.retry.support.RetryTemplate=DEBUG"
                                  ,"--spring.cloud.stream.defaultBinder=rabbit");
        }

        public interface ToUpperCaseGateway {

           public String toUpperCase(String value) ;

        }
        
        @Configuration
        @Slf4j
        public static class DslGatewayConfig {

            @Bean
            public IntegrationFlow toUpperCaseFlow() {
                return IntegrationFlows
                                  .from(ToUpperCaseGateway.class)
                                  .handle(String.class,(payload, headers) -> payload.toUpperCase())
                                  .get();
            }

            public interface StringFunction extends Function<String,String> {

            }

            @Bean
            public IntegrationFlow toUpperCaseFunctionFlow() {
                return IntegrationFlows
                                 .from(StringFunction.class , gateway -> gateway.beanName("toUpperCaseFunction") )
                                 .handle(String.class, (payload, headers) -> payload.toUpperCase()/* ,t -> t.advice()*/ )
                                 .get();
            }

            @Bean
            @Order(30)
            public ApplicationRunner gatewayrunner(@Qualifier("toUpperCaseFlow.gateway") ToUpperCaseGateway toUpperCaseGateway,
                                                   @Qualifier("toUpperCaseFunction")  StringFunction stringFunction) {
                return args -> {
                    log.info("{}",toUpperCaseGateway.toUpperCase("Helllooooo"));
                    log.info("{}",toUpperCaseGateway.toUpperCase("Helllooooo2"));
                    log.info("{}",toUpperCaseGateway.toUpperCase("Helllooooo3"));
                    log.info("{}",toUpperCaseGateway.toUpperCase("Helllooooo4"));
                    log.info("{}",toUpperCaseGateway.toUpperCase("Helllooooo5"));
                    log.info("{}",toUpperCaseGateway.toUpperCase("Helllooooo6"));


                    log.info("{}",stringFunction.apply("Hellllllloooo----"));
                    log.info("{}",stringFunction.apply("Hellllllloooo2----"));
                };
            }


        }
    }

    /**
     * Dynamic and Runtime Integration Flows
     * 
     * IntegrationFlow and all its dependent components can be registered at runtime. Before version 5.0, we used the 
     * BeanFactory.registerSingleton() hook. Starting in the Spring Framework 5.0, we use the instanceSupplier hook for programmatic 
     * BeanDefinition registration. The following example shows how to programmatically register a bean:
     * 
     * 
     *   BeanDefinition beanDefinition =
     *            BeanDefinitionBuilder.genericBeanDefinition((Class<Object>) bean.getClass(), () -> bean)
     *                  .getRawBeanDefinition();
     *
     *   ((BeanDefinitionRegistry) this.beanFactory).registerBeanDefinition(beanName, beanDefinition);
     * 
     * Note that, in the preceding example, the instanceSupplier hook is the last parameter to the genericBeanDefinition method, provided 
     * by a lambda in this case.
     * 
     * All the necessary bean initialization and lifecycle is done automatically, as it is with the standard context configuration bean 
     * definitions.
     * 
     *  To simplify the development experience, Spring Integration introduced IntegrationFlowContext to register and manage IntegrationFlow 
     * instances at runtime, as the following example shows:
     * 
     * 
     *   @Autowired
     *   private AbstractServerConnectionFactory server1;
     *   
     *   @Autowired
     *   private IntegrationFlowContext flowContext;
     *   
     *   ...
     *   
     *   @Test
     *   public void testTcpGateways() {
     *       TestingUtilities.waitListening(this.server1, null);
     *   
     *       IntegrationFlow flow = f -> f
     *               .handle(Tcp.outboundGateway(Tcp.netClient("localhost", this.server1.getPort())
     *                       .serializer(TcpCodecs.crlf())
     *                       .deserializer(TcpCodecs.lengthHeader1())
     *                       .id("client1"))
     *                   .remoteTimeout(m -> 5000))
     *               .transform(Transformers.objectToString());
     *   
     *       IntegrationFlowRegistration theFlow = this.flowContext.registration(flow).register();
     *       assertThat(theFlow.getMessagingTemplate().convertSendAndReceive("foo", String.class), equalTo("FOO"));
     *   }
     *   
     *   This is useful when we have multiple configuration options and have to create several instances of similar flows. To do so, we can 
     * iterate our options and create and register IntegrationFlow instances within a loop. Another variant is when our source of data is 
     * not Spring-based and we must create it on the fly. Such a sample is Reactive Streams event source, as the following example shows:
     * 
     * 
     *   Flux<Message<?>> messageFlux =
     *       Flux.just("1,2,3,4")
     *           .map(v -> v.split(","))
     *           .flatMapIterable(Arrays::asList)
     *           .map(Integer::parseInt)
     *           .map(GenericMessage<Integer>::new);
     *   
     *   QueueChannel resultChannel = new QueueChannel();
     *   
     *   IntegrationFlow integrationFlow =
     *       IntegrationFlows.from(messageFlux)
     *           .<Integer, Integer>transform(p -> p * 2)
     *           .channel(resultChannel)
     *           .get();
     *   
     *   this.integrationFlowContext.registration(integrationFlow)
     *               .register();
     *   
     *   The IntegrationFlowRegistrationBuilder (as a result of the IntegrationFlowContext.registration()) can be used to specify a bean 
     * name for the IntegrationFlow to register, to control its autoStartup, and to register, non-Spring Integration beans. Usually, those 
     * additional beans are connection factories (AMQP, JMS, (S)FTP, TCP/UDP, and others.), serializers and deserializers, or any other 
     * required support components.
     *   
     *   You can use the IntegrationFlowRegistration.destroy() callback to remove a dynamically registered IntegrationFlow and all its 
     * dependent beans when you no longer need them. See the IntegrationFlowContext Javadoc for more information.
     *   
     *        Starting with version 5.0.6, all generated bean names in an IntegrationFlow definition are prepended with the flow ID as a 
     * prefix. We recommend always specifying an explicit flow ID. Otherwise, a synchronization barrier is initiated in the 
     * IntegrationFlowContext, to generate the bean name for the IntegrationFlow and register its beans. We synchronize on these two 
     * operations to avoid a race condition when the same generated bean name may be used for different IntegrationFlow instances.
     *   
     *   Also, starting with version 5.0.6, the registration builder API has a new method: useFlowIdAsPrefix(). This is useful if you wish 
     * to declare multiple instances of the same flow and avoid bean name collisions when components in the flows have the same ID, as the 
     * following example shows:
     *   
     *   private void registerFlows() {
     *       IntegrationFlowRegistration flow1 =
     *                 this.flowContext.registration(buildFlow(1234))
     *                       .id("tcp1")
     *                       .useFlowIdAsPrefix()
     *                       .register();
     *   
     *       IntegrationFlowRegistration flow2 =
     *                 this.flowContext.registration(buildFlow(1235))
     *                       .id("tcp2")
     *                       .useFlowIdAsPrefix()
     *                       .register();
     *   }
     *   
     *   private IntegrationFlow buildFlow(int port) {
     *       return f -> f
     *               .handle(Tcp.outboundGateway(Tcp.netClient("localhost", port)
     *                       .serializer(TcpCodecs.crlf())
     *                       .deserializer(TcpCodecs.lengthHeader1())
     *                       .id("client"))
     *                   .remoteTimeout(m -> 5000))
     *               .transform(Transformers.objectToString());
     *   }
     *   
     *   In this case, the message handler for the first flow can be referenced with bean a name of tcp1.client.handler.
     *   	
     *        An id attribute is required when you usE useFlowIdAsPrefix().
     */
    /**
     * IntegrationFlow as a Gateway
     * 
     * The IntegrationFlow can start from the service interface that provides a GatewayProxyFactoryBean component, as the following example 
     * shows:
     * 
     *   public interface ControlBusGateway {
     *   
     *       void send(String command);
     *   }
     *   
     *   ...
     *   
     *   @Bean
     *   public IntegrationFlow controlBusFlow() {
     *       return IntegrationFlows.from(ControlBusGateway.class)
     *               .controlBus()
     *               .get();
     *   }
     *   
     * All the proxy for interface methods are supplied with the channel to send messages to the next integration component in the 
     * IntegrationFlow. You can mark the service interface with the @MessagingGateway annotation and mark the methods with the @Gateway 
     * annotations. Nevertheless, the requestChannel is ignored and overridden with that internal channel for the next component in the 
     * IntegrationFlow. Otherwise, creating such a configuration by using IntegrationFlow does not make sense.
     *   
     * By default a GatewayProxyFactoryBean gets a conventional bean name, such as [FLOW_BEAN_NAME.gateway]. You can change that ID by using 
     * the @MessagingGateway.name() attribute or the overloaded IntegrationFlows.from(Class<?> serviceInterface, Consumer<GatewayProxySpec> 
     * endpointConfigurer) factory method. Also all the attributes from the @MessagingGateway annotation on the interface are applied to the
     * target GatewayProxyFactoryBean. When annotation configuration is not applicable, the Consumer<GatewayProxySpec> variant can be used for 
     * providing appropriate option for the target proxy. This DSL method is available starting with version 5.2.
     * 
     * With Java 8, you can even create an integration gateway with the java.util.function interfaces, as the following example shows:
     * 
     *   @Bean
     *   public IntegrationFlow errorRecovererFlow() {
     *       return IntegrationFlows.from(Function.class, (gateway) -> gateway.beanName("errorRecovererFunction"))
     *               .handle((GenericHandler<?>) (p, h) -> {
     *                   throw new RuntimeException("intentional");
     *               }, e -> e.advice(retryAdvice()))
     *               .get();
     *   }
     * 
     * That errorRecovererFlow can be used as follows:
     * 
     *   @Autowired
     *   @Qualifier("errorRecovererFunction")
     *   private Function<String, String> errorRecovererFlowGateway;
     *   
     */
    /**
     * Integration Flows Composition
     * 
     * With the MessageChannel abstraction as a first class citizen in Spring Integration, the composition of integration flows was always 
     * assumed. The input channel of any endpoint in the flow can be used to send messages from any other endpoint and not only from the one 
     * which has this channel as an output. Furthermore, with a @MessagingGateway contract, Content Enricher components, composite endpoints 
     * like a <chain>, and now with IntegrationFlow beans (e.g. IntegrationFlowAdapter), it is straightforward enough to distribute the 
     * business logic between shorter, reusable parts. All that is needed for the final composition is knowledge about a MessageChannel to 
     * send to or receive from.
     * 
     * Starting with version 5.5.4, to abstract more from MessageChannel and hide implementation details from the end-user, the 
     * IntegrationFlows introduces the from(IntegrationFlow) factory method to allow starting the current IntegrationFlow from the output 
     * of an existing flow:
     * 
     *   @Bean
     *   IntegrationFlow templateSourceFlow() {
     *       return IntegrationFlows.fromSupplier(() -> "test data")
     *               .channel("sourceChannel")
     *               .get();
     *   }
     *   
     *   @Bean
     *   IntegrationFlow compositionMainFlow(IntegrationFlow templateSourceFlow) {
     *       return IntegrationFlows.from(templateSourceFlow)
     *               .<String, String>transform(String::toUpperCase)
     *               .channel(c -> c.queue("compositionMainFlowResult"))
     *               .get();
     *   }
     * 
     * On the other hand, the IntegrationFlowDefinition has added a to(IntegrationFlow) terminal operator to continue the current flow at 
     * the input channel of some other flow:
     * 
     *   @Bean
     *   IntegrationFlow mainFlow(IntegrationFlow otherFlow) {
     *       return f -> f
     *               .<String, String>transform(String::toUpperCase)
     *               .to(otherFlow);
     *   }
     *   
     *   @Bean
     *   IntegrationFlow otherFlow() {
     *       return f -> f
     *               .<String, String>transform(p -> p + " from other flow")
     *               .channel(c -> c.queue("otherFlowResultChannel"));
     *   }
     * 
     * The composition in the middle of the flow is simply achievable with an existing gateway(IntegrationFlow) EIP-method. This way we can 
     * build flows with any complexity by composing them from simpler, reusable logical blocks. For example, you may add a library of 
     * IntegrationFlow beans as a dependency and it is just enough to have their configuration classes imported to the final project and 
     * autowired for your IntegrationFlow definitions.
     * 
     */
}
