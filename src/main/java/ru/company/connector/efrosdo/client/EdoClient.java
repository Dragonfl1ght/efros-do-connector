package ru.company.connector.efrosdo.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import ru.company.connector.efrosdo.dto.edo.EdoSecurityObject;

import java.util.List;
import java.util.Map;

@Component
public class EdoClient {

    private static final Logger log = LoggerFactory.getLogger(EdoClient.class);

    private final RestClient edoRestClient;
    private final EdoTokenProvider tokenProvider;

    public EdoClient(RestClient edoRestClient, EdoTokenProvider tokenProvider) {
        this.edoRestClient = edoRestClient;
        this.tokenProvider = tokenProvider;
    }

    public List<EdoSecurityObject> getSecurityObjects() {
        try {
            return fetchSecurityObjects(tokenProvider.accessToken());
        } catch (HttpClientErrorException.Unauthorized e) {
            log.warn("EDO отклонил accessToken (401) на GetListSoHierarchy, обновляем токен и повторяем");
            return fetchSecurityObjects(tokenProvider.renew());
        }
    }

    private List<EdoSecurityObject> fetchSecurityObjects(String accessToken) {
        log.debug("EDO hierarchy: POST {}", EdoApiPaths.LIST_SO_HIERARCHY);
        List<EdoSecurityObject> roots = edoRestClient.post()
                .uri(EdoApiPaths.LIST_SO_HIERARCHY)
                .headers(headers -> headers.setBearerAuth(accessToken))
                .body(Map.of())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
        if (roots == null) {
            return List.of();
        }
        return roots.stream()
                .flatMap(EdoSecurityObject::withDescendants)
                .toList();
    }
}
