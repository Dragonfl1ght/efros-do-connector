package ru.company.connector.efrosdo.mapper;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.company.connector.efrosdo.dto.e4.DeviceImport;
import ru.company.connector.efrosdo.dto.edo.EdoSecurityObject;
import ru.company.connector.efrosdo.dto.edo.EdoSecurityObject.AcsFeature;
import ru.company.connector.efrosdo.dto.edo.EdoSecurityObject.CiFeature;

import java.util.List;
import java.util.stream.Stream;

@Component
public class DeviceMapper {

    public List<DeviceImport> toDeviceImports(List<EdoSecurityObject> objects) {
        return objects.stream()
                .filter(DeviceMapper::isSecurityObject)
                .map(DeviceMapper::toDeviceImport)
                .toList();
    }

    private static boolean isSecurityObject(EdoSecurityObject so) {
        return so != null && so.id() != null && EdoSecurityObject.TYPE_SECURITY_OBJECT.equals(so.type());
    }

    private static DeviceImport toDeviceImport(EdoSecurityObject so) {
        HostAddress address = HostAddress.of(resolveHost(so));
        return DeviceImport.of(so.id(), so.name(), so.description(), address.hostName(), address.ipv4());
    }

    private static String resolveHost(EdoSecurityObject so) {
        return firstNonBlank(so.host(), ciFeatureHost(so), acsFeatureHost(so));
    }

    private static String ciFeatureHost(EdoSecurityObject so) {
        CiFeature ciFeature = so.ciFeature();
        return ciFeature == null ? null : ciFeature.host();
    }

    private static String acsFeatureHost(EdoSecurityObject so) {
        List<AcsFeature> acsFeatures = so.acsFeatures();
        if (acsFeatures == null) {
            return null;
        }
        return acsFeatures.stream()
                .map(AcsFeature::host)
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse(null);
    }

    private static String firstNonBlank(String... values) {
        return Stream.of(values)
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse(null);
    }
}
