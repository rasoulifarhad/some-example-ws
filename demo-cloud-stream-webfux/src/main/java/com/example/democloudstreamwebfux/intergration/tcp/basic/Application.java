package com.example.democloudstreamwebfux.intergration.tcp.basic;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.dsl.Transformers;
import org.springframework.integration.http.config.EnableIntegrationGraphController;
import org.springframework.integration.ip.dsl.Tcp;
import org.springframework.integration.ip.tcp.serializer.TcpCodecs;

import com.example.democloudstreamwebfux.intergration.ChannelConfiguration;
import com.example.democloudstreamwebfux.intergration.ChannelConfiguration.TestStringChannels;

import lombok.extern.slf4j.Slf4j;

/**
 * Introduction
 * 
 * Two flavors each of UDP inbound and outbound channel adapters are provided:
 * 
 *   - UnicastSendingMessageHandler sends a datagram packet to a single destination.
 *   - UnicastReceivingChannelAdapter receives incoming datagram packets.
 *   - MulticastSendingMessageHandler sends (broadcasts) datagram packets to a multicast address.
 *   - MulticastReceivingChannelAdapter receives incoming datagram packets by joining to a multicast address.
 * 
 * TCP inbound and outbound channel adapters are provided:
 * 
 *   - TcpSendingMessageHandler sends messages over TCP.
 *   - TcpReceivingChannelAdapter receives messages over TCP.
 * 
 * An inbound TCP gateway is provided. It allows for simple request-response processing. While the gateway can support any number of connections, 
 * each connection can only be processed serially. The thread that reads from the socket waits for, and sends, the response before reading again. 
 * If the connection factory is configured for single use connections, the connection is closed after the socket times out.
 * 
 * An outbound TCP gateway is provided. It allows for simple request-response processing. If the associated connection factory is configured for 
 * single-use connections, a new connection is immediately created for each new request. Otherwise, if the connection is in use, the calling 
 * thread blocks on the connection until either a response is received or a timeout or I/O error occurs.
 * 
 * The TCP and UDP inbound channel adapters and the TCP inbound gateway support the error-channel attribute. This provides the same basic 
 * functionality as described in Enter the GatewayProxyFactoryBean.
 * 
 * TCP Connection Factories
 * 
 * For TCP, the configuration of the underlying connection is provided by using a connection factory. Two types of connection factory are 
 * provided: a client connection factory and a server connection factory. Client connection factories establish outgoing connections. Server 
 * connection factories listen for incoming connections.
 * 
 * An outbound channel adapter uses a client connection factory, but you can also provide a reference to a client connection factory to an 
 * inbound channel adapter. That adapter receives any incoming messages that are received on connections created by the outbound adapter.
 * 
 * An inbound channel adapter or gateway uses a server connection factory. (In fact, the connection factory cannot function without one). You 
 * can also provide a reference to a server connection factory to an outbound adapter. You can then use that adapter to send replies to 
 * incoming messages on the same connection.
 * 
 * Reply messages are routed to the connection only if the reply contains the ip_connectionId header that was inserted into the original 
 * message by the connection factory. 
 * 
 * This is the extent of message correlation performed when sharing connection factories between inbound and outbound adapters. Such sharing 
 * allows for asynchronous two-way communication over TCP. By default, only payload information is transferred using TCP. Therefore, any 
 * message correlation must be performed by downstream components such as aggregators or other endpoints. Support for transferring selected 
 * headers was introduced in version 3.0. For more information, see TCP Message Correlation. 
 * 
 * You may give a reference to a connection factory to a maximum of one adapter of each type.
 * 
 * Spring Integration provides connection factories that use java.net.Socket and java.nio.channel.SocketChannel
 * 
 * Starting with Spring Integration version 4.2, if the server is configured to listen on a random port (by setting the port to 0), you can 
 * get the actual port chosen by the OS by using getPort(). Also, getServerSocketAddress() lets you get the complete SocketAddress. See the 
 * Javadoc for the TcpServerConnectionFactory interface for more information. 
 * 
 * Starting with version 5.2, the client connection factories support the property connectTimeout, specified in seconds, which defaults to 60.
 * 
 *   <int-ip:tcp-connection-factory id="server"
 *       type="server"
 *       port="1234"/>
 *   
 *   <int-ip:tcp-connection-factory id="server"
 *       type="server"
 *       port="1234"
 *       using-nio="true"/>
 * 
 *   <int-ip:tcp-connection-factory id="client"
 *       type="client"
 *       host="localhost"
 *       port="1234"
 *       single-use="true"
 *       so-timeout="10000"/>
 * 
 *   <int-ip:tcp-connection-factory id="client"
 *       type="client"
 *       host="localhost"
 *       port="1234"
 *       single-use="true"
 *       so-timeout="10000"
 *       using-nio=true/>
 * 
 * Message Demarcation (Serializers and Deserializers)
 * 
 * TCP is a streaming protocol. This means that some structure has to be provided to data transported over TCP so that the receiver can demarcate 
 * the data into discrete messages. Connection factories are configured to use serializers and deserializers to convert between the message 
 * payload and the bits that are sent over TCP. This is accomplished by providing a deserializer and a serializer for inbound and outbound 
 * messages, respectively. Spring Integration provides a number of standard serializers and deserializers.
 * 
 *   - ByteArrayCrlfSerializer* converts a byte array to a stream of bytes followed by carriage return and linefeed characters (\r\n). This is 
 *     the default serializer (and deserializer) and can be used (for example) with telnet as a client.
 * 
 *   - The ByteArraySingleTerminatorSerializer* converts a byte array to a stream of bytes followed by a single termination character (the 
 *     default is 0x00).
 * 
 *   - The ByteArrayLfSerializer* converts a byte array to a stream of bytes followed by a single linefeed character (0x0a)
 * 
 *   - The ByteArrayStxEtxSerializer* converts a byte array to a stream of bytes preceded by an STX (0x02) and followed by an ETX (0x03).
 * 
 *   - The ByteArrayLengthHeaderSerializer converts a byte array to a stream of bytes preceded by a binary length in network byte order (big endian). 
 *     This an efficient deserializer because it does not have to parse every byte to look for a termination character sequence. It can also be 
 *     used for payloads that contain binary data.
 * 
 *     The preceding serializers support only text in the payload. The default size of the length header is four bytes (an Integer), allowing for 
 *     messages up to (2^31 - 1) bytes. However, the length header can be a single byte (unsigned) for messages up to 255 bytes, or an unsigned 
 *     short (2 bytes) for messages up to (2^16 - 1) bytes. If you need any other format for the header, you can subclass 
 *     ByteArrayLengthHeaderSerializer and provide implementations for the readHeader and writeHeader methods. The absolute maximum data size 
 *     is (2^31 - 1) bytes. Starting with version 5.2, the header value can include the length of the header in addition to the payload. Set the 
 *     inclusive property to enable that mechanism (it must be set to the same for producers and consumers).
 *  
 *   - The ByteArrayRawSerializer*, converts a byte array to a stream of bytes and adds no additional message demarcation data. With this serializer 
 *     (and deserializer), the end of a message is indicated by the client closing the socket in an orderly fashion. When using this serializer, message 
 *     reception hangs until the client closes the socket or a timeout occurs. A timeout does not result in a message. When this serializer is being 
 *     used and the client is a Spring Integration application, the client must use a connection factory that is configured with single-use="true". 
 *     Doing so causes the adapter to close the socket after sending the message. The serializer does not, by itself, close the connection. You should 
 *     use this serializer only with the connection factories used by channel adapters (not gateways), and the connection factories should be used by 
 *     either an inbound or outbound adapter but not both. See also ByteArrayElasticRawDeserializer, later in this section. However, since version 5.2, 
 *     the outbound gateway has a new property closeStreamAfterSend; this allows the use of raw serializers/deserializers because the EOF is signaled 
 *     to the server, while leaving the connection open to receive the reply.
 * 
 * Each of these is a subclass of AbstractByteArraySerializer, which implements both org.springframework.core.serializer.Serializer and 
 * org.springframework.core.serializer.Deserializer. For backwards compatibility, connections that use any subclass of AbstractByteArraySerializer 
 * for serialization also accept a String that is first converted to a byte array. Each of these serializers and deserializers converts an input 
 * stream that contains the corresponding format to a byte array payload.
 * 
 * To avoid memory exhaustion due to a badly behaved client (one that does not adhere to the protocol of the configured serializer), these serializers impose a maximum message size. If an incoming message exceeds this size, an exception is thrown. The default maximum message size is 2048 bytes. You can increase it by setting the maxMessageSize property. If you use the default serializer or deserializer and wish to increase the maximum message size, you must declare the maximum message size as an explicit bean with the maxMessageSize property set and configure the connection factory to use that bean.
 * 
 *   - The MapJsonSerializer uses a Jackson ObjectMapper to convert between a Map and JSON. You can use this serializer in conjunction with a MessageConvertingTcpMessageMapper and a MapMessageConverter to transfer selected headers and the payload in JSON.
 * 
 * The Jackson ObjectMapper cannot demarcate messages in the stream. Therefore, the MapJsonSerializer needs to delegate to another serializer or deserializer to handle message demarcation. By default, a ByteArrayLfSerializer is used, resulting in messages with a format of <json><LF> on the wire, but you can configure it to use others instead. (The next example shows how to do so.) 
 * 
 *   - the final standard serializer is org.springframework.core.serializer.DefaultSerializer, which you can use to convert serializable objects with Java serialization. org.springframework.core.serializer.DefaultDeserializer is provided for inbound deserialization of streams that contain serializable objects.
 * 
 * If you do not wish to use the default serializer and deserializer (ByteArrayCrLfSerializer), you must set the serializer and deserializer attributes on the connection factory. The following example shows how to do so:
 * 
 *   <bean id="javaSerializer"
 *         class="org.springframework.core.serializer.DefaultSerializer" />
 *   <bean id="javaDeserializer"
 *         class="org.springframework.core.serializer.DefaultDeserializer" />
 *   
 *   <int-ip:tcp-connection-factory id="server"
 *       type="server"
 *       port="1234"
 *       deserializer="javaDeserializer"
 *       serializer="javaSerializer"/>
 * 
 * Custom Serializers and Deserializers
 * 
 * If your data is not in a format supported by one of the standard deserializers, you can implement your own; you can also implement a custom serializer.
 * 
 * To implement a custom serializer and deserializer pair, implement the org.springframework.core.serializer.Deserializer and org.springframework.core.serializer.Serializer interfaces.
 * 
 * When the deserializer detects a closed input stream between messages, it must throw a SoftEndOfStreamException; this is a signal to the framework to indicate that the close was "normal". If the stream is closed while decoding a message, some other exception should be thrown instead.
 * 
 * Starting with version 5.2, SoftEndOfStreamException is now a RuntimeException instead of extending IOException.
 * 
 * TCP Caching Client Connection Factory
 * 
 * As noted earlier, TCP sockets can be 'single-use' (one request or response) or shared. Shared sockets do not perform well with outbound gateways in high-volume environments, because the socket can only process one request or response at a time.
 * 
 * To improve performance, you can use collaborating channel adapters instead of gateways, but that requires application-level message correlation. See TCP Message Correlation for more information.
 * 
 * Spring Integration 2.2 introduced a caching client connection factory, which uses a pool of shared sockets, letting a gateway process multiple concurrent requests with a pool of shared connections.
 * 
 * TCP Failover Client Connection Factory
 * 
 * You can configure a TCP connection factory that supports failover to one or more other servers. When sending a message, the factory iterates over all its configured factories until either the message can be sent or no connection can be found. Initially, the first factory in the configured list is used. If a connection subsequently fails, the next factory becomes the current factory. The following example shows how to configure a failover client connection factory:
 * 
 *   <bean id="failCF" class="o.s.i.ip.tcp.connection.FailoverClientConnectionFactory">
 *       <constructor-arg>
 *           <list>
 *               <ref bean="clientFactory1"/>
 *               <ref bean="clientFactory2"/>
 *           </list>
 *       </constructor-arg>
 *   </bean>
 * 
 *   
 * TCP Thread Affinity Connection Factory
 * 
 * Spring Integration version 5.0 introduced this connection factory. It binds a connection to the calling thread, and the same connection is reused each time that thread sends a message. This continues until the connection is closed (by the server or the network) or until the thread calls the releaseConnection() method. The connections themselves are provided by another client factory implementation, which must be configured to provide non-shared (single-use) connections so that each thread gets a connection.
 * 
 * The following example shows how to configure a TCP thread affinity connection factory:
 *
 *    @Bean
 *    public TcpNetClientConnectionFactory cf() {
 *        TcpNetClientConnectionFactory cf = new TcpNetClientConnectionFactory("localhost",
 *                Integer.parseInt(System.getProperty(PORT)));
 *        cf.setSingleUse(true);
 *        return cf;
 *    }
 *    
 *    @Bean
 *    public ThreadAffinityClientConnectionFactory tacf() {
 *        return new ThreadAffinityClientConnectionFactory(cf());
 *    }
 *    
 *    @Bean
 *    @ServiceActivator(inputChannel = "out")
 *    public TcpOutboundGateway outGate() {
 *        TcpOutboundGateway outGate = new TcpOutboundGateway();
 *        outGate.setConnectionFactory(tacf());
 *        outGate.setReplyChannelName("toString");
 *        return outGate;
 *    }
 *    
 * Testing Connections
 * 
 * In some scenarios, it can be useful to send some kind of health-check request when a connection is first opened. One such scenario might be when using a TCP Failover Client Connection Factory so that we can fail over if the selected server allowed a connection to be opened but reports that it is not healthy.
 * 
 * In order to support this feature, add a connectionTest to the client connection factory.
 * 
 *   /**
 *    * Set a {@link Predicate} that will be invoked to test a new connection; return true
 *    * to accept the connection, false the reject.
 *    * @param connectionTest the predicate.
 *    * @since 5.3
 *    *
 *   public void setConnectionTest(@Nullable Predicate<TcpConnectionSupport> connectionTest) {
 *       this.connectionTest = connectionTest;
 *   }
 *   
 * To test the connection, attach a temporary listener to the connection within the test. If the test fails, the connection is closed and an exception thrown. When used with the TCP Failover Client Connection Factory this triggers trying the next server.
 * In the following example, the server is considered healthy if the server replies PONG when we send PING.
 * 
 *   Message<String> ping = new GenericMessage<>("PING");
 *   byte[] pong = "PONG".getBytes();
 *   clientFactory.setConnectionTest(conn -> {
 *       CountDownLatch latch = new CountDownLatch(1);
 *       AtomicBoolean result = new AtomicBoolean();
 *       conn.registerTestListener(msg -> {
 *           if (Arrays.equals(pong, (byte[]) msg.getPayload())) {
 *               result.set(true);
 *           }
 *           latch.countDown();
 *           return false;
 *       });
 *       conn.send(ping);
 *       try {
 *           latch.await(10, TimeUnit.SECONDS);
 *       }
 *       catch (InterruptedException e) {
 *           Thread.currentThread().interrupt();
 *       }
 *       return result.get();
 *   });
 *   
 * TCP Connection Interceptors
 * 
 * You can configure connection factories with a reference to a TcpConnectionInterceptorFactoryChain. You can use interceptors to add behavior to connections, such as negotiation, security, and other options. No interceptors are currently provided by the framework, but see InterceptedSharedConnectionTests in the source repository for an example.
 * 
 * All TcpConnection methods are intercepted. Interceptor instances are created for each connection by an interceptor factory. If an interceptor is stateful, the factory should create a new instance for each connection. If there is no state, the same interceptor can wrap each connection. Interceptor factories are added to the configuration of an interceptor factory chain, which you can provide to a connection factory by setting the interceptor-factory attribute. Interceptors must extend TcpConnectionInterceptorSupport. Factories must implement the TcpConnectionInterceptorFactory interface. TcpConnectionInterceptorSupport has passthrough methods. By extending this class, you only need to implement those methods you wish to intercept.
 * 
 * TCP Connection Events
 * 
 * Beginning with version 3.0, changes to TcpConnection instances are reported by TcpConnectionEvent instances. TcpConnectionEvent is a subclass of ApplicationEvent and can thus be received by any ApplicationListener defined in the ApplicationContext — for example an event inbound channel adapter.
 * 
 * TcpConnectionEvents have the following properties:
 * 
 *   - connectionId: The connection identifier, which you can use in a message header to send data to the connection.
 * 
 *   - connectionFactoryName: The bean name of the connection factory to which the connection belongs.
 * 
 *   - throwable: The Throwable (for TcpConnectionExceptionEvent events only).
 * 
 *   - source: The TcpConnection. You can use this, for example, to determine the remote IP Address with getHostAddress() (cast required).
 * 
 * in addition, since version 4.0, the standard deserializers discussed in TCP Connection Factories now emit TcpDeserializationExceptionEvent instances when they encounter problems while decoding the data stream. These events contain the exception, the buffer that was in the process of being built, and an offset into the buffer (if available) at the point where the exception occurred. Applications can use a normal ApplicationListener or an ApplicationEventListeningMessageProducer (see Receiving Spring Application Events) to capture these events, allowing analysis of the problem
 * 
 * Starting with versions 4.0.7 and 4.1.3, TcpConnectionServerExceptionEvent instances are published whenever an unexpected exception occurs on a server socket (such as a BindException when the server socket is in use). These events have a reference to the connection factory and the cause.
 * 
 * Starting with version 4.2, TcpConnectionFailedCorrelationEvent instances are published whenever an endpoint (inbound gateway or collaborating outbound channel adapter) receives a message that cannot be routed to a connection because the ip_connectionId header is invalid. Outbound gateways also publish this event when a late reply is received (the sender thread has timed out). The event contains the connection ID as well as an exception in the cause property, which contains the failed message.
 * 
 * Starting with version 4.3, a TcpConnectionServerListeningEvent is emitted when a server connection factory is started. This is useful when the factory is configured to listen on port 0, meaning that the operating system chooses the port. It can also be used instead of polling isListening(), if you need to wait before starting some other process that connects to the socket.
 * 
 *  To avoid delaying the listening thread from accepting connections, the event is published on a separate thread. 
 * 
 * Starting with version 4.3.2, a TcpConnectionFailedEvent is emitted whenever a client connection cannot be created. The source of the event is the connection factory, which you can use to determine the host and port to which the connection could not be established.
 * 
 * TCP Adapters
 * 
 * TCP inbound and outbound channel adapters that use connection factories mentioned earlier are provided. These adapters have two relevant attributes: connection-factory and channel. The connection-factory attribute indicates which connection factory is to be used to manage connections for the adapter. The channel attribute specifies the channel on which messages arrive at an outbound adapter and on which messages are placed by an inbound adapter. While both inbound and outbound adapters can share a connection factory, server connection factories are always “owned” by an inbound adapter. Client connection factories are always “owned” by an outbound adapter. Only one adapter of each type may get a reference to a connection factory. 
 * 
 * Normally, inbound adapters use a type="server" connection factory, which listens for incoming connection requests. In some cases, you may want to establish the connection in reverse, such that the inbound adapter connects to an external server and then waits for inbound messages on that connection.
 * 
 * this topology is supported by setting client-mode="true" on the inbound adapter. In this case, the connection factory must be of type client and must have single-use set to false.
 * 
 * Two additional attributes support this mechanism. The retry-interval specifies (in milliseconds) how often the framework attempts to reconnect after a connection failure. The scheduler supplies a TaskScheduler to schedule the connection attempts and to test that the connection is still active.
 * 
 * if you don’t provide a scheduler, the framework’s default taskScheduler bean is used.
 * 
 * For an outbound adapter, the connection is normally established when the first message is sent. The client-mode="true" on an outbound adapter causes the connection to be established when the adapter is started. By default, adapters are automatically started. Again, the connection factory must be of type client and have single-use="false". A retry-interval and scheduler are also supported. If a connection fails, it is re-established either by the scheduler or when the next message is sent.
 * 
 * For both inbound and outbound, if the adapter is started, you can force the adapter to establish a connection by sending a <control-bus /> command: @adapter_id.retryConnection(). Then you can examine the current state with @adapter_id.isClientModeConnected().
 * 
 * TCP Gateways
 * 
 * The inbound TCP gateway TcpInboundGateway and outbound TCP gateway TcpOutboundGateway use a server and client connection factory, respectively. Each connection can process a single request or response at a time.
 * 
 * The inbound gateway, after constructing a message with the incoming payload and sending it to the requestChannel, waits for a response and sends the payload from the response message by writing it to the connection.
 * 
 * For the inbound gateway, you must retain or populate, the ip_connectionId header, because it is used to correlate the message to a connection. Messages that originate at the gateway automatically have the header set. If the reply is constructed as a new message, you need to set the header. The header value can be captured from the incoming message
 * 
 * As with inbound adapters, inbound gateways normally use a type="server" connection factory, which listens for incoming connection requests. In some cases, you may want to establish the connection in reverse, such that the inbound gateway connects to an external server and then waits for and replies to inbound messages on that connection.
 * 
 * This topology is supported by using client-mode="true" on the inbound gateway. In this case, the connection factory must be of type client and must have single-use set to false.
 * 
 * Two additional attributes support this mechanism. The retry-interval specifies (in milliseconds) how often the framework tries to reconnect after a connection failure. The scheduler supplies a TaskScheduler to schedule the connection attempts and to test that the connection is still active.
 * 
 * If the gateway is started, you may force the gateway to establish a connection by sending a <control-bus/> command: @adapter_id.retryConnection() and examine the current state with @adapter_id.isClientModeConnected().
 * 
 * The outbound gateway, after sending a message over the connection, waits for a response, constructs a response message, and puts it on the reply channel. Communications over the connections are single-threaded. Only one message can be handled at a time. If another thread attempts to send a message before the current response has been received, it blocks until any previous requests are complete (or time out). If, however, the client connection factory is configured for single-use connections, each new request gets its own connection and is processed immediately.
 * 
 * If a connection factory configured with the default serializer or deserializer is used, messages is \r\n delimited data and the gateway can be used by a simple client such as telnet.
 * 
 * The client-mode is not currently available with the outbound gateway.
 * 
 * Starting with version 5.2, the outbound gateway can be configured with the property closeStreamAfterSend. If the connection factory is configured for single-use (a new connection for each request/reply) the gateway will close the output stream; this signals EOF to the server. This is useful if the server uses the EOF to determine the end of message, rather than some delimiter in the stream, but leaves the connection open in order to receive the reply.
 * 
 * Normally, the calling thread will block in the gateway, waiting for the reply (or a timeout). Starting with version 5.3, you can set the async property on the gateway and the sending thread is released to do other work. The reply (or error) will be sent on the receiving thread. This only applies when using the TcpNetClientConnectionFactory, it is ignored when using NIO because there is a race condition whereby a socket error that occurs after the reply is received can be passed to the gateway before the reply.
 * 
 * When using a shared connection (singleUse=false), a new request, while another is in process, will be blocked until the current reply is received. Consider using the CachingClientConnectionFactory if you wish to support concurrent requests on a pool of long-lived connections. 
 * 
 * Starting with version 5.4, the inbound can be configured with an unsolicitedMessageChannel. Unsolicited inbound messages will be sent to this channel, as well as late replies (where the client timed out). To support this on the server side, you can now register multiple TcpSender s with the connection factory. Gateways and Channel Adapters automatically register themselves. When sending unsolicited messages from the server, you must add the appropriate IpHeaders.CONNECTION_ID to the messages sent.
 * 
 * TCP Message Correlation
 * 
 * One goal of the IP endpoints is to provide communication with systems other than Spring Integration applications. For this reason, only message payloads are sent and received by default. Since 3.0, you can transfer headers by using JSON, Java serialization, or custom serializers and deserializers. See Transferring Headers for more information. No message correlation is provided by the framework (except when using the gateways) or collaborating channel adapters on the server side. Later in this document, we discuss the various correlation techniques available to applications. In most cases, this requires specific application-level correlation of messages, even when message payloads contain some natural correlation data (such as an order number).
 * 
 * Gateways
 * 
 * Gateways automatically correlate messages. However, you should use an outbound gateway for relatively low-volume applications. When you configure the connection factory to use a single shared connection for all message pairs ('single-use="false"'), only one message can be processed at a time. A new message has to wait until the reply to the previous message has been received. When a connection factory is configured for each new message to use a new connection ('single-use="true"'), this restriction does not apply. While this setting can give higher throughput than a shared connection environment, it comes with the overhead of opening and closing a new connection for each message pair.
 * 
 * Therefore, for high-volume messages, consider using a collaborating pair of channel adapters. However, to do so, you need to provide collaboration logic.
 * 
 * Another solution, introduced in Spring Integration 2.2, is to use a CachingClientConnectionFactory, which allows the use of a pool of shared connections.
 * 
 * Collaborating Outbound and Inbound Channel Adapters
 * 
 * To achieve high-volume throughput (avoiding the pitfalls of using gateways, as mentioned earlier) you can configure a pair of collaborating outbound and inbound channel adapters. You can also use collaborating adapters (server-side or client-side) for totally asynchronous communication (rather than with request-reply semantics). On the server side, message correlation is automatically handled by the adapters, because the inbound adapter adds a header that allows the outbound adapter to determine which connection to use when sending the reply message
 * 
 * On the server side, you must populate the ip_connectionId header, because it is used to correlate the message to a connection. Messages that originate at the inbound adapter automatically have the header set. If you wish to construct other messages to send, you need to set the header. You can get the header value from an incoming message. 
 * 
 * On the client side, the application must provide its own correlation logic, if needed. You can do so in a number of ways.
 * 
 * If the message payload has some natural correlation data (such as a transaction ID or an order number) and you have no need to retain any information (such as a reply channel header) from the original outbound message, the correlation is simple and would be done at the application level in any case.
 * 
 * If the message payload has some natural correlation data (such as a transaction ID or an order number), but you need to retain some information (such as a reply channel header) from the original outbound message, you can retain a copy of the original outbound message (perhaps by using a publish-subscribe channel) and use an aggregator to recombine the necessary data.
 * 
 * For either of the previous two scenarios, if the payload has no natural correlation data, you can provide a transformer upstream of the outbound channel adapter to enhance the payload with such data. Such a transformer may transform the original payload to a new object that contains both the original payload and some subset of the message headers. Of course, live objects (such as reply channels) from the headers cannot be included in the transformed payload.
 * 
 * If you choose such a strategy, you need to ensure the connection factory has an appropriate serializer-deserializer pair to handle such a payload (such as DefaultSerializer and DefaultDeserializer, which use java serialization, or a custom serializer and deserializer). The ByteArray*Serializer options mentioned in TCP Connection Factories, including the default ByteArrayCrLfSerializer, do not support such payloads unless the transformed payload is a String or byte[].
 * 
 * Before the 2.2 release, when collaborating channel adapters used a client connection factory, the so-timeout attribute defaulted to the default reply timeout (10 seconds). This meant that, if no data were received by the inbound adapter for this period of time, the socket was closed.
 * This default behavior was not appropriate in a truly asynchronous environment, so it now defaults to an infinite timeout. You can reinstate the previous default behavior by setting the so-timeout attribute on the client connection factory to 10000 milliseconds.
 * 
 * Starting with version 5.4, multiple outbound channel adapters and one TcpInboundChannelAdapter can share the same connection factory. This allows an application to support both request/reply and arbitrary server → client messaging. See TCP Gateways for more information.
 * 
 * Transferring Headers
 * 
 * TCP is a streaming protocol. Serializers and Deserializers demarcate messages within the stream. Prior to 3.0, only message payloads (String or byte[]) could be transferred over TCP. Beginning with 3.0, you can transfer selected headers as well as the payload. However, “live” objects, such as the replyChannel header, cannot be serialized.
 * 
 * Sending header information over TCP requires some additional configuration.
 * 
 * The first step is to provide the ConnectionFactory with a MessageConvertingTcpMessageMapper that uses the mapper attribute. This mapper delegates to any MessageConverter implementation to convert the message to and from some object that can be serialized and deserialized by the configured serializer and deserializer.
 * 
 * Spring Integration provides a MapMessageConverter, which allows the specification of a list of headers that are added to a Map object, along with the payload. The generated Map has two entries: payload and headers. The headers entry is itself a Map and contains the selected headers.
 * 
 * The second step is to provide a serializer and a deserializer that can convert between a Map and some wire format. This can be a custom Serializer or Deserializer, which you typically need if the peer system is not a Spring Integration application.
 * 
 * Spring Integration provides a MapJsonSerializer to convert a Map to and from JSON. It uses a Spring Integration JsonObjectMapper. You can provide a custom JsonObjectMapper if needed. By default, the serializer inserts a linefeed (0x0a) character between objects. See the Javadoc for more information.
 * 
 * The JsonObjectMapper uses whichever version of Jackson is on the classpath. 
 * 
 * You can also use standard Java serialization of the Map, by using the DefaultSerializer and DefaultDeserializer.
 * 
 * 
 * 
 */
