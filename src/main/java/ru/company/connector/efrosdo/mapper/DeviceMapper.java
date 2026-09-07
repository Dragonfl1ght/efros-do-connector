package ru.company.connector.efrosdo.mapper;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.company.connector.efrosdo.dto.e4.DeviceImport;
import ru.company.connector.efrosdo.dto.edo.EdoSecurityObject;
import ru.company.connector.efrosdo.dto.edo.EdoSecurityObject.AcsFeature;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Фильтр объектов защиты EDO и маппинг в тело импорта e4.
 */
@Component
public class DeviceMapper {

    /** Октеты 0-255: по нестрогому шаблону "999.1.1.1" тоже считался бы адресом. */
    private static final Pattern IPV4 = Pattern.compile(
            "^((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)$");

    public List<DeviceImport> toDeviceImports(List<EdoSecurityObject> objects) {
        return objects.stream()
                .filter(this::isSecurityObject)
                .map(this::toDeviceImport)
                .toList();
    }

    /** Проверено на реальном стенде: "Group" — папка иерархии, "SecurityObject" — объект защиты. */
    private boolean isSecurityObject(EdoSecurityObject so) {
        return so != null && so.id() != null && EdoSecurityObject.TYPE_SECURITY_OBJECT.equals(so.type());
    }

    private DeviceImport toDeviceImport(EdoSecurityObject so) {
        HostAddress address = HostAddress.of(resolveHost(so));
        return DeviceImport.of(so.id(), so.name(), so.description(), address.hostName(), address.ipv4());
    }

    /**
     * host бывает в трёх местах в зависимости от источника объекта (проверено на реальных данных).
     * TODO: если у объекта несколько acsFeatures с разными host — сейчас берётся первый непустой,
     * уточнить у тимлида, нужна ли другая логика для такого случая.
     */
    private String resolveHost(EdoSecurityObject so) {
        if (StringUtils.hasText(so.host())) {
            return so.host();
        }
        if (so.ciFeature() != null && StringUtils.hasText(so.ciFeature().host())) {
            return so.ciFeature().host();
        }
        return firstAcsHost(so);
    }

    private String firstAcsHost(EdoSecurityObject so) {
        if (so.acsFeatures() == null) {
            return null;
        }
        return so.acsFeatures().stream()
                .map(AcsFeature::host)
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse(null);
    }

    /**
     * Один host из EDO раскладывается на два поля e4: IP-адрес идёт в ipv4,
     * всё остальное (например, DNS-имя) — в hostName. Заполнено не больше одного из двух.
     */
    private record HostAddress(String hostName, String ipv4) {

        static HostAddress of(String host) {
            if (!StringUtils.hasText(host)) {
                return new HostAddress(null, null);
            }
            return IPV4.matcher(host).matches()
                    ? new HostAddress(null, host)
                    : new HostAddress(host, null);
        }
    }
}
