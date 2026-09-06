package ru.company.connector.efrosdo.dto.e4;

/**
 * Тело для адаптера e4 (импорт одного ТС), по факту согласовано с тимлидом.
 * Смысл полей sSourceInput/includedSys/storeInfA/loadConPhd/swInstalled/sSoftwareInstances
 * не уточнён — см. TODO в DeviceMapper и открытые вопросы в CLAUDE.md.
 */
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
    public record Srcs(
            String guid,
            String name,
            String idAdjSys
    ) {}
}