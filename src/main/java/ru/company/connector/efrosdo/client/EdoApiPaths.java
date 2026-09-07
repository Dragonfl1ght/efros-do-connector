package ru.company.connector.efrosdo.client;

/**
 * Пути API Efros DO. Проверены вручную на реальном стенде через Swagger,
 * а не взяты из документации — она по ним расходится с фактом.
 */
final class EdoApiPaths {

    static final String LOGIN_BY_PASSWORD = "/api/identity/Auth/LoginByPassword";

    /** TODO: метод (POST) не подтверждён через Swagger, взят из описания стенда. */
    static final String REFRESH_TOKEN = "/api/identity/Auth/refreshToken/{refreshToken}";

    static final String FLATTEN_SO_HIERARCHY = "/api/v1/SecurityObject/GetFlattenSoHierarchy";

    private EdoApiPaths() {
    }
}
