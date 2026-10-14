package ru.company.connector.efrosdo.dto.e4;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class E4TokenJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void tokenRequest_usesLowercaseUsername_theOnlyVariantAdapterAccepts() {
        JsonNode json = objectMapper.valueToTree(new E4TokenRequest("DEV\\edo", "secret"));

        assertThat(json.fieldNames()).toIterable().containsExactlyInAnyOrder("username", "password");
        assertThat(json.get("username").asText()).isEqualTo("DEV\\edo");
    }

    @Test
    void tokenResponse_readsAccessToken_fromRealAdapterResponse() throws Exception {
        String realResponse = """
                {"$type":"GRC.ApplicationLayer.Features.External.Tokens.TokenResultDto, GRC.ApplicationLayer",\
                "access_token":"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.payload.signature",\
                "token_type":"bearer","userName":"edo"}""";

        E4TokenResponse response = objectMapper.readValue(realResponse, E4TokenResponse.class);

        assertThat(response.accessToken()).isEqualTo("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.payload.signature");
    }
}
