package ru.company.connector.efrosdo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Коннектор Efros Defense Operations -> САОБ.
 * <p>
 * ТМ дёргает /api/run. Мы логинимся в EDO, тянем объекты защиты,
 * маппим поля и отправляем в адаптер e4.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class EfrosDoConnectorApplication {

    public static void main(String[] args) {
        SpringApplication.run(EfrosDoConnectorApplication.class, args);
    }
}
