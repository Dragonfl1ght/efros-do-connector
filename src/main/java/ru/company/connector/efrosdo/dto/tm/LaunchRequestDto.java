package ru.company.connector.efrosdo.dto.tm;

/** Тело запроса ТМ на POST /api/integration/launch (раздел 1.5.1 документации ТМ). */
public record LaunchRequestDto(Integration integration) {

    public record Integration(String taskGuid, Object parameters) {}
}
