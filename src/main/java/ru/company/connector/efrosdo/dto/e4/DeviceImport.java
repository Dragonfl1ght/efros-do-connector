package ru.company.connector.efrosdo.dto.e4;

/**
 * Тело для адаптера e4 (импорт одного ТС).
 * Поля по составу для импорта (таблица 9).
 * TODO: точный контракт адаптера e4 согласовать с тимлидом (URL, формат, тип ТС).
 */
public record DeviceImport(
        String name,
        String description,
        String host,
        String idAdjSys,   // guid эфроса
        String source       // константа "Efros Defense Operations"
) {}
