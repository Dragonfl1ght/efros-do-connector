package ru.company.connector.efrosdo.dto.tm;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ServiceConditionDto(Service service) {

    public static final String STATUS_FREE = "Free";

    public record Service(@JsonProperty("uId") String uId, Condition condition) {}

    public record Condition(String status) {}

    public static ServiceConditionDto free(String serviceUid) {
        return new ServiceConditionDto(new Service(serviceUid, new Condition(STATUS_FREE)));
    }
}
