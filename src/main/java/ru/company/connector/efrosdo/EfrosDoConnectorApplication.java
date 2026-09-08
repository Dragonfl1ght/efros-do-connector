package ru.company.connector.efrosdo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class EfrosDoConnectorApplication {

    public static void main(String[] args) {
        SpringApplication.run(EfrosDoConnectorApplication.class, args);
    }
}
