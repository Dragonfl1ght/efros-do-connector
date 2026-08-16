package ru.company.connector.efrosdo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Set;

/**
 * Настройки коннектора: адрес и креды EDO, адрес адаптера e4.
 */
@ConfigurationProperties(prefix = "connector")
public record AppProperties(Edo edo, E4 e4) {

    public record Edo(
            String baseUrl,
            String login,
            String password,
            // TODO: согласовать с тимлидом реальные значения type для "объекта защиты".
            // Пусто = фильтрация по типу выключена, идут все объекты с непустым id.
            Set<String> securityObjectTypes,
            Duration connectTimeout,
            Duration readTimeout
    ) {
        public Edo {
            if (securityObjectTypes == null) {
                securityObjectTypes = Set.of();
            }
            if (connectTimeout == null) {
                connectTimeout = Duration.ofSeconds(5);
            }
            if (readTimeout == null) {
                readTimeout = Duration.ofSeconds(30);
            }
        }
    }

    public record E4(
            String url,
            Duration connectTimeout,
            Duration readTimeout
    ) {
        public E4 {
            if (connectTimeout == null) {
                connectTimeout = Duration.ofSeconds(5);
            }
            if (readTimeout == null) {
                readTimeout = Duration.ofSeconds(30);
            }
        }
    }
}
