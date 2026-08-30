package ru.company.connector.efrosdo.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
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
 */
@Component
public class EdoClient {

    private final RestClient edoRestClient;
    private final AppProperties props;

    public EdoClient(RestClient edoRestClient, AppProperties props) {
        this.edoRestClient = edoRestClient;
        this.props = props;
    }

    /** Получить свежий JWT. Кеширование добавим, когда решим, что оно нужно. */
    public String login() {
        var body = new EdoLoginRequest(props.edo().login(), props.edo().password());
        EdoLoginResponse resp = edoRestClient.post()
                .uri("/api/identity/Auth/LoginByPassword")
                .body(body)
                .retrieve()
                .body(EdoLoginResponse.class);
        if (resp == null || resp.token() == null || resp.token().accessToken() == null) {
            throw new IllegalStateException("EDO вернул пустой ответ на login");
        }
        return resp.token().accessToken();
    }

    /**
     * POST /api/v1/SecurityObject/GetFlattenSoHierarchy с пустым телом фильтра.
     * TODO: уточнить пагинацию и реальные имена полей элементов ответа.
     */
    public List<EdoSecurityObject> getFlattenSoHierarchy(String jwt) {
        List<EdoSecurityObject> list = edoRestClient.post()
                .uri("/api/v1/SecurityObject/GetFlattenSoHierarchy")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                .body(Map.of())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
        return list == null ? List.of() : list;
    }
}
