package ru.company.connector.efrosdo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Objects;

@ConfigurationProperties(prefix = "connector")
public record AppProperties(Edo edo, E4 e4) {

    private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(30);

    public record Edo(
            String baseUrl,
            String login,
            String password,
            Duration connectTimeout,
            Duration readTimeout
    ) {
        public Edo {
            connectTimeout = Objects.requireNonNullElse(connectTimeout, DEFAULT_CONNECT_TIMEOUT);
            readTimeout = Objects.requireNonNullElse(readTimeout, DEFAULT_READ_TIMEOUT);
        }
    }

    public record E4(
            String url,
            String login,
            String password,
            Duration connectTimeout,
            Duration readTimeout
    ) {
        public E4 {
            connectTimeout = Objects.requireNonNullElse(connectTimeout, DEFAULT_CONNECT_TIMEOUT);
            readTimeout = Objects.requireNonNullElse(readTimeout, DEFAULT_READ_TIMEOUT);
        }
    }
}
