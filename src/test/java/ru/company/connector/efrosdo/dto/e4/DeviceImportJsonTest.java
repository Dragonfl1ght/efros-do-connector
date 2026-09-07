package ru.company.connector.efrosdo.dto.e4;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Имена полей в теле импорта e4 сверяет строкой, поэтому они должны совпадать с согласованным
 * примером буквально. Проверяем тем же ObjectMapper, который настраивает Spring Boot и которым
 * реально сериализует RestClient, — чтобы тест ловил и глобальную смену naming strategy.
 */
@JsonTest
class DeviceImportJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void serializesExactlyTheAgreedFieldNames() {
        DeviceImport device = DeviceImport.of("guid-1", "SRV-DB-01", "Сервер базы данных", null, "10.10.20.5");

        JsonNode json = objectMapper.valueToTree(device);

        assertThat(json.fieldNames()).toIterable().containsExactlyInAnyOrder(
                "guid", "hostName", "sSourceInput", "purpose", "name", "ipv4",
                "includedSys", "storeInfA", "loadConPhd", "swInstalled", "srcs", "sSoftwareInstances");
        assertThat(json.get("srcs").fieldNames()).toIterable().containsExactlyInAnyOrder(
                "guid", "name", "idAdjSys");
    }

    @Test
    void serializesValuesFromTheAgreedExample() {
        DeviceImport device = DeviceImport.of("guid-1", "SRV-DB-01", "Сервер базы данных", null, "10.10.20.5");

        JsonNode json = objectMapper.valueToTree(device);

        assertThat(json.get("guid").asText()).isEqualTo("guid-1");
        assertThat(json.get("name").asText()).isEqualTo("SRV-DB-01");
        assertThat(json.get("purpose").asText()).isEqualTo("Сервер базы данных");
        assertThat(json.get("ipv4").asText()).isEqualTo("10.10.20.5");
        assertThat(json.get("hostName").isNull()).isTrue();
        assertThat(json.get("loadConPhd").asText()).isEqualTo("Да");
        assertThat(json.get("sSourceInput").asText()).isEqualTo("Нет");
        assertThat(json.get("srcs").get("name").asText()).isEqualTo("Efros Defense Operation");
    }

    /** Импорт уходит пачкой — тимлид подтвердил, что массив можно передавать. */
    @Test
    void serializesBatchAsJsonArray() {
        List<DeviceImport> devices = List.of(
                DeviceImport.of("guid-1", "SRV-DB-01", "descr", null, "10.10.20.5"),
                DeviceImport.of("guid-2", "SRV-WEB-02", "descr", "srv-web-02.corp.local", null));

        JsonNode json = objectMapper.valueToTree(devices);

        assertThat(json.isArray()).isTrue();
        assertThat(json).hasSize(2);
        assertThat(json.get(1).get("hostName").asText()).isEqualTo("srv-web-02.corp.local");
    }
}
