package ru.company.connector.efrosdo.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.company.connector.efrosdo.dto.tm.LaunchRequestDto;
import ru.company.connector.efrosdo.dto.tm.LaunchResponseDto;
import ru.company.connector.efrosdo.service.DeviceSyncService;

/**
 * Единственный эндпоинт, который дёргает ТМ (Менеджер задач).
 * Путь и формат тела фиксированы документацией ТМ (раздел 1.5.1) — свой путь
 * придумать нельзя, ТМ стучится строго сюда: POST /api/integration/launch.
 */
@RestController
@RequestMapping("/api/integration")
public class IntegrationController {

    private final DeviceSyncService deviceSyncService;

    public IntegrationController(DeviceSyncService deviceSyncService) {
        this.deviceSyncService = deviceSyncService;
    }

    @PostMapping("/launch")
    public LaunchResponseDto launch(@RequestBody(required = false) LaunchRequestDto request) {
        deviceSyncService.run();
        return LaunchResponseDto.accepted();
    }
}
