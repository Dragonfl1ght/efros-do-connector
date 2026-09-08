package ru.company.connector.efrosdo.dto.edo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EdoLoginResponse(EdoToken token) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EdoToken(String accessToken, String refreshToken, long expires) {}
}
