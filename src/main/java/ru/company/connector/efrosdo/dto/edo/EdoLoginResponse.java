package ru.company.connector.efrosdo.dto.edo;

/** Ответ POST /api/v1/Auth/LoginByPassword — токен вложен в поле token. Проверено на реальном стенде. */
public record EdoLoginResponse(EdoToken token) {

    public record EdoToken(String accessToken, String refreshToken, long expires) {}
}
