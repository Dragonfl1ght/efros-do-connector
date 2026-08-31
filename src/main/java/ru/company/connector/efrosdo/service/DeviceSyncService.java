package ru.company.connector.efrosdo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.company.connector.efrosdo.client.E4Client;
import ru.company.connector.efrosdo.client.EdoClient;
import ru.company.connector.efrosdo.dto.RunResultDto;
import ru.company.connector.efrosdo.dto.e4.DeviceImport;
import ru.company.connector.efrosdo.dto.edo.EdoSecurityObject;
import ru.company.connector.efrosdo.mapper.DeviceMapper;

import java.util.List;

/**
 * Один прогон синхронизации: логин в EDO -> объекты защиты -> фильтр/маппинг -> отправка в e4.
 */
@Service
public class DeviceSyncService {

    private static final Logger log = LoggerFactory.getLogger(DeviceSyncService.class);

    private final EdoClient edoClient;
    private final E4Client e4Client;
    private final DeviceMapper deviceMapper;

    public DeviceSyncService(EdoClient edoClient, E4Client e4Client, DeviceMapper deviceMapper) {
        this.edoClient = edoClient;
        this.e4Client = e4Client;
        this.deviceMapper = deviceMapper;
    }

    public RunResultDto run() {
        log.info("Запуск задачи");

        List<EdoSecurityObject> objects = edoClient.getFlattenSoHierarchy();
        log.info("EDO вернул {} записей", objects.size());

        List<DeviceImport> devices = deviceMapper.toDeviceImports(objects);
        log.info("После фильтра осталось {} ТС, отправляем в e4", devices.size());

        e4Client.importDevices(devices);
        log.info("Задача завершена");

        return new RunResultDto(objects.size(), devices.size());
    }
}
