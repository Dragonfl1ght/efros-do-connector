package ru.company.connector.efrosdo.dto.tm;

public record LaunchResponseDto(boolean accept, Condition condition) {

    private static final String STATUS_RUNNING = "running";

    public record Condition(String status) {}

    public static LaunchResponseDto accepted() {
        return new LaunchResponseDto(true, new Condition(STATUS_RUNNING));
    }
}
