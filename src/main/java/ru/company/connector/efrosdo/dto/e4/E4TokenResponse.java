package ru.company.connector.efrosdo.dto.e4;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record E4TokenResponse(@JsonProperty("access_token") String accessToken) {}
