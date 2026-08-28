package ru.company.connector.efrosdo.mapper;

import org.springframework.stereotype.Component;
import ru.company.connector.efrosdo.dto.e4.DeviceImport;
import ru.company.connector.efrosdo.dto.edo.EdoSecurityObject;
import ru.company.connector.efrosdo.dto.edo.EdoSecurityObject.AcsFeature;

import java.util.List;

/**
 * Фильтр объектов защиты EDO и маппинг в модель импорта e4.
 */
@Component
public class DeviceMapper {

    private static final String SOURCE = "Efros Defense Operations";
    private static final String SECURITY_OBJECT_TYPE = "SecurityObject";

    public List<DeviceImport> toDeviceImports(List<EdoSecurityObject> objects) {
        return objects.stream()
                .filter(this::isSecurityObject)
                .map(this::toDeviceImport)
                .toList();
    }

    /** Проверено на реальном стенде: "Group" — папка иерархии, "SecurityObject" — объект защиты. */
    private boolean isSecurityObject(EdoSecurityObject so) {
        return so != null && so.id() != null && SECURITY_OBJECT_TYPE.equals(so.type());
    }

    private DeviceImport toDeviceImport(EdoSecurityObject so) {
        return new DeviceImport(
                so.name(),
                so.description(),
                resolveHost(so),
                so.id(),
                SOURCE
        );
    }

    /**
     * host бывает в трёх местах в зависимости от источника объекта (проверено на реальных данных).
     * TODO: если у объекта несколько acsFeatures с разными host — сейчас берётся первый непустой,
     * уточнить у тимлида, нужна ли другая логика для такого случая.
     */
    private String resolveHost(EdoSecurityObject so) {
        if (isNotBlank(so.host())) {
            return so.host();
        }
        if (so.ciFeature() != null && isNotBlank(so.ciFeature().host())) {
            return so.ciFeature().host();
        }
        if (so.acsFeatures() != null) {
            for (AcsFeature feature : so.acsFeatures()) {
                if (isNotBlank(feature.host())) {
                    return feature.host();
                }
            }
        }
        return null;
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }
}
