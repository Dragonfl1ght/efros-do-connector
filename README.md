# efros-do-connector

Коннектор Efros Defense Operations → САОБ (через адаптер e4).

**Флоу:** ТМ (Менеджер задач) дёргает `POST /api/integration/launch` (путь и формат тела фиксированы документацией ТМ, раздел 1.5.1 — свой путь придумать нельзя) → логинимся в EDO (`POST /api/identity/Auth/LoginByPassword`) → `POST /api/v1/SecurityObject/GetFlattenSoHierarchy` с пустым телом фильтра → фильтр по `type == "SecurityObject"` → маппинг полей → POST в адаптер e4. Синхронно, в одном потоке, без очередей и без стейта.

Регистрация сервиса в ТМ — **разовая ручная операция**, не код (см. `registration/`). Уведомления об этапах/статусах ТМ (`running` → `free`) сознательно не отправляются — см. `CLAUDE.md`, раздел "Что мы сознательно НЕ делаем".

## Структура (по слоям)

```
controller/IntegrationController.java  - единственный входящий эндпоинт POST /api/integration/launch, только HTTP-обвязка
service/DeviceSyncService.java         - оркестрация одного прогона: login -> fetch -> map -> send
mapper/DeviceMapper.java               - фильтр объектов защиты + маппинг EdoSecurityObject -> DeviceImport
mapper/HostAddress.java                - разбор host на ipv4/hostName (регекс IPv4)
client/EdoClient.java                  - HTTP-вызов EDO: getFlattenSoHierarchy, повтор при 401
client/EdoTokenProvider.java           - логин/refresh и кеш токена EDO
client/EdoApiPaths.java                - пути API EDO одной кучей
client/E4Client.java                   - HTTP-вызов адаптера e4: importDevices
dto/tm/                                - LaunchRequestDto, LaunchResponseDto (контракт ТМ)
dto/edo/                               - EdoLoginRequest, EdoLoginResponse, EdoSecurityObject
dto/e4/DeviceImport.java               - тело импорта ТС в e4, собирается через DeviceImport.of()
dto/e4/E4ImportConstants.java          - фиксированные значения контракта e4 (Да/Нет, имя источника)
dto/RunResultDto.java                  - внутренний итог прогона (для логов DeviceSyncService)
exception/GlobalExceptionHandler.java  - единая обработка ошибок EDO/e4 -> 502 + сообщение
config/AppProperties.java              - URL/креды EDO, URL/креды e4, таймауты
config/HttpClientsConfig.java          - RestClient beans (edoRestClient, e4RestClient)
config/TrustAllRequestFactory.java     - доверие самоподписанным сертификатам стендов
registration/                          - разовая ручная регистрация в ТМ (regconn.json, register.sh, README.md)
```

Контроллер не содержит бизнес-логики — только делегирует в `DeviceSyncService`. Фильтрация и маппинг вынесены в `DeviceMapper`, поэтому тестируются юнит-тестом без поднятия Spring-контекста (`DeviceMapperTest`). Имена полей в теле импорта e4 зафиксированы отдельным тестом (`DeviceImportJsonTest`) — e4 сверяет их строкой.

Комментариев в коде нет — это решение команды. Смысл несут имена классов, методов и тестов, а всё, что нужно знать про контракты и договорённости, собрано в `CLAUDE.md`. Открытые вопросы там же, а не в `// TODO`.

## Запуск

```bash
mvn spring-boot:run
```

## Что уже подтверждено на реальном стенде

- Путь логина EDO: `POST /api/identity/Auth/LoginByPassword`, тело `{"userName": "...", "password": "..."}` (не `login`).
- Ответ логина — токен вложен в `token.accessToken`/`token.refreshToken`/`token.expires`.
- `GetFlattenSoHierarchy` — POST с пустым телом `{}` (не GET, без обязательных фильтров).
- `type`: `"Group"` — папка иерархии, `"SecurityObject"` — объект защиты.
- `host` объекта защиты бывает в трёх местах: `host`, `ciFeature.host`, `acsFeatures[].host`.
- Стенд EDO — самоподписанный сертификат, EDO-клиент настроен доверять ему явно.
- Контракт ТМ: путь и тело `POST /api/integration/launch` фиксированы документацией.
- Контракт импорта в e4 (согласован с тимлидом): `guid`/`srcs.guid`/`srcs.idAdjSys` — все три равны GUID объекта в EDO; `host` раскладывается на `ipv4` (если это IP) или `hostName` (иначе); флаги `Да`/`Нет` — константы; передавать можно массивом.

## TODO (согласовать с тимлидом)

- Авторизация адаптера e4: URL (`https://10.10.18.174/api/v1/import/Hardware`), состав полей и batch согласованы, но на Basic стенд отвечает `401 www-authenticate: Bearer` — нужен источник JWT-токена.
- Маппинг адреса: несколько `acsFeatures` с разными `host` (сейчас берём первый непустой), IPv6 (уйдёт в `hostName`), объект вообще без `host`.
- Пагинация в `GetFlattenSoHierarchy` — есть или нет (в задаче: "получить максимальное количество ОЗ").
- Метод `refreshToken` — предположительно `POST /api/identity/Auth/refreshToken/{refreshToken}`, не проверен вручную через Swagger (сверено только по описанию пользователя).
- Параметры регистрации в ТМ (`uid`, `cronString`, содержимое `currentConfig`, `adjacentSystemType`) — см. открытые вопросы в `registration/README.md` и `CLAUDE.md`.
