package ru.company.connector.efrosdo.dto.e4;

import static ru.company.connector.efrosdo.dto.e4.E4ImportConstants.NO;
import static ru.company.connector.efrosdo.dto.e4.E4ImportConstants.SOURCE_NAME;
import static ru.company.connector.efrosdo.dto.e4.E4ImportConstants.YES;

public record DeviceImport(
        String guid,
        String hostName,
        String sSourceInput,
        String purpose,
        String name,
        String ipv4,
        String includedSys,
        String storeInfA,
        String loadConPhd,
        String swInstalled,
        Srcs srcs,
        String sSoftwareInstances
) {

    public static DeviceImport of(String id, String name, String purpose, String hostName, String ipv4) {
        return new DeviceImport(
                id,
                hostName,
                NO,
                purpose,
                name,
                ipv4,
                NO,
                NO,
                YES,
                NO,
                new Srcs(id, SOURCE_NAME, id),
                NO
        );
    }

    public record Srcs(String guid, String name, String idAdjSys) {}
}
