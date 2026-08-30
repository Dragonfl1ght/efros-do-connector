package ru.company.connector.efrosdo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;

@Configuration
public class HttpClientsConfig {

    @Bean
    RestClient edoRestClient(AppProperties props) {
        return RestClient.builder()
                .baseUrl(props.edo().baseUrl())
                .requestFactory(trustAllRequestFactory(props.edo().connectTimeout(), props.edo().readTimeout()))
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

    /** Тестовый стенд EDO ходит по самоподписанному сертификату (тот же случай, что и curl -k). */
    private ClientHttpRequestFactory trustAllRequestFactory(Duration connectTimeout, Duration readTimeout) {
        SSLContext sslContext = trustAllSslContext();
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory() {
            @Override
            protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
                if (connection instanceof HttpsURLConnection https) {
                    https.setSSLSocketFactory(sslContext.getSocketFactory());
                    https.setHostnameVerifier((hostname, session) -> true);
                }
                super.prepareConnection(connection, httpMethod);
            }
        };
        factory.setConnectTimeout((int) connectTimeout.toMillis());
        factory.setReadTimeout((int) readTimeout.toMillis());
        return factory;
    }

    private SSLContext trustAllSslContext() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {}

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {}

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }}, new SecureRandom());
            return sslContext;
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new IllegalStateException("Не удалось создать доверяющий всем сертификатам SSLContext", e);
        }
    }
}
