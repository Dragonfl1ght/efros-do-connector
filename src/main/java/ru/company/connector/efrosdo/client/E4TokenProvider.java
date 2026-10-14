package ru.company.connector.efrosdo.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import ru.company.connector.efrosdo.config.AppProperties;
import ru.company.connector.efrosdo.dto.e4.E4TokenRequest;
import ru.company.connector.efrosdo.dto.e4.E4TokenResponse;

@Component
class E4TokenProvider {

    private static final Logger log = LoggerFactory.getLogger(E4TokenProvider.class);

    private final RestClient e4RestClient;
    private final AppProperties props;

    private volatile String token;

    E4TokenProvider(RestClient e4RestClient, AppProperties props) {
        this.e4RestClient = e4RestClient;
        this.props = props;
    }

    synchronized String accessToken() {
        if (token == null) {
            log.info("Токена адаптера e4 ещё нет, запрашиваем");
            token = requestToken();
        }
        return token;
    }

    synchronized String renew() {
        token = requestToken();
        return token;
    }

    private String requestToken() {
        log.debug("e4 token: POST {}", E4ApiPaths.GET_TOKEN);
        try {
            E4TokenResponse response = e4RestClient.post()
                    .uri(E4ApiPaths.GET_TOKEN)
                    .body(new E4TokenRequest(props.e4().login(), props.e4().password()))
                    .retrieve()
                    .body(E4TokenResponse.class);
            if (response == null || !StringUtils.hasText(response.accessToken())) {
                throw new IllegalStateException("Адаптер e4 вернул пустой ответ на getToken");
            }
            log.info("Токен адаптера e4 получен");
            return response.accessToken();
        } catch (HttpStatusCodeException e) {
            log.error("Адаптер e4 отклонил getToken: HTTP {} — проверь connector.e4.login/password",
                    e.getStatusCode().value());
            throw e;
        }
    }
}
