package com.example.democloudstreamwebfux.cloudFunctionStreamIntegration;

import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication()
// @PropertySource("classpath:com/example/democloudstreamwebfux/cloudFunctionStreamIntegration/application.properties")
@Slf4j
public class Application {


    public static void main(String[] args) throws Exception {

        new SpringApplicationBuilder()
                .sources(Application.class)
                .bannerMode(Banner.Mode.OFF)
                .properties("spring.config.name=cloudFunctionStreamIntegration")
                .properties("spring.config.location=optional:classpath:com/example/democloudstreamwebfux/cloudFunctionStreamIntegration/")
                // .properties("spring.main.lazy-initialization=false")
                // .properties("spring.cloud.function.definition=process;doubleIt|produceIt|logIt")
                // //.properties("spring.cloud.stream.function.definition=doubleIt|produceIt|logIt")
                // .properties("spring.cloud.stream.bindings.doubleIt|produceIt|logIt-in-0.destination=idestination")
                // .properties("spring.cloud.stream.bindings.doubleIt|produceIt|logIt-in-0.group=igroup")
                // .properties("spring.cloud.stream.rabbit.bindings.doubleIt|produceIt|logIt-in-0.consumer.durable-subscription=true")

                .web(WebApplicationType.REACTIVE)
                .run(args);


        // SpringApplication application = new SpringApplication(Application.class);
        // application.setBannerMode(Banner.Mode.OFF);
        // Map<String, Object> myAppProperties = new HashMap<String, Object>();
	// myAppProperties.put("spring.config.name", "cloudFunctionStreamIntegration");
	// myAppProperties.put("spring.config.location", "optional:classpath:com/example/democloudstreamwebfux/cloudFunctionStreamIntegration/");
	// // myAppProperties.put("spring.main.lazy-initialization", "false");
	// // myAppProperties.put("spring.cloud.function.definition", "process;doubleIt|produceIt|logIt");
	// // myAppProperties.put("spring.cloud.stream.bindings.doubleIt|produceIt|logIt-in-0.destination", "idestination");
	// // myAppProperties.put("spring.cloud.stream.bindings.doubleIt|produceIt|logIt-in-0.group", "igroup");
	// // myAppProperties.put("spring.cloud.stream.rabbit.bindings.doubleIt|produceIt|logIt-in-0.consumer.durable-subscription", "true");

        // application.setWebApplicationType(WebApplicationType.REACTIVE);
        // application.run(args);
         
    }

//     public static void main(String[] args) throws Exception {
//         ConfigurableApplicationContext applicationContext = new SpringApplicationBuilder()
//                 .properties("spring.config.name:cloudFunctionStreamIntegration",
//                         "spring.config.location:optional:classpath:com/example/democloudstreamwebfux/cloudFunctionStreamIntegration/")
//                 .sources(Application.class)        
//                 .build().run(
//                         "--spring.config.name=cloudFunctionStreamIntegration",
//                         "--spring.config.location=optional:classpath:com/example/democloudstreamwebfux/cloudFunctionStreamIntegration/",
//                         "--spring.main.lazy-initialization=false",
//                         "--spring.cloud.function.definition=process;doubleIt|produceIt|logIt",
//                         // "--spring.cloud.stream.function.definition=doubleIt|produceIt|logIt",
//                         "--spring.cloud.stream.bindings.doubleIt|produceIt|logIt-in-0.destination=idestination",
//                         "--spring.cloud.stream.bindings.doubleIt|produceIt|logIt-in-0.group=igroup",
//                         "--spring.cloud.stream.rabbit.bindings.doubleIt|produceIt|logIt-in-0.consumer.durable-subscription=true"

//                 );
 
//         // SpringApplication.run(
//         //                             Application.class, 
//                                        "--spring.config.name=cloudFunctionStreamIntegration",
//                                        "--spring.config.location=optional:classpath:com/example/democloudstreamwebfux/cloudFunctionStreamIntegration/",
//         //                             "--spring.main.lazy-initialization=false",
//         //                             "--spring.cloud.function.definition=process;doubleIt|produceIt|logIt",
//         //                             // "--spring.cloud.stream.function.definition=doubleIt|produceIt|logIt",
//         //                             "--spring.cloud.stream.bindings.doubleIt|produceIt|logIt-in-0.destination=idestination",
//         //                             "--spring.cloud.stream.bindings.doubleIt|produceIt|logIt-in-0.group=igroup",
//         //                             "--spring.cloud.stream.rabbit.bindings.doubleIt|produceIt|logIt-in-0.consumer.durable-subscription=true"
//         //                             );
//     }    
}

