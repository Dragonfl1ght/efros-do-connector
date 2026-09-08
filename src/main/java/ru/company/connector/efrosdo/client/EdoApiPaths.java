package ru.company.connector.efrosdo.client;

final class EdoApiPaths {

    static final String LOGIN_BY_PASSWORD = "/api/identity/Auth/LoginByPassword";

    static final String REFRESH_TOKEN = "/api/identity/Auth/refreshToken/{refreshToken}";

    static final String FLATTEN_SO_HIERARCHY = "/api/v1/SecurityObject/GetFlattenSoHierarchy";

    private EdoApiPaths() {
    }
}
