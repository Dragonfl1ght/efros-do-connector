package ru.company.connector.efrosdo.dto;

/** Итог одного запуска синхронизации, для логирования внутри DeviceSyncService. */
public record RunResultDto(int fetchedCount, int sentCount) {}
