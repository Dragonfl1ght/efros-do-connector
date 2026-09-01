package ru.company.connector.efrosdo.dto.edo;

/** Тело POST /api/identity/Auth/LoginByPassword. Поле называется userName, не login (проверено на реальном стенде). */
public record EdoLoginRequest(String userName, String password) {}
