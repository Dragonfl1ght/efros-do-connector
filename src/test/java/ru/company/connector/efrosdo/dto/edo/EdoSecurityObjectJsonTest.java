package ru.company.connector.efrosdo.dto.edo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.core.io.ClassPathResource;
import ru.company.connector.efrosdo.dto.e4.DeviceImport;
import ru.company.connector.efrosdo.mapper.DeviceMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@JsonTest
class EdoSecurityObjectJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    private List<EdoSecurityObject> flattened;

    @BeforeEach
    void readListSoHierarchyResponse() throws IOException {
        try (InputStream json = new ClassPathResource("edo/get-list-so-hierarchy.json").getInputStream()) {
            List<EdoSecurityObject> roots = objectMapper.readValue(json, new TypeReference<>() {});
            flattened = roots.stream().flatMap(EdoSecurityObject::withDescendants).toList();
        }
    }

    @Test
    void flattensNestedChildren_parentBeforeItsSubtree() {
        assertThat(flattened).extracting(EdoSecurityObject::id).containsExactly(
                "30c5f5ef-caac-4c33-b611-a8388d6562a4",
                "4734e00b-276c-4f21-a936-60c525a1a33f",
                "86360448-cbdb-44c4-a065-cbbc9808042d",
                "49a08391-300a-415e-990f-430cfb30f9e8",
                "97433e04-e706-4432-b27d-8cbda69b31a3",
                "00000000-0000-0000-0000-0000000000c1",
                "00000000-0000-0000-0000-0000000000a1");
    }

    @Test
    void readsFieldsOfRealResponse_ignoringUnusedOnes() {
        EdoSecurityObject so = flattened.stream()
                .filter(o -> "97433e04-e706-4432-b27d-8cbda69b31a3".equals(o.id()))
                .findFirst()
                .orElseThrow();

        assertThat(so.type()).isEqualTo(EdoSecurityObject.TYPE_SECURITY_OBJECT);
        assertThat(so.name()).isEqualTo("AutoCreate#10");
        assertThat(so.host()).isEqualTo("10.72.11.95");
        assertThat(so.description()).startsWith("Объект Защиты создан автоматически");
    }

    @Test
    void mapsEverySecurityObject_withHostFromTopLevelOrCiOrAcsFeatures() {
        List<DeviceImport> devices = new DeviceMapper().toDeviceImports(flattened);

        assertThat(devices).extracting(DeviceImport::guid, DeviceImport::ipv4).containsExactly(
                tuple("97433e04-e706-4432-b27d-8cbda69b31a3", "10.72.11.95"),
                tuple("00000000-0000-0000-0000-0000000000c1", "10.72.11.21"),
                tuple("00000000-0000-0000-0000-0000000000a1", "1.1.0.1"));
    }
}
