package ru.company.connector.efrosdo.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import ru.company.connector.efrosdo.config.AppProperties;
import ru.company.connector.efrosdo.dto.tm.ServiceConditionDto;

@Component
public class TmClient {

    private static final Logger log = LoggerFactory.getLogger(TmClient.class);

    private final RestClient tmRestClient;
    private final String serviceUid;

    public TmClient(RestClient tmRestClient, AppProperties props) {
        this.tmRestClient = tmRestClient;
        this.serviceUid = props.tm().serviceUid();
    }

    public void reportFree() {
        try {
            tmRestClient.post()
                    .uri(TmApiPaths.SERVICE_CONDITION)
                    .body(ServiceConditionDto.free(serviceUid))
                    .retrieve()
                    .toBodilessEntity();
            log.info("Менеджер задач уведомлён: коннектор {} свободен", serviceUid);
        } catch (RestClientException e) {
            log.warn("Не удалось сообщить Менеджеру задач статус Free: {}."
                    + " Пока статус Running, ТМ не запустит коннектор по расписанию", e.getMessage());
        }
    }
}
