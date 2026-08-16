package ru.company.connector.efrosdo.dto.edo;

/** Тело POST /api/v1/Login/LoginByPassword. TODO: сверить со свагером. */
public record EdoLoginRequest(String login, String password) {}
