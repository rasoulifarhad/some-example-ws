package com.example.democloudstreamwebfux.cloudstream;

import java.util.function.Function;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import reactor.core.publisher.Flux;

@Configuration
public class ProcessorConfig {
    

	public interface FluxFunction extends Function<Flux<StringPayload>,Flux<StringPayload>> {}

	@Bean
    public  Function<Flux<StringPayload>,Flux<StringPayload>> toUppercase() {
		return flux -> flux.map(value ->  StringPayload.builder().value( value.getValue().toUpperCase()).build() ) ;
	}

	// @Bean
	// public IntegrationFlow uppercaseFluxFlow() {
	// 	return IntegrationFlows
	// 							.from(FluxFunction.class , gateway -> gateway.beanName("uppercaseFluxFunction") )
	// 							.<String,String>transform(String::toUpperCase)
	// 							.logAndReply(LoggingHandler.Level.WARN)
	// 							;
	// }

	// public interface MessageFunction extends Function<Message<String>,Message<String>> {}
	// @Bean
	// public IntegrationFlow uppercaseFlow() {
	// 	return IntegrationFlows
	// 							.from(MessageFunction.class , gateway -> gateway.beanName("uppercaseFunction") )
	// 							.<String,String>transform(String::toUpperCase)
	// 							.logAndReply(LoggingHandler.Level.WARN)
	// 							;
	// }

	// @Bean
	// public IntegrationFlow uppercaseFlow1() {
	// 	return IntegrationFlows
	// 							.from(Function.class , gateway -> gateway.beanName("uppercaseFunction") )
	// 							.<String,String>transform(String::toUpperCase)
	// 							.logAndReply(LoggingHandler.Level.WARN)
	// 							;
	// }



	// @Bean
	// public ServerRSocketConnector serverRSocketConnector() {
	// 	ServerRSocketConnector serverRSocketConnector = new ServerRSocketConnector("localhost", 0);
	// 	serverRSocketConnector.setRSocketStrategies(rsocketStrategies());
	// 	serverRSocketConnector.setMetadataMimeType(new MimeType("message", "x.rsocket.routing.v0"));
	// 	serverRSocketConnector.setServerConfigurer((server) -> server.payloadDecoder(PayloadDecoder.ZERO_COPY));
	// 	serverRSocketConnector.setClientRSocketKeyStrategy((headers, data) -> ""
	// 									+ headers.get(DestinationPatternsMessageCondition.LOOKUP_DESTINATION_HEADER));
	// 	return serverRSocketConnector;
	// }
	
	// @Bean
	// public RSocketStrategies rsocketStrategies() {
	// 	return RSocketStrategies.builder()
	// 		.decoder(StringDecoder.textPlainOnly())
	// 		.encoder(CharSequenceEncoder.allMimeTypes())
	// 		.dataBufferFactory(new DefaultDataBufferFactory(true))
	// 		.build();
	// }

	// @Bean
	// public ClientRSocketConnector clientRSocketConnector() {
	// 	ClientRSocketConnector clientRSocketConnector =
	// 			new ClientRSocketConnector("localhost", serverRSocketConnector().getBoundPort().block());
	// 	clientRSocketConnector.setRSocketStrategies(rsocketStrategies());
	// 	clientRSocketConnector.setSetupRoute("clientConnect/{user}");
	// 	clientRSocketConnector.setSetupRouteVariables("myUser");
	// 	return clientRSocketConnector;
	// }
	
	// @Bean
	// public IntegrationFlow rsocketuppercaseFlow(ClientRSocketConnector clientRSocketConnector) {

	// 	return 
	// 			IntegrationFlows.from(
	// 								FluxFunction.class,
	// 								gateway -> gateway.beanName("uppercase") )
	// 							.handle(
	// 								RSockets.outboundGateway("/upercase")
	// 										.interactionModel(RSocketInteractionModel.requestResponse)
	// 										.expectedResponseType(String.class)
	// 										.clientRSocketConnector(clientRSocketConnector)
											
	// 							)		
	// 							.get()
	// 							;	

	// }

	// @Bean
	// public IntegrationFlow errorRecovererFlow() {
	// 	return IntegrationFlows.from(Function.class, (gateway) -> gateway.beanName("errorRecovererFunction"))
	// 			.handle((GenericHandler<?>) (p, h) -> {
	// 				throw new RuntimeException("intentional");
	// 			// }, e -> e.advice(retryAdvice()))
	// 		}, e -> e.advice())
	// 			.get();
	// }
    
    // That errorRecovererFlow can be used as follows:
    // 
	// @Autowired
	// @Qualifier("errorRecovererFunction")
	// private Function<String, String> errorRecovererFlowGateway;


}
