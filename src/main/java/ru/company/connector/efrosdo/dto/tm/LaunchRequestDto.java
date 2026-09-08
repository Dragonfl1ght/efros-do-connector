package ru.company.connector.efrosdo.dto.tm;

public record LaunchRequestDto(Integration integration) {

    public record Integration(String taskGuid, Object parameters) {}
}