public class Application {
    
    @EnableAutoConfiguration
    @Configuration
    @IntegrationComponentScan
    @EnableIntegrationGraphController
    @Slf4j
    public static class IntegrationApplication {
        public static void main(String[] args) throws Exception {

            log.info("--");
            new SpringApplicationBuilder()
                      .sources(IntegrationApplication.class,
                      //,
                      IntegrationConfig.class
                                          )
                      .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/intergration/tcp/basic/")
                      .run("--management.endpoints.web.exposure.include=*","--spring.main.lazy-initialization=false" 
                                  ,"--logging.level.root=INFO" 
                                  ,"--logging.level.org.springframework.retry.support.RetryTemplate=DEBUG"
                                  ,"--spring.cloud.stream.defaultBinder=rabbit");
        }
    }

    @Configuration
    @Slf4j
    public static class IntegrationConfig {

        @Bean(name = "tcpIn.errorChannel")
        public DirectChannel tcpInErrorChannel() {
            return MessageChannels.direct().get();
        }

        @Bean(name = "tcpInbound")
        public DirectChannel tcpInbound() {

            return MessageChannels.direct().get();
        }

        @Bean
        public IntegrationFlow server() {

            return IntegrationFlows.from( Tcp.inboundAdapter(Tcp.netServer(1234)
                                                                .deserializer(TcpCodecs.lengthHeader1())
                                                                .backlog(30))
                                             .errorChannel("tcpIn.errorChannel")
                                             .id("tcpIn"))
                                   .transform(Transformers.objectToString())   
                                   .channel("tcpInbound")
                                //    .log()
                                   .get();
        }

        @Bean
        public IntegrationFlow client() {

            return flow -> flow.handle(Tcp.outboundAdapter(Tcp.nioClient("localhost", 1234)
                                                              .serializer(TcpCodecs.lengthHeader1())));
        }

        @Bean
        public MessagingTemplate messagingTemplate() {
            return new MessagingTemplate() ;
        }

        @Bean
        @Order(30)
        public ApplicationRunner runner(MessagingTemplate messagingTemplate,DirectChannel tcpInbound) {

            return args -> {

                tcpInbound.subscribe(message -> log.info("Received: {}",message));
                
                messagingTemplate.convertAndSend("client.input", "Hiiiiiiiiiii");

            };
        }
    }

    
}
