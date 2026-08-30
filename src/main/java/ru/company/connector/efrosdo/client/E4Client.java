package ru.company.connector.efrosdo.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.company.connector.efrosdo.dto.e4.DeviceImport;

import java.util.List;

/**
 * Клиент адаптера e4 для импорта ТС.
 * TODO: точный контракт (путь, формат ответа, авторизация) согласовать с Иваном.
 */
@Component
public class E4Client {

    private final RestClient e4RestClient;

    public E4Client(RestClient e4RestClient) {
        this.e4RestClient = e4RestClient;
    }

    public void importDevices(List<DeviceImport> devices) {
        e4RestClient.post()
                .body(devices)
                .retrieve()
                .toBodilessEntity();
    }
}
