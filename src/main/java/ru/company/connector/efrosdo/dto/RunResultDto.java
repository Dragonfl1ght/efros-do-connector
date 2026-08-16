package ru.company.connector.efrosdo.dto;

/** Итог одного запуска синхронизации — тело ответа /api/run. */
public record RunResultDto(int fetchedCount, int sentCount) {}
