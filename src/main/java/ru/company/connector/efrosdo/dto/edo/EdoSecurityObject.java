package ru.company.connector.efrosdo.dto.edo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

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

    public static final String TYPE_SECURITY_OBJECT = "SecurityObject";

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AcsFeature(String host, String id, String name) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CiFeature(String host) {}
}
