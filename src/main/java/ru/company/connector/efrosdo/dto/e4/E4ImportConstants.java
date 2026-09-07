package ru.company.connector.efrosdo.dto.e4;

/**
 * Фиксированные значения контракта импорта ТС в e4: одинаковы для всех объектов,
 * подтверждены тимлидом на реальном примере тела запроса.
 */
public final class E4ImportConstants {

    /**
     * Название смежной системы в e4. Без "s" на конце — намеренно отличается от
     * "Efros Defense Operations" в registration/regconn.json: там контракт ТМ, здесь — e4.
     */
    public static final String SOURCE_NAME = "Efros Defense Operation";

    public static final String YES = "Да";
    public static final String NO = "Нет";

    private E4ImportConstants() {
    }
}
