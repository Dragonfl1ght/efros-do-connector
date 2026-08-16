package ru.company.connector.efrosdo.dto.edo;

/**
 * Элемент ответа GET /api/v1/SecurityObject/GetFlattenSoHierarchy.
 * Оставлены только поля, нужные по составу для импорта (таблица 9), плюс type для фильтра.
 * TODO: сверить имена полей с реальным стендом.
 */
public record EdoSecurityObject(
        String id,
        String name,
        String description,
        String host,
        String type
) {}
