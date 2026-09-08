package ru.company.connector.efrosdo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.company.connector.efrosdo.dto.RunResultDto;
import ru.company.connector.efrosdo.dto.tm.LaunchRequestDto;
import ru.company.connector.efrosdo.dto.tm.LaunchResponseDto;
import ru.company.connector.efrosdo.service.DeviceSyncService;

@RestController
@RequestMapping("/api/integration")
public class IntegrationController {

    private static final Logger log = LoggerFactory.getLogger(IntegrationController.class);

    private static final String UNKNOWN_TASK_GUID = "<без taskGuid>";

    private final DeviceSyncService deviceSyncService;

    public IntegrationController(DeviceSyncService deviceSyncService) {
        this.deviceSyncService = deviceSyncService;
    }

    @PostMapping("/launch")
    public LaunchResponseDto launch(@RequestBody(required = false) LaunchRequestDto request) {
        String taskGuid = taskGuid(request);

        RunResultDto result = deviceSyncService.run();

        log.info("Задача {} завершена: получено из EDO {}, отправлено в e4 {}",
                taskGuid, result.fetchedCount(), result.sentCount());
        return LaunchResponseDto.accepted();
    }

    private static String taskGuid(LaunchRequestDto request) {
        return request == null || request.integration() == null
                ? UNKNOWN_TASK_GUID
                : request.integration().taskGuid();
    }
}
