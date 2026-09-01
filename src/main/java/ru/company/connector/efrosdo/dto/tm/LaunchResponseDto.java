package ru.company.connector.efrosdo.dto.tm;

/**
 * Ответ на POST /api/integration/launch, которого ждёт ТМ.
 * Мы не шлём отдельные уведомления об этапах/статусах (решено не отправлять,
 * см. CLAUDE.md) — весь прогон выполняется синхронно внутри этого запроса,
 * а в ответе всегда фиксированный ack.
 */
public record LaunchResponseDto(boolean accept, Condition condition) {

    public record Condition(String status) {}

    public static LaunchResponseDto accepted() {
        return new LaunchResponseDto(true, new Condition("running"));
    }
}
