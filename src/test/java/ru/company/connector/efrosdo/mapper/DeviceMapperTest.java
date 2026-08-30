package ru.company.connector.efrosdo.mapper;

import org.junit.jupiter.api.Test;
import ru.company.connector.efrosdo.dto.e4.DeviceImport;
import ru.company.connector.efrosdo.dto.edo.EdoSecurityObject;
import ru.company.connector.efrosdo.dto.edo.EdoSecurityObject.AcsFeature;
import ru.company.connector.efrosdo.dto.edo.EdoSecurityObject.CiFeature;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeviceMapperTest {

    private final DeviceMapper mapper = new DeviceMapper();

    @Test
    void keepsOnlySecurityObjects_dropsGroupsAndObjectsWithoutId() {
        List<DeviceImport> result = mapper.toDeviceImports(List.of(
                new EdoSecurityObject("id-1", null, "SecurityObject", "so-1", "descr", "10.0.0.1", List.of(), null),
                new EdoSecurityObject("id-2", "id-1", "Group", "group-1", "descr", null, List.of(), null),
                new EdoSecurityObject(null, "id-1", "SecurityObject", "no-id", "descr", "10.0.0.2", List.of(), null)
        ));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).idEfros()).isEqualTo("id-1");
        assertThat(result.get(0).source()).isEqualTo("Efros Defense Operations");
        assertThat(result.get(0).host()).isEqualTo("10.0.0.1");
    }

    @Test
    void resolvesHost_fromCiFeature_whenTopLevelHostMissing() {
        var so = new EdoSecurityObject("id-1", null, "SecurityObject", "ci-so", "descr", null,
                List.of(), new CiFeature("10.1.1.1"));

        List<DeviceImport> result = mapper.toDeviceImports(List.of(so));

        assertThat(result.get(0).host()).isEqualTo("10.1.1.1");
    }

    @Test
    void resolvesHost_fromFirstAcsFeature_whenTopLevelAndCiFeatureMissing() {
        var so = new EdoSecurityObject("id-1", null, "SecurityObject", "acs-so", "descr", null,
                List.of(new AcsFeature("1.1.0.1", "feature-id", "feature-name")), null);

        List<DeviceImport> result = mapper.toDeviceImports(List.of(so));

        assertThat(result.get(0).host()).isEqualTo("1.1.0.1");
    }
}
