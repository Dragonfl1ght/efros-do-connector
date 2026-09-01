#!/usr/bin/env bash
#
# Разовая ручная регистрация коннектора Efros DO в Менеджере задач (ТМ).
# Сервис НЕ регистрируется сам при старте — это сознательное решение,
# см. CLAUDE.md, раздел "Что мы сознательно НЕ делаем".
#
#   ./register.sh                                  # регистрация
#   ./register.sh list                             # список зарегистрированных сервисов
#   ./register.sh status                           # состояние нашего сервиса
#   ./register.sh delete                           # удалить регистрацию
#
# Адрес ТМ можно переопределить: TM=http://host:port ./register.sh

set -euo pipefail

TM="${TM:-https://10.10.18.175:8443}"
UID_SERVICE="${UID_SERVICE:-efrosdo_uko}"
JSON="$(dirname "$0")/regconn.json"

# -k: ТМ на этом стенде поднят по HTTPS с самоподписанным сертификатом (как и EDO).
CURL_INSECURE="-k"

cmd="${1:-register}"

case "$cmd" in
  register)
    echo "Регистрация $UID_SERVICE в ТМ ($TM)..."
    curl $CURL_INSECURE -i -X POST "$TM/api/manage/regconn" \
      -H "Content-Type: application/json" \
      -d @"$JSON"
    ;;

  list)
    curl $CURL_INSECURE -s -X POST "$TM/api/performanceinfo/getlistconn" \
      -H "Content-Type: application/json" \
      -d '{"limit": "50"}'
    ;;

  status)
    curl $CURL_INSECURE -s -X POST "$TM/api/performanceinfo/getstatus" \
      -H "Content-Type: application/json" \
      -d "{\"serviceUId\": \"$UID_SERVICE\"}"
    ;;

  delete)
    read -rp "Удалить регистрацию $UID_SERVICE? [y/N] " answer
    [[ "$answer" == "y" ]] || exit 0
    curl $CURL_INSECURE -i -X POST "$TM/api/manage/deleteconnector" \
      -H "Content-Type: application/json" \
      -d "{\"serviceUId\": \"$UID_SERVICE\"}"
    ;;

  *)
    echo "Неизвестная команда: $cmd" >&2
    echo "Доступно: register | list | status | delete" >&2
    exit 1
    ;;
esac
