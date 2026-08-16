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

/**
 * Клиент Efros DO: логин + получение объектов защиты.
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
                .uri("/api/v1/Login/LoginByPassword")   // TODO: сверить путь
                .body(body)
                .retrieve()
                .body(EdoLoginResponse.class);
        if (resp == null || resp.accessToken() == null) {
            throw new IllegalStateException("EDO вернул пустой ответ на login");
        }
        return resp.accessToken();
    }

    /** GET /api/v1/SecurityObject/GetFlattenSoHierarchy. TODO: уточнить пагинацию. */
    public List<EdoSecurityObject> getFlattenSoHierarchy(String jwt) {
        List<EdoSecurityObject> list = edoRestClient.get()
                .uri("/api/v1/SecurityObject/GetFlattenSoHierarchy")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
        return list == null ? List.of() : list;
    }
}
