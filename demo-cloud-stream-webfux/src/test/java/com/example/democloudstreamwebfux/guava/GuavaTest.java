package com.example.democloudstreamwebfux.guava;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.http.HttpHost;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.junit.jupiter.api.Test;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GuavaTest {
    
    private static final int REST_CLIENT_CACHE_SIZE = 100;
    
    private static final Cache<List, RestClient> REST_CLIENTS = CacheBuilder.newBuilder()
      .maximumSize(REST_CLIENT_CACHE_SIZE)
      .removalListener(new RemovalListener<List, RestClient>() {
        @Override public void onRemoval(RemovalNotification<List, RestClient> notice) {
          log.warn(
              "Will close an ES REST client to keep the number of open clients under {}. "
              + "Any schema objects that might still have been relying on this client are now "
              + "broken! Do not try to access more than {} distinct ES REST APIs through this "
              + "adapter.",
              REST_CLIENT_CACHE_SIZE,
              REST_CLIENT_CACHE_SIZE
          );

          try {
            // Free resources allocated by this RestClient
            notice.getValue().close();
          } catch (IOException ex) {
            log.warn("Could not close RestClient {}", notice.getValue(), ex);
          }
        }
      })
      .build();

    private static RestClient connect(List<HttpHost> hosts) {

        List cacheKey = ImmutableList.of(hosts, "/", "","");

        try {
            return REST_CLIENTS.get(cacheKey, new Callable<RestClient>() {
              @Override public RestClient call() {
                RestClientBuilder builder = RestClient.builder(hosts.toArray(new HttpHost[hosts.size()]));
                return builder.build();
              }
            });
          } catch (ExecutionException ex) {
            throw new RuntimeException("Cannot return a cached RestClient", ex);
          }
    }  
    @Test
    public void testMapper() throws IOException {

        final ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);

        Map<String,String> map = new HashMap<>();
        map.put("hosts", "['127.0.0.1:9200']");
        if (map.containsKey("hosts")) {
            final List<String> configHosts = mapper.readValue((String) map.get("hosts"),
                new TypeReference<List<String>>() { });

            List<HttpHost> hosts =  configHosts
                                        .stream()
                                        .map(host -> HttpHost.create(host))
                                        .collect(Collectors.toList())
                                        ;

            RestClient restClient = connect(hosts);
            // RestClientBuilder builder = RestClient.builder(hosts.toArray(new HttpHost[hosts.size()]));
            // RestClient restClient =  builder.build() ;
            Request request = new Request("GET", "/");
            Response response =  restClient.performRequest(request);
            String responseBody = EntityUtils.toString(response.getEntity()); 
            System.out.println(responseBody);

        }
    }

    @Test
    public void oldIteratorTest() {

      List<String> names = Arrays.asList("Amir","fdarhad","progress","best","pretty");

      int count = 0 ;

      for (String name : names) {
        if (name.length() == 4) {
          log.info("{}",name.toUpperCase());
          count++ ;

          if(count == 2 )
            break;
        }
      }

    }

    @Test
    public void functionalIteratorTest() {
      List<String> names = Arrays.asList("Amir","fdarhad","progress","best","pretty");

      names.stream()
           .filter(name -> name.length() == 4 )
           .map(String::toUpperCase)
           .limit(2)
           .forEach(n -> log.info("{}",n));
            ;
    }

    @Test
    public void totalSampleTest() {

      List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

      log.info("{}" , TotalSample.totalValues(numbers));
      log.info("{}" , TotalSample.totalEvenValues(numbers));
      log.info("{}" , TotalSample.totalOddValues(numbers));

    }

    @Test
    public void lambdaTotalSampleTest() {

      List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

      log.info("{}",LambdaTotalSample.totalValues(numbers, t -> true));
      log.info("{}",LambdaTotalSample.totalValues(numbers, number -> number % 2 ==0));

      log.info("{}",LambdaTotalSample.totalValues(numbers, LambdaTotalSample::isOdd));
    }

    @Test
    public void samplePlayTest() {

      Sample.call(new DogPerson());
      Sample.call(new CatLover());
    }
}
