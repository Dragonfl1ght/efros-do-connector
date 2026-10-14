# efros-do-connector

Коннектор Efros Defense Operations → САОБ (через адаптер e4).

**Флоу:** ТМ (Менеджер задач) дёргает `POST /api/integration/launch` (путь и формат тела фиксированы документацией ТМ, раздел 1.5.1 — свой путь придумать нельзя) → логинимся в EDO (`POST /api/identity/Auth/LoginByPassword`) → `POST /api/v1/SecurityObject/GetListSoHierarchy` с пустым телом фильтра → разворачивание дерева в плоский список → фильтр по `type == "SecurityObject"` → маппинг полей → POST в адаптер e4. Синхронно, в одном потоке, без очередей и без стейта.

Регистрация сервиса в ТМ — **разовая ручная операция**, не код (см. `registration/`). Уведомления об этапах/статусах ТМ (`running` → `free`) сознательно не отправляются — см. `CLAUDE.md`, раздел "Что мы сознательно НЕ делаем".

## Структура (по слоям)

```
controller/IntegrationController.java  - единственный входящий эндпоинт POST /api/integration/launch, только HTTP-обвязка
service/DeviceSyncService.java         - оркестрация одного прогона: login -> fetch -> map -> send
mapper/DeviceMapper.java               - фильтр объектов защиты + маппинг EdoSecurityObject -> DeviceImport
mapper/HostAddress.java                - разбор host на ipv4/hostName (регекс IPv4)
client/EdoClient.java                  - HTTP-вызов EDO: GetListSoHierarchy, разворачивание дерева, повтор при 401
client/EdoTokenProvider.java           - логин/refresh и кеш токена EDO
client/EdoApiPaths.java                - пути API EDO одной кучей
client/E4Client.java                   - HTTP-вызов адаптера e4: importDevices с Bearer-токеном, повтор при 401
client/E4TokenProvider.java            - получение и кеш токена адаптера через /api/getToken
dto/tm/                                - LaunchRequestDto, LaunchResponseDto (контракт ТМ)
dto/edo/                               - EdoLoginRequest, EdoLoginResponse, EdoSecurityObject
dto/e4/DeviceImport.java               - тело импорта ТС в e4, собирается через DeviceImport.of()
dto/e4/E4ImportConstants.java          - фиксированные значения контракта e4 (Да/Нет, имя источника)
dto/RunResultDto.java                  - внутренний итог прогона (для логов DeviceSyncService)
exception/GlobalExceptionHandler.java  - единая обработка ошибок EDO/e4 -> 502 + сообщение
config/AppProperties.java              - URL/креды EDO, URL/креды e4, таймауты
config/HttpClientsConfig.java          - RestClient beans (edoRestClient, e4RestClient)
config/TrustAllRequestFactory.java     - доверие самоподписанным сертификатам стендов
config/RequiredSettingsCheck.java      - падение на старте, если не задана обязательная настройка
registration/                          - разовая ручная регистрация в ТМ (regconn.json, register.sh, README.md)
config/application.yml.example         - шаблон внешней конфигурации (лежит в корне, не в jar)
```

Контроллер не содержит бизнес-логики — только делегирует в `DeviceSyncService`. Фильтрация и маппинг вынесены в `DeviceMapper`, поэтому тестируются юнит-тестом без поднятия Spring-контекста (`DeviceMapperTest`). Имена полей в теле импорта e4 зафиксированы отдельным тестом (`DeviceImportJsonTest`) — e4 сверяет их строкой.

Комментариев в коде нет — это решение команды. Смысл несут имена классов, методов и тестов, а всё, что нужно знать про контракты и договорённости, собрано в `CLAUDE.md`. Открытые вопросы там же, а не в `// TODO`.

## Настройка

В jar не зашито ни одного адреса и ни одного пароля — коннектор настраивается снаружи, без пересборки. Источники, по возрастанию приоритета:

1. **Значения по умолчанию** внутри jar — только порт, таймауты и уровень логов.
2. **Внешний файл `config/application.yml`** рядом с jar (в контейнере — `/app/config/application.yml`). Spring Boot подхватывает его сам, флаги запуска не нужны. Шаблон со всеми настройками и пояснениями: `config/application.yml.example`.
3. **Переменные окружения** — перекрывают файл. Имя получается из имени настройки: верхний регистр, точки и дефисы в подчёркивания (`connector.edo.base-url` → `CONNECTOR_EDO_BASE_URL`).

Обязательный минимум — `base-url`, `login` и `password` и для `connector.edo`, и для `connector.e4`. Без любого из них коннектор не стартует и пишет в лог, какой именно настройки не хватает, вместо того чтобы падать позже на первом запросе.

Пароли лучше передавать переменными окружения, а не файлом. `config/application.yml` добавлен в `.gitignore`, чтобы реальные креды не попали в репозиторий.

## Запуск

Локально:

```bash
cp config/application.yml.example config/application.yml   # и подставить свои значения
mvn spring-boot:run
```

В Docker — каталог с конфигом монтируется снаружи, пароли отдельно:

```bash
docker run -d --name efros-do-connector -p 8080:8080 \
  -v /opt/efros-do-connector/config:/app/config \
  -e CONNECTOR_EDO_PASSWORD='...' \
  -e CONNECTOR_E4_PASSWORD='...' \
  efros-do-connector
```

## Что уже подтверждено на реальном стенде

- Путь логина EDO: `POST /api/identity/Auth/LoginByPassword`, тело `{"userName": "...", "password": "..."}` (не `login`).
- Ответ логина — токен вложен в `token.accessToken`/`token.refreshToken`/`token.expires`.
- Выгрузка через `GetListSoHierarchy` — POST с пустым телом `{}`, ответ — дерево через `children`. Выбран вместо `GetFlattenSoHierarchy` ради совместимости с EDO 2.13 у заказчика; на стенде оба метода дают одинаковые 1888 записей. Подробности — `CLAUDE.md`.
- `type`: `"Group"` — папка иерархии, `"SecurityObject"` — объект защиты.
- `host` объекта защиты бывает в трёх местах: `host`, `ciFeatures[].host`, `acsFeatures[].host`.
- `refreshToken` — `POST /api/identity/Auth/refreshToken/{refreshToken}`, подтверждено Swagger.
- Стенд EDO — самоподписанный сертификат, EDO-клиент настроен доверять ему явно.
- Контракт ТМ: путь и тело `POST /api/integration/launch` фиксированы документацией.
- Контракт импорта в e4 (согласован с тимлидом): `guid`/`srcs.guid`/`srcs.idAdjSys` — все три равны GUID объекта в EDO; `host` раскладывается на `ipv4` (если это IP) или `hostName` (иначе); флаги `Да`/`Нет` — константы; передавать можно массивом.
- Авторизация адаптера e4: `POST /api/getToken` с телом `{"username", "password"}` (ключ с маленькой `n`), токен из `access_token` уходит в `Authorization: Bearer`. Подтверждено на стенде: импорт ответил `200 "Импорт завершен успешно"`.

## TODO (согласовать с тимлидом)

- Маппинг адреса: несколько `acsFeatures` с разными `host` (сейчас берём первый непустой), IPv6 (уйдёт в `hostName`), объект вообще без `host`.
- Есть ли `GetListSoHierarchy` в EDO 2.13.42 у заказчика и та ли у него форма ответа — у себя проверить нечем.
- Параметры регистрации в ТМ (`uid`, `cronString`, содержимое `currentConfig`, `adjacentSystemType`) — см. открытые вопросы в `registration/README.md` и `CLAUDE.md`.
