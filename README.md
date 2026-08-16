# efros-do-connector

Коннектор Efros Defense Operations → САОБ.

**Флоу:** ТМ дёргает `POST /api/run` → логинимся в EDO → GET `/api/v1/SecurityObject/GetFlattenSoHierarchy` → фильтр по `type` → маппинг полей → POST в адаптер e4. Синхронно, в одном потоке, без очередей и без стейта.

## Структура (по слоям)

```
controller/RunController.java        - единственный входящий эндпоинт POST /api/run, только HTTP-обвязка
service/DeviceSyncService.java       - оркестрация одного прогона: login -> fetch -> map -> send
mapper/DeviceMapper.java             - фильтр объектов защиты + маппинг EdoSecurityObject -> DeviceImport
client/EdoClient.java                - HTTP-вызовы EDO: login, getFlattenSoHierarchy
client/E4Client.java                 - HTTP-вызов адаптера e4: importDevices
dto/edo/                             - EdoLoginRequest, EdoLoginResponse, EdoSecurityObject
dto/e4/DeviceImport.java             - тело импорта ТС в e4
dto/RunResultDto.java                - тело ответа /api/run (кол-во полученных/отправленных)
exception/GlobalExceptionHandler.java - единая обработка ошибок EDO/e4 -> 502 + сообщение
config/AppProperties.java            - URL/креды EDO, URL e4, фильтр по type, таймауты
config/HttpClientsConfig.java        - RestClient beans с таймаутами
```

Контроллер не содержит бизнес-логики — только делегирует в `DeviceSyncService`. Фильтрация и маппинг вынесены в `DeviceMapper`, поэтому тестируются юнит-тестом без поднятия Spring-контекста (`DeviceMapperTest`).

## Запуск

```bash
mvn spring-boot:run
```

## TODO (согласовать с тимлидом)

- Точный контракт e4-адаптера (URL, формат тела, авторизация, что при повторной отправке того же id).
- Реальные имена полей в ответе EDO (свагер vs факт со стенда).
- Значения `type`, которые считаются "объектом защиты" — сейчас `connector.edo.security-object-types` пуст, фильтр по типу выключен, идут все объекты с непустым id.
- Есть ли пагинация в `GetFlattenSoHierarchy`.
- Нужна ли регистрация сервиса в ТМ и уведомления `condition` (в этом скелете не реализованы — исходим из того, что достаточно одного входящего эндпоинта).
