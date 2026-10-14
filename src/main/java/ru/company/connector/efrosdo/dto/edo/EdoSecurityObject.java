package ru.company.connector.efrosdo.dto.edo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.stream.Stream;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EdoSecurityObject(
        String id,
        String type,
        String name,
        String description,
        String host,
        List<AcsFeature> acsFeatures,
        List<CiFeature> ciFeatures,
        List<EdoSecurityObject> children
) {

    public static final String TYPE_SECURITY_OBJECT = "SecurityObject";

    public Stream<EdoSecurityObject> withDescendants() {
        Stream<EdoSecurityObject> descendants = children == null
                ? Stream.empty()
                : children.stream().flatMap(EdoSecurityObject::withDescendants);
        return Stream.concat(Stream.of(this), descendants);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AcsFeature(String host, String id, String name) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CiFeature(String host) {}
}
