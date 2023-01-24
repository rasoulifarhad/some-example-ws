package com.example.democloudstreamwebfux.intergration.messagehandler;

/**
 * what makes endpoints different enough to give them different names. Several characteristics are important:
 * 
 *   - Polling or event-driven
 * 
 *   - Inbound or outbound
 * 
 *   - Unidirectional or bidirectional
 * 
 *   - Internal or external
 * 
 * Considering all the possible ways we can combine the switches, we end up with 16 candidates for unique names.
 * 
 * Multiply this by the number of supported protocols in case of an external endpoint, and the numbers become dizzying.
 * 
 * Luckily, only certain combinations make sense for a given protocol, which greatly reduces the number of options. 
 * 
 * a gateway is always capable of bidirectional communication. This doesn’t change between HTTP and JMS or between inbound and outbound. 
 * 
 * a channel adapter is always unidirectional: it’s either the beginning (inbound) or the end (outbound) of a message flow. 
 * 
 *   Endpoint                       Polling/Event-driven   Inbound/Outbound   Unidirectional/Bidirectional   Internal/External
 * 
 *   inbound-channel-adapter        Polling                Inbound            Unidirectional                 Internal
 *   outbound-channel-adapter       Either                 Outbound           Unidirectional                 Internal
 *   gateway                        Event-driven           Inbound            Bidirectional                  Internal
 *   service-activator              Either                 Outbound           Bidirectional                  Internal
 *   http:outbound-gateway          Either                 Outbound           Bidirectional                  External
 *   amqp:inbound-channel-adapter   Event-driven           Inbound            Unidirectional                 External
 * 
 * Behavior of an inbound endpoint. Input is taken from an external source and then converted into a message, which is sent to a channel. 
 * 
 * Behavior of an outbound endpoint. First a message is received from a channel; then the message is converted into something the external 
 * component understands. Finally the external API is invoked.
 * 
 * In Spring Integration, a gateway is synonymous with synchronous two-way communication
 * 
 *  If you need an asynchronous gateway, you should compose it from other base components (for example, an inbound and outbound channel adapter).
 * 
 * THE RETURN ADDRESS
 * 
 * When a message reaches an endpoint, through polling or otherwise, two things can happen:
 *  
 *   1- The message is consumed and no response is generated.
 *   2- The message is processed and a response is generated.
 * 
 * The first case is where you’d use a channel adapter.
 * 
 * The second case becomes more complicated, but luckily Spring Integration has gateways to help you support it. The complexity lies in that 
 * you must do something with the response.
 * 
 * Usually you’ll want to either send it further along its way to the next endpoint or send a confirmation back to the original sender when 
 * all processing for this message is done. 
 * 
 * For the first option, you set an output channel, and for the second, you omit the output channel and Spring Integration uses the 
 * REPLY_CHANNEL header to find the channel on which the original sender wants the confirmation to go.
 * 
 * The base class for all endpoints is AbstractEndpoint, and several subtypes exist to take care of the differences between polling and 
 * event-driven behavior.
 * 
 * PollingConsumer wraps a MessageHandler and decorates it with a poller so that it can be connected to any PollableChannel. EventDrivenConsumer 
 * wraps a MessageHandler and connects it to any SubscribableChannel. 
 * 
 * The AbstractPollingEndpoint is more complicated because it has to find a poller and handle exceptions asynchronously. Albeit more complex, this 
 * is still nothing more than a wrapper around a MessageHandler.
 * 
 * ConsumerEndpointFactoryBean
 * 
 * two types of consumer endpoints: PollingConsumer and EventDrivenConsumer. The distinction between them is based on the type of input channel: 
 * PollableChannel or SubscribableChannel, respectively. 
 * 
 * This distinction becomes clear when we investigate the constructors for each of these objects. Both take a MessageHandler that handles the
 * messages sent by a producer to the input channel, but the interface of the expected channel is different.
 * 
 * The PollingConsumer expects a PollableChannel
 * An EventDrivenConsumer expects a SubscribableChannel
 * 
 * primary responsibility of these consumer endpoints is to connect the MessageHandler to an input channel,but what does that mean?
 * 
 * It has to do with the lifecycle management of these components. As you can imagine, that’s also a different issue for PollingConsumers and 
 * EventDrivenConsumers because the former requires an active poller and the latter is passive.
 * 
 * The foundation for managing the lifecycle of any components in Spring Integration is the Spring Framework.
 * The core framework defines a Lifecycle interface: 
 * 
 *   package org.springframework.context;
 *   public interface Lifecycle {
 *      void start();
 *      void stop();
 *      boolean isRunning();
 *   }
 * As you can see, methods are available for starting and stopping a component.
 * 
 * For the EventDrivenConsumer, the start operation consists of calling the SubscribableChannel’s subscribe method while passing the MessageHandler. 
 * Likewise, the unsubscribe method is called from within the stop operation.
 * 
 * The start operation of a PollingConsumer schedules the poller task with the trigger that’s set on that consumer. The trigger implementation may 
 * be either a PeriodicTrigger (for fixed delay or fixed rate) or a CronTrigger. 
 * The stop operation of a PollingConsumer cancels the poller task so that it stops running.
 * 
 * You may wonder who’s responsible for calling these lifecycle methods. Stopping is more straightforward than starting, because when a Spring 
 * ApplicationContext is closing, any lifecycle component whose isRunning method returns true is stopped automatically. 
 * 
 * When it comes to starting, Spring 3.0 added an extension called SmartLifecycle to the Lifecycle interface. SmartLifecycle adds a boolean method, 
 * isAutoStartup(), to indicate whether startup should be automatic. It also extends the new Phased interface with its getPhase() method to provide 
 * the concept of lifecycle phases so that starting and stopping can be ordered.
 * 
 * Spring Integration consumer endpoints make use of this new interface. By default, they all return true from the isAutoStartup method. Also, 
 * the phase values are such that any EventDrivenConsumer is started (subscribed to its input channel) before any PollingConsumer is activated. 
 * 
 * That’s important because PollingConsumers often produce reply messages that may flow downstream to EventDrivenConsumers.
 * 
 * 
 * 
 * All channels in Spring Integration implement the following MessageChannel interface, which defines standard methods for sending messages. 
 * Note that it provides no methods for receiving messages:
 * 
 *   package org.springframework.integration;
 *   public interface MessageChannel {
 *       boolean send(Message<?> message);
 *       boolean send(Message<?> message, long timeout);
 *   }
 * 
 * The reason no methods are provided for receiving messages is because Spring Integration differentiates clearly between two mechanisms through 
 * which messages are handed over to the next endpoint—polling and subscription—and provides two distinct types of channels accordingly.
 * 
 * Channels that implement the SubscribableChannel interface, shown below, take responsibility for notifying subscribers when a message is available:
 * 
 *   package org.springframework.integration.core;
 *   public interface SubscribableChannel extends MessageChannel {
 *      boolean subscribe(MessageHandler handler);
 *      boolean unsubscribe(MessageHandler handler);
 *   }
 * 
 * The alternative is the PollableChannel, whose definition follows. This type of channel requires the receiver or the framework acting on behalf 
 * of the receiver to periodically check whether messages are available on the channel. This approach has the advantage that the consumer can 
 * choose when to process messages. The approach can also have its downsides, requiring a trade-off between longer poll periods, which may 
 * introduce latency in receiving a message, and computation overhead from more frequent polls that find no messages:
 * 
 *   package org.springframework.integration.core;
 *   public interface PollableChannel extends MessageChannel {
 *      Message<?> receive();
 *      Message<?> receive(long timeout);
 *   }
 * 
 * From a logical point of view, the responsibility of connecting a consumer to a channel belongs to the framework, thus alleviating the 
 * complications of defining the appropriate consumer types. To put it plainly, your job is to configure the appropriate channel type, and the 
 * framework will select the appropriate consumer type (polling or event-driven).
 * 
 * In Spring Integration, the default channels are SubscribableChannels, and the message transmission is synchronous.
 * 
 * The effect is simple: one thread is responsible for invoking the all services sequentially.
 * 
 * Analyzing your actions in terms of what you need to do now and what you can afford to do later is a good way of deciding what service calls 
 * you should block on. 
 * 
 * Billing the credit card and updating the seat availability are clearly things you need to do now so you can respond with confidence that the 
 * booking has been made. Sending the confirmation email isn’t time critical, and you don’t want to refuse bookings simply because the mail server 
 * is down. Therefore, introducing a queue between the mainline business logic execution and the confirmation email service will allow you to do 
 * just that: charge the card, update availability, and send the email confirmation when you can.
 * 
 * We’ve looked at scenarios where a number of services are invoked in sequence with the output of one service becoming the input of the next 
 * service in the sequence. This works well when the result of a service invocation needs to be consumed only once, but it’s common that more 
 * than one consumer may be interested in receiving certain messages.
 * 
 * To allow delivery of the same message to more than one consumer, you introduce a publish-subscribe channel after the availability check. The 
 * publish-subscribe channel provides one-to-many semantics rather than the one-to-one semantics provided by most channel implementations. 
 * One-to-many semantics are particularly useful when you want the flexibility to add additional consumers to the configuration; if the name of 
 * the publish-subscribe channel is known, that’s all that’s required for the configuration of additional consumers with no changes to the core 
 * application configuration.
 * 
 * The publish-subscribe channel doesn’t support queueing, but it does support asynchronous operation if you provide a task executor that 
 * delivers messages to each of the subscribers in separate threads. 
 * 
 * But this approach may still block the main thread sending the message on the channel where the task executor is configured to use the caller 
 * thread or block the caller thread when the underlying thread pool is exhausted.
 * 
 * To ensure that a backlog in sending email confirmations doesn’t block either the sender thread or the entire thread pool for the task executor, 
 * you can connect the new publish-subscribe channel to the existing email confirmation queue by means of a bridge. The bridge is an enterprise 
 * integration pattern that supports the connection of two channels, which allows the publish-subscribe channel to deliver to the queue and then 
 * have the thread return immediately.
 * 
 * Now it’s possible to connect one producer with multiple consumers by means of a publish-subscribe channel. Let’s get to the last challenge 
 * and emerge victoriously with our dramatically improved application: what if “first come, first served” isn’t always right?
 * 
 * Channel collaborators
 * 
 *   - MessageDispatcher 
 *      which controls how messages sent to a channel are passed to any registered handlers
 * 
 *   - ChannelInterceptor
 *      which allows interception at key points, like the channel send and receive operations.
 * 
 * MessageDispatcher
 * 
 * In most of the examples given so far, a channel has a single message handler connected to it. Though having a single component that takes 
 * care of processing the messages that arrive on a channel is a pretty common occurrence, multiple handlers processing messages arriving on 
 * the same channel is also a typical configuration.
 * Depending on how messages are distributed to the different message handlers, we can have either a competing consumers scenario or a 
 * broadcasting scenario.
 * 
 * In the broadcasting scenario, multiple (or all) handlers will receive the message.
 * 
 * In the competing consumers scenario, from the multiple handlers that are connected to a given channel, only one will receive and handle the 
 * message. 
 * 
 * Having multiple receivers that are capable of handling the message, even if only one of them will actually be selected to do the processing, 
 * is useful for load balancing or failover.
 * 
 * The strategy for determining how the channel implementation dispatches the message to the handlers is defined by the following 
 * MessageDispatcher interface:
 * 
 *   package org.springframework.integration.dispatcher;
 *   public interface MessageDispatcher {
 *      boolean addHandler(MessageHandler handler);
 *      boolean removeHandler(MessageHandler handler);
 *      boolean dispatch(Message<?> message);
 *   }
 * 
 * Spring Integration provides two dispatcher implementations out of the box: 
 *       - UnicastingDispatcher, which, as the name suggests, delivers the message to at most one MessageHandler, and 
 *       - BroadcastingDispatcher, which delivers to zero or more.
 * 
 * The UnicastingDispatcher provides an additional strategy interface, LoadBalancingStrategy, which determines which single MessageHandler 
 * of potentially many should receive any given message. The only provided implementation of this is the RoundRobinLoadBalancingStrategy, 
 * which works through the list of handlers, passing one message to each in turn.
 * 
 *   package org.springframework.integration.dispatcher;
 *   public interface LoadBalancingStrategy {
 * 
 *      public Iterator<MessageHandler>  getHandlerIterator(Message<?> message,
 *                                                          List<MessageHandler> handlers);
 *   }
 * 
 * ChannelInterceptor
 * 
 * Another important requirement for an integration system is that it can be notified as messages are traveling through the system. This 
 * functionality can be used for several purposes, ranging from monitoring of messages as they pass through the system to vetoing send and 
 * receive operations for security reasons. 
 * 
 * For supporting this, Spring Integration provides a special type of component called a channel interceptor.
 * 
 * The channel implementations provided by Spring Integration all allow the registration of one or more ChannelInterceptor instances.
 * 
 * Channel interceptors can be registered for individual channels or globally.
 * 
 * The ChannelInterceptor interface allows implementing classes to hook into the sending and receiving of messages by the channel: 
 * 
 *   package org.springframework.integration.channel;
 *   public interface ChannelInterceptor {
 *      Message<?> preSend(Message<?> message,MessageChannel channel);
 *      void postSend(Message<?> message, MessageChannel channel,boolean sent);
 *      boolean preReceive(MessageChannel channel);
 *      Message<?> postReceive(Message<?> message,MessageChannel channel);
 *   }
 * 
 * Just by looking at the names of the methods, it’s easy to get an idea when these methods are invoked, but there’s more to this component 
 * than simple notification. Let’s review them one by one:
 * 
 *   - preSend
 *     is invoked before a message is sent and returns the message that will be sent to the channel when the method returns. If the 
 *     method returns null, nothing is sent. This allows the implementation to control what gets sent to the channel, effectively 
 *     filtering the messages.
 * 
 *   - postSend
 *     is invoked after an attempt to send the message has been made. It indicates whether the attempt was successful through the 
 *     boolean flag it passes as an argument. This allows the implementation to monitor the message flow and learn which messages 
 *     are sent and which ones fail.
 * 
 *   - preReceive
 *     applies only if the channel is pollable. It’s invoked when a component calls receive() on the channel, but before a Message 
 *     is actually read from that channel. It allows implementers to decide whether the channel can return a message to the caller.
 * 
 *   - postReceive
 *     like preReceive, applies only to pollable channels. It’s invokedn after a message is read from a channel but before it’s 
 *     returned to the component that called receive(). If it returns null, then no message is received. This allows the implementer 
 *     to control what, if anything, is actually received by the poller.
 * 
 * Two different types of interception: monitoring and filtering messages before they are sent on a channel.
 * 
 * For the monitoring scenario, Spring Integration provides WireTap, an implementation of the more general Wire Tap enterprise integration 
 * pattern. As you saw earlier, it’s easy to audit messages that arrive on a channel using a custom interceptor, but WireTap enables you 
 * to do this in an even less invasive fashion by defining an interceptor that sends copies of messages on a distinct channel. This means 
 * that monitoring is completely separated from the actual business flow, from a logical standpoint, but also that it can take place 
 * asynchronously. 
 * 
 * The filtering scenario is based on the idea that only certain types of messages can be sent to a given channel. For this purpose, Spring 
 * Integration provides a MessageSelectingInterceptor that uses a MessageSelector to decide whether a message is allowed to be sent on a 
 * channel.
 * 
 * The MessageSelector is used in several other places in the framework, and its role is to encapsulate the decision whether a message is 
 * acceptable, taking into consideration a given set of criteria, as shown here:
 * 
 *   public interface MessageSelector {
 *      boolean accept(Message<?> message);
 *   }
 * 
 * One of the implementations of a MessageSelector provided by the framework is the PayloadTypeSelector, which accepts only the payload types 
 * it’s configured with, so combining it with a MessageSelectingInterceptor allows you to implement another enterprise integration pattern, 
 * the Datatype Channel, which allows only certain types of messages to be sent on a given channel.
 * 
 * 
 */
public class Application {
    

}
