package ru.company.connector.efrosdo.dto.edo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Элемент ответа POST /api/v1/SecurityObject/GetFlattenSoHierarchy.
 * Проверено на реальном стенде: type принимает значения "Group" (папка иерархии,
 * не объект защиты) и "SecurityObject" (реальный объект защиты).
 * host у объекта защиты бывает трёх видов:
 * - прямым полем host (объекты с источником CI);
 * - внутри ciFeature.host;
 * - внутри одного из acsFeatures[].host (объекты с источником ACS).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EdoSecurityObject(
        String id,
        String parentId,
        String type,
        String name,
        String description,
        String host,
        List<AcsFeature> acsFeatures,
        CiFeature ciFeature
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AcsFeature(String host, String id, String name) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CiFeature(String host) {}
}
