package ru.company.connector.efrosdo.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.ResponseCreator;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import ru.company.connector.efrosdo.config.AppProperties;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class E4ClientTest {

    private static final String BASE_URL = "http://e4";

    private MockRestServiceServer adapter;
    private E4Client client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        adapter = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();
        AppProperties props = new AppProperties(null,
                new AppProperties.E4(BASE_URL, "DEV\\edo", "secret", null, null), null);
        client = new E4Client(restClient, new E4TokenProvider(restClient, props));
    }

    @Test
    void expiredTokenRejectedWith403_renewsTokenAndRetriesOnce() {
        expectTokenIssued("token-1");
        expectImport("token-1", withSuccess("\"Импорт завершен успешно\"", MediaType.APPLICATION_JSON));
        expectImport("token-1", withStatus(HttpStatus.FORBIDDEN).body("\"Ошибка авторизации\""));
        expectTokenIssued("token-2");
        expectImport("token-2", withSuccess("\"Импорт завершен успешно\"", MediaType.APPLICATION_JSON));

        client.importDevices(List.of());
        client.importDevices(List.of());

        adapter.verify();
    }

    @Test
    void expiredTokenRejectedWith401_renewsTokenAndRetriesOnce() {
        expectTokenIssued("token-1");
        expectImport("token-1", withStatus(HttpStatus.UNAUTHORIZED));
        expectTokenIssued("token-2");
        expectImport("token-2", withSuccess("\"Импорт завершен успешно\"", MediaType.APPLICATION_JSON));

        client.importDevices(List.of());

        adapter.verify();
    }

    @Test
    void freshTokenAlsoRejected_failsWithoutEndlessRetries() {
        expectTokenIssued("token-1");
        expectImport("token-1", withStatus(HttpStatus.FORBIDDEN).body("\"Ошибка авторизации\""));
        expectTokenIssued("token-2");
        expectImport("token-2", withStatus(HttpStatus.FORBIDDEN).body("\"Ошибка авторизации\""));

        assertThatThrownBy(() -> client.importDevices(List.of()))
                .isInstanceOf(HttpClientErrorException.Forbidden.class);
        adapter.verify();
    }

    private void expectTokenIssued(String token) {
        adapter.expect(requestTo(BASE_URL + E4ApiPaths.GET_TOKEN))
                .andRespond(withSuccess("{\"access_token\":\"" + token + "\",\"token_type\":\"bearer\"}",
                        MediaType.APPLICATION_JSON));
    }

    private void expectImport(String token, ResponseCreator response) {
        adapter.expect(requestTo(BASE_URL + E4ApiPaths.IMPORT_HARDWARE))
                .andExpect(header("Authorization", "Bearer " + token))
                .andRespond(response);
    }
}
