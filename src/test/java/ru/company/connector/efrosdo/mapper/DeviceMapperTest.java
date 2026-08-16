package ru.company.connector.efrosdo.mapper;

import org.junit.jupiter.api.Test;
import ru.company.connector.efrosdo.config.AppProperties;
import ru.company.connector.efrosdo.dto.e4.DeviceImport;
import ru.company.connector.efrosdo.dto.edo.EdoSecurityObject;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DeviceMapperTest {

    @Test
    void withoutTypeFilterConfigured_keepsAllObjectsWithId() {
        DeviceMapper mapper = new DeviceMapper(propsWithTypes(Set.of()));

        List<DeviceImport> result = mapper.toDeviceImports(List.of(
                new EdoSecurityObject("id-1", "sw-1", "descr", "10.0.0.1", "SWITCH"),
                new EdoSecurityObject(null, "no-id", "descr", "10.0.0.2", "SWITCH")
        ));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).idAdjSys()).isEqualTo("id-1");
        assertThat(result.get(0).source()).isEqualTo("Efros Defense Operations");
    }

    @Test
    void withTypeFilterConfigured_keepsOnlyMatchingTypes() {
        DeviceMapper mapper = new DeviceMapper(propsWithTypes(Set.of("SWITCH")));

        List<DeviceImport> result = mapper.toDeviceImports(List.of(
                new EdoSecurityObject("id-1", "sw-1", "descr", "10.0.0.1", "SWITCH"),
                new EdoSecurityObject("id-2", "folder", "descr", null, "FOLDER")
        ));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("sw-1");
    }

    private AppProperties propsWithTypes(Set<String> types) {
        return new AppProperties(
                new AppProperties.Edo("https://edo", "u", "p", types, Duration.ofSeconds(5), Duration.ofSeconds(5)),
                new AppProperties.E4("https://e4", Duration.ofSeconds(5), Duration.ofSeconds(5))
        );
    }
}
