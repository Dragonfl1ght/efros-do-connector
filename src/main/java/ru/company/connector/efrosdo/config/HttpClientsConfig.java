package ru.company.connector.efrosdo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class HttpClientsConfig {

    @Bean
    RestClient edoRestClient(AppProperties props) {
        return RestClient.builder()
                .baseUrl(props.edo().baseUrl())
                .requestFactory(requestFactory(props.edo().connectTimeout(), props.edo().readTimeout()))
                .build();
    }

    @Bean
    RestClient e4RestClient(AppProperties props) {
        return RestClient.builder()
                .baseUrl(props.e4().url())
                .requestFactory(requestFactory(props.e4().connectTimeout(), props.e4().readTimeout()))
                .build();
    }

    private ClientHttpRequestFactory requestFactory(Duration connectTimeout, Duration readTimeout) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) connectTimeout.toMillis());
        factory.setReadTimeout((int) readTimeout.toMillis());
        return factory;
    }
}
