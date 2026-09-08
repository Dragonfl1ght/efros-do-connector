package ru.company.connector.efrosdo.config;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
class RequiredSettingsCheck {

    RequiredSettingsCheck(AppProperties props) {
        require(props.edo().baseUrl(), "connector.edo.base-url");
        require(props.edo().login(), "connector.edo.login");
        require(props.edo().password(), "connector.edo.password");
        require(props.e4().url(), "connector.e4.url");
    }

    private static void require(String value, String property) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalStateException("Не задана обязательная настройка " + property
                    + ". Укажите её в config/application.yml рядом с jar либо переменной окружения"
                    + " — см. config/application.yml.example");
        }
    }
}
