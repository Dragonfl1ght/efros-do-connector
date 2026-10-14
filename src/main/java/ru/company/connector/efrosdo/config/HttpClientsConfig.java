package ru.company.connector.efrosdo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class HttpClientsConfig {

    @Bean
    RestClient edoRestClient(AppProperties props) {
        AppProperties.Edo edo = props.edo();
        return RestClient.builder()
                .baseUrl(edo.baseUrl())
                .requestFactory(new TrustAllRequestFactory(edo.connectTimeout(), edo.readTimeout()))
                .build();
    }

    @Bean
    RestClient e4RestClient(AppProperties props) {
        AppProperties.E4 e4 = props.e4();
        return RestClient.builder()
                .baseUrl(e4.baseUrl())
                .requestFactory(new BufferingClientHttpRequestFactory(
                        new TrustAllRequestFactory(e4.connectTimeout(), e4.readTimeout())))
                .build();
    }
}
