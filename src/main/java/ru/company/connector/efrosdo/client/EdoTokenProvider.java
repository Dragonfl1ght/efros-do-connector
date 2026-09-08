package ru.company.connector.efrosdo.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import ru.company.connector.efrosdo.config.AppProperties;
import ru.company.connector.efrosdo.dto.edo.EdoLoginRequest;
import ru.company.connector.efrosdo.dto.edo.EdoLoginResponse;

@Component
class EdoTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(EdoTokenProvider.class);

    private final RestClient edoRestClient;
    private final AppProperties props;

    private volatile TokenPair token;

    EdoTokenProvider(RestClient edoRestClient, AppProperties props) {
        this.edoRestClient = edoRestClient;
        this.props = props;
    }

    synchronized String accessToken() {
        if (token == null) {
            log.info("Токена ещё нет, выполняем первичный логин в EDO");
            token = login();
        }
        return token.accessToken();
    }

    synchronized String renew() {
        if (token != null && token.refreshToken() != null) {
            try {
                token = refresh(token.refreshToken());
                log.info("Токен EDO успешно обновлён через refreshToken");
                return token.accessToken();
            } catch (RuntimeException e) {
                log.warn("refreshToken не сработал ({}), логинимся заново по логину/паролю", e.getMessage());
            }
        }
        token = login();
        return token.accessToken();
    }

    private TokenPair login() {
        log.debug("EDO login: POST {}", EdoApiPaths.LOGIN_BY_PASSWORD);
        try {
            EdoLoginResponse response = edoRestClient.post()
                    .uri(EdoApiPaths.LOGIN_BY_PASSWORD)
                    .body(new EdoLoginRequest(props.edo().login(), props.edo().password()))
                    .retrieve()
                    .body(EdoLoginResponse.class);
            TokenPair pair = toTokenPair(response, "login");
            log.info("Логин в EDO успешен");
            return pair;
        } catch (HttpClientErrorException e) {
            log.error("EDO отклонил LoginByPassword: HTTP {} — проверь connector.edo.login/password",
                    e.getStatusCode().value());
            throw e;
        }
    }

    private TokenPair refresh(String refreshToken) {
        log.debug("EDO refresh: POST {}", EdoApiPaths.REFRESH_TOKEN);
        try {
            EdoLoginResponse response = edoRestClient.post()
                    .uri(EdoApiPaths.REFRESH_TOKEN, refreshToken)
                    .retrieve()
                    .body(EdoLoginResponse.class);
            return toTokenPair(response, "refresh");
        } catch (HttpClientErrorException e) {
            log.warn("EDO отклонил refreshToken: HTTP {}", e.getStatusCode().value());
            throw e;
        }
    }

    private static TokenPair toTokenPair(EdoLoginResponse response, String operation) {
        if (response == null || response.token() == null || response.token().accessToken() == null) {
            throw new IllegalStateException("EDO вернул пустой ответ на " + operation);
        }
        return new TokenPair(response.token().accessToken(), response.token().refreshToken());
    }

    private record TokenPair(String accessToken, String refreshToken) {}
}
