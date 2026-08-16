package ru.company.connector.efrosdo.mapper;

import org.springframework.stereotype.Component;
import ru.company.connector.efrosdo.config.AppProperties;
import ru.company.connector.efrosdo.dto.e4.DeviceImport;
import ru.company.connector.efrosdo.dto.edo.EdoSecurityObject;

import java.util.List;
import java.util.Set;

/**
 * Фильтр объектов защиты EDO и маппинг в модель импорта e4.
 */
@Component
public class DeviceMapper {

    private static final String SOURCE = "Efros Defense Operations";

    private final Set<String> allowedTypes;

    public DeviceMapper(AppProperties props) {
        this.allowedTypes = props.edo().securityObjectTypes();
    }

    public List<DeviceImport> toDeviceImports(List<EdoSecurityObject> objects) {
        return objects.stream()
                .filter(this::isSecurityObject)
                .map(this::toDeviceImport)
                .toList();
    }

    /**
     * Объект должен иметь id и (если список задан в конфиге) подходящий type.
     * TODO: уточнить у тимлида точный набор значений type для "объекта защиты".
     * Пока connector.edo.security-object-types пуст в конфиге — фильтрация по типу выключена,
     * идут все объекты с непустым id.
     */
    private boolean isSecurityObject(EdoSecurityObject so) {
        if (so == null || so.id() == null) {
            return false;
        }
        return allowedTypes.isEmpty() || allowedTypes.contains(so.type());
    }

    private DeviceImport toDeviceImport(EdoSecurityObject so) {
        return new DeviceImport(
                so.name(),
                so.description(),
                so.host(),
                so.id(),
                SOURCE
        );
    }
}
