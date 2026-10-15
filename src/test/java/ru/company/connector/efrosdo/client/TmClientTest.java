package ru.company.connector.efrosdo.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import ru.company.connector.efrosdo.config.AppProperties;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class TmClientTest {

    private static final String BASE_URL = "https://tm";

    private MockRestServiceServer taskManager;
    private TmClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        taskManager = MockRestServiceServer.bindTo(builder).build();
        AppProperties props = new AppProperties(null, null,
                new AppProperties.Tm(BASE_URL, "efrosdo_uko", null, null, null));
        client = new TmClient(builder.build(), props);
    }

    @Test
    void reportFree_sendsServiceConditionInTaskManagerSchema() {
        taskManager.expect(requestTo(BASE_URL + TmApiPaths.SERVICE_CONDITION))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("""
                        {"service": {"uId": "efrosdo_uko", "condition": {"status": "Free"}}}""", true))
                .andRespond(withSuccess());

        client.reportFree();

        taskManager.verify();
    }

    @Test
    void reportFree_taskManagerFailure_doesNotFailTheRun() {
        taskManager.expect(requestTo(BASE_URL + TmApiPaths.SERVICE_CONDITION))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatCode(client::reportFree).doesNotThrowAnyException();
        taskManager.verify();
    }
}
