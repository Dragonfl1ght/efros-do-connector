package ru.company.connector.efrosdo.dto.e4;

import static ru.company.connector.efrosdo.dto.e4.E4ImportConstants.NO;
import static ru.company.connector.efrosdo.dto.e4.E4ImportConstants.SOURCE_NAME;
import static ru.company.connector.efrosdo.dto.e4.E4ImportConstants.YES;

/**
 * Тело импорта одного ТС в e4, согласовано с тимлидом.
 * Собирается через {@link #of}: остальные поля контракта одинаковы для всех объектов.
 * Имена полей должны совпадать с согласованным примером буквально — это проверяет DeviceImportJsonTest.
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

    /**
     * @param id       GUID объекта в EDO. Им заполняются все три id-поля контракта
     *                 (guid, srcs.guid, srcs.idAdjSys), поэтому повторная синхронизация
     *                 того же объекта обновляет запись в e4, а не создаёт дубль
     * @param hostName сетевое имя — если host в EDO не является IP-адресом, иначе null
     * @param ipv4     IP-адрес — если host в EDO является IP-адресом, иначе null
     */
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
