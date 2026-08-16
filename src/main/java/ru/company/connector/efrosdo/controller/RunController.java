package ru.company.connector.efrosdo.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.company.connector.efrosdo.dto.RunResultDto;
import ru.company.connector.efrosdo.service.DeviceSyncService;

/**
 * Единственный эндпоинт, который дёргает ТМ.
 * POST /api/run — забирает объекты защиты из EDO и отправляет в e4.
 */
@RestController
@RequestMapping("/api/run")
public class RunController {

    private final DeviceSyncService deviceSyncService;

    public RunController(DeviceSyncService deviceSyncService) {
        this.deviceSyncService = deviceSyncService;
    }

    @PostMapping
    public RunResultDto run() {
        return deviceSyncService.run();
    }
}
