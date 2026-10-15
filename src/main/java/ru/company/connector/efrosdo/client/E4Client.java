package ru.company.connector.efrosdo.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import ru.company.connector.efrosdo.dto.e4.DeviceImport;

import java.util.List;

@Component
public class E4Client {

    private static final Logger log = LoggerFactory.getLogger(E4Client.class);

    private final RestClient e4RestClient;
    private final E4TokenProvider tokenProvider;

    public E4Client(RestClient e4RestClient, E4TokenProvider tokenProvider) {
        this.e4RestClient = e4RestClient;
        this.tokenProvider = tokenProvider;
    }

    public void importDevices(List<DeviceImport> devices) {
        try {
            sendDevices(devices, tokenProvider.accessToken());
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden e) {
            log.warn("Адаптер e4 отклонил токен (HTTP {}), запрашиваем новый и повторяем",
                    e.getStatusCode().value());
            sendDevices(devices, tokenProvider.renew());
        }
    }

    private void sendDevices(List<DeviceImport> devices, String accessToken) {
        log.debug("e4 import: POST {}, ТС: {}", E4ApiPaths.IMPORT_HARDWARE, devices.size());
        String result = e4RestClient.post()
                .uri(E4ApiPaths.IMPORT_HARDWARE)
                .headers(headers -> headers.setBearerAuth(accessToken))
                .body(devices)
                .retrieve()
                .body(String.class);
        log.info("Адаптер e4 ответил: {}", result);
    }
}
