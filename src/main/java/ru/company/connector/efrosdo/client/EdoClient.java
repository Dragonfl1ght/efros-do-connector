package ru.company.connector.efrosdo.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import ru.company.connector.efrosdo.config.AppProperties;
import ru.company.connector.efrosdo.dto.edo.EdoLoginRequest;
import ru.company.connector.efrosdo.dto.edo.EdoLoginResponse;
import ru.company.connector.efrosdo.dto.edo.EdoSecurityObject;

import java.util.List;
import java.util.Map;

/**
 * Клиент Efros DO: логин + получение объектов защиты.
 * Пути и формат тела проверены вручную на реальном стенде через Swagger.
 * Токен переиспользуется между вызовами (повторный LoginByPassword на каждый запрос
 * стенд не принимает); при 401 сначала пробуем refreshToken, и только если он тоже
 * не сработал — логинимся заново.
 */
@Component
public class EdoClient {

    private static final Logger log = LoggerFactory.getLogger(EdoClient.class);

    private final RestClient edoRestClient;
    private final AppProperties props;

    private volatile TokenPair token;

    public EdoClient(RestClient edoRestClient, AppProperties props) {
        this.edoRestClient = edoRestClient;
        this.props = props;
    }

    /**
     * POST /api/v1/SecurityObject/GetFlattenSoHierarchy с пустым телом фильтра.
     * TODO: уточнить пагинацию и реальные имена полей элементов ответа.
     */
    public List<EdoSecurityObject> getFlattenSoHierarchy() {
        ensureToken();
        try {
            return fetchHierarchy(token.accessToken());
        } catch (HttpClientErrorException.Unauthorized e) {
            log.warn("EDO отклонил текущий accessToken (401) на GetFlattenSoHierarchy, обновляем токен и повторяем");
            refreshOrLogin();
            return fetchHierarchy(token.accessToken());
        }
    }

    private synchronized void ensureToken() {
        if (token == null) {
            log.info("Токена ещё нет, выполняем первичный логин в EDO");
            token = login();
        }
    }

    private synchronized void refreshOrLogin() {
        if (token != null && token.refreshToken() != null) {
            try {
                token = refresh(token.refreshToken());
                log.info("Токен EDO успешно обновлён через refreshToken");
                return;
            } catch (Exception e) {
                log.warn("refreshToken не сработал ({}), логинимся заново по логину/паролю", e.getMessage());
            }
        }
        token = login();
    }

    private TokenPair login() {
        log.debug("EDO login: POST {} /api/identity/Auth/LoginByPassword", props.edo().baseUrl());
        try {
            var body = new EdoLoginRequest(props.edo().login(), props.edo().password());
            EdoLoginResponse resp = edoRestClient.post()
                    .uri("/api/identity/Auth/LoginByPassword")
                    .body(body)
                    .retrieve()
                    .body(EdoLoginResponse.class);
            TokenPair pair = toTokenPair(resp, "login");
            log.info("Логин в EDO успешен");
            return pair;
        } catch (HttpClientErrorException e) {
            log.error("EDO отклонил LoginByPassword: HTTP {} — проверь логин/пароль в connector.edo.login/password"
                    + " (возможно, не переданы при запуске или устарели)", e.getStatusCode().value());
            throw e;
        }
    }

    private TokenPair refresh(String refreshToken) {
        log.debug("EDO refresh: POST {} /api/identity/Auth/refreshToken/...", props.edo().baseUrl());
        try {
            EdoLoginResponse resp = edoRestClient.post()
                    .uri("/api/identity/Auth/refreshToken/{refreshToken}", refreshToken)
                    .retrieve()
                    .body(EdoLoginResponse.class);
            return toTokenPair(resp, "refresh");
        } catch (HttpClientErrorException e) {
            log.warn("EDO отклонил refreshToken: HTTP {}", e.getStatusCode().value());
            throw e;
        }
    }

    private TokenPair toTokenPair(EdoLoginResponse resp, String operation) {
        if (resp == null || resp.token() == null || resp.token().accessToken() == null) {
            throw new IllegalStateException("EDO вернул пустой ответ на " + operation);
        }
        return new TokenPair(resp.token().accessToken(), resp.token().refreshToken());
    }

    private List<EdoSecurityObject> fetchHierarchy(String accessToken) {
        log.debug("EDO hierarchy: POST {} /api/v1/SecurityObject/GetFlattenSoHierarchy", props.edo().baseUrl());
        List<EdoSecurityObject> list = edoRestClient.post()
                .uri("/api/v1/SecurityObject/GetFlattenSoHierarchy")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .body(Map.of())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
        return list == null ? List.of() : list;
    }

    private record TokenPair(String accessToken, String refreshToken) {}
}
