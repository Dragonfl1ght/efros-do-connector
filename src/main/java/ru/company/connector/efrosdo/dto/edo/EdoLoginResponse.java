package ru.company.connector.efrosdo.dto.edo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Ответ POST /api/identity/Auth/LoginByPassword и /api/identity/Auth/refreshToken/{refreshToken} —
 * токен вложен в поле token, остальные поля верхнего уровня (userId, roles, claims и т.д.) не используются.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EdoLoginResponse(EdoToken token) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EdoToken(String accessToken, String refreshToken, long expires) {}
}
