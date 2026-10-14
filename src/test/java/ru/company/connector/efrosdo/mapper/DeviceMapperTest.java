package ru.company.connector.efrosdo.mapper;

import org.junit.jupiter.api.Test;
import ru.company.connector.efrosdo.dto.e4.DeviceImport;
import ru.company.connector.efrosdo.dto.e4.E4ImportConstants;
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
                securityObject("id-1", "so-1", "10.0.0.1"),
                new EdoSecurityObject("id-2", "Group", "group-1", "descr", "", List.of(), List.of(), List.of()),
                securityObject(null, "no-id", "10.0.0.2")
        ));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("so-1");
    }

    @Test
    void fillsAllThreeIdFieldsWithEdoGuid_soRepeatedSyncUpdatesInsteadOfDuplicating() {
        DeviceImport device = mapOne(securityObject("edo-guid", "so-1", "10.0.0.1"));

        assertThat(device.guid()).isEqualTo("edo-guid");
        assertThat(device.srcs().guid()).isEqualTo("edo-guid");
        assertThat(device.srcs().idAdjSys()).isEqualTo("edo-guid");
        assertThat(device.srcs().name()).isEqualTo(E4ImportConstants.SOURCE_NAME);
    }

    @Test
    void putsHostIntoIpv4_whenItIsIpAddress() {
        DeviceImport device = mapOne(securityObject("id-1", "so-1", "10.0.0.1"));

        assertThat(device.ipv4()).isEqualTo("10.0.0.1");
        assertThat(device.hostName()).isNull();
    }

    @Test
    void putsHostIntoHostName_whenItIsNotIpAddress() {
        DeviceImport device = mapOne(securityObject("id-1", "so-1", "srv-db-01.corp.local"));

        assertThat(device.hostName()).isEqualTo("srv-db-01.corp.local");
        assertThat(device.ipv4()).isNull();
    }

    @Test
    void treatsOutOfRangeOctetsAsHostName_notIpAddress() {
        DeviceImport device = mapOne(securityObject("id-1", "so-1", "999.1.1.1"));

        assertThat(device.hostName()).isEqualTo("999.1.1.1");
        assertThat(device.ipv4()).isNull();
    }

    @Test
    void leavesBothAddressFieldsEmpty_whenObjectHasNoHostAtAll() {
        DeviceImport device = mapOne(withFeatures("", List.of(), List.of()));

        assertThat(device.hostName()).isNull();
        assertThat(device.ipv4()).isNull();
    }

    @Test
    void resolvesHost_fromFirstNonBlankCiFeature_whenTopLevelHostEmpty() {
        DeviceImport device = mapOne(withFeatures("",
                List.of(),
                List.of(new CiFeature(""), new CiFeature("10.72.11.21"))));

        assertThat(device.ipv4()).isEqualTo("10.72.11.21");
    }

    @Test
    void resolvesHost_fromFirstNonBlankAcsFeature_whenTopLevelAndCiFeaturesEmpty() {
        DeviceImport device = mapOne(withFeatures("",
                List.of(new AcsFeature("  ", "f-1", "blank"), new AcsFeature("1.1.0.1", "f-2", "eth0")),
                List.of()));

        assertThat(device.ipv4()).isEqualTo("1.1.0.1");
    }

    @Test
    void prefersTopLevelHost_thenCiFeatures_thenAcsFeatures() {
        List<AcsFeature> acs = List.of(new AcsFeature("3.3.3.3", "f-acs", "acs"));
        List<CiFeature> ci = List.of(new CiFeature("2.2.2.2"));

        assertThat(mapOne(withFeatures("1.1.1.1", acs, ci)).ipv4()).isEqualTo("1.1.1.1");
        assertThat(mapOne(withFeatures("", acs, ci)).ipv4()).isEqualTo("2.2.2.2");
        assertThat(mapOne(withFeatures("", acs, List.of())).ipv4()).isEqualTo("3.3.3.3");
    }

    @Test
    void toleratesMissingFeatureLists() {
        DeviceImport device = mapOne(withFeatures(null, null, null));

        assertThat(device.ipv4()).isNull();
        assertThat(device.hostName()).isNull();
    }

    @Test
    void fillsContractConstants_sameForEveryObject() {
        DeviceImport device = mapOne(securityObject("id-1", "so-1", "10.0.0.1"));

        assertThat(device.sSourceInput()).isEqualTo(E4ImportConstants.NO);
        assertThat(device.includedSys()).isEqualTo(E4ImportConstants.NO);
        assertThat(device.storeInfA()).isEqualTo(E4ImportConstants.NO);
        assertThat(device.loadConPhd()).isEqualTo(E4ImportConstants.YES);
        assertThat(device.swInstalled()).isEqualTo(E4ImportConstants.NO);
        assertThat(device.sSoftwareInstances()).isEqualTo(E4ImportConstants.NO);
    }

    private DeviceImport mapOne(EdoSecurityObject so) {
        List<DeviceImport> result = mapper.toDeviceImports(List.of(so));
        assertThat(result).hasSize(1);
        return result.get(0);
    }

    private EdoSecurityObject securityObject(String id, String name, String host) {
        return new EdoSecurityObject(
                id, EdoSecurityObject.TYPE_SECURITY_OBJECT, name, "descr", host, List.of(), List.of(), List.of());
    }

    private EdoSecurityObject withFeatures(String host, List<AcsFeature> acsFeatures, List<CiFeature> ciFeatures) {
        return new EdoSecurityObject(
                "id-1", EdoSecurityObject.TYPE_SECURITY_OBJECT, "so-1", "descr", host, acsFeatures, ciFeatures, List.of());
    }
}
