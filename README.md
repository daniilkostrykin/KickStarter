# KickStarter

Распределённый бэкенд краудфандинговой площадки на Java 21 и Spring Boot 4. Домен (проекты, награды, взносы) доступен по REST и GraphQL, изменения публикуются в RabbitMQ. Поверх событий работают аудит, gRPC-скоринг проектов и push-уведомления в браузер по WebSocket.

![Java](https://img.shields.io/badge/Java-21-informational)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.3-informational)
![Maven](https://img.shields.io/badge/build-Maven-informational)

## Архитектура

```text
HTTP-клиент
    │  REST  /api/projects  (JSON + HATEOAS)
    │  GraphQL /graphql     (DGS + GraphiQL)
    ▼
┌──────────────────────────┐        PostgreSQL 16 (JPA + Flyway)
│   kickstarter-rest :8080 │◀──────────┐
└───────────┬──────────────┘           │
            │ publish                  │ read/write
            ▼                          │
   RabbitMQ topic exchange             │
   "kickstarter.events"                │
      │        │         │             │
      │        │         └─────────────┴────────▶ q.notifications.all
      │        │                                      │
      │        └──▶ q.audit.events                    │ WebSocket
      │                    │                          ▼
      │                    ▼                   kickstarter-notification-service :8084
      │           kickstarter-audit-service :8081     → ws://.../ws/notifications
      │           (JWT Keycloak, GET /api/audit)
      │
      └──▶ q.enrichment.project-created
                    │
                    ▼  gRPC AnalyzeProject
           kickstarter-grpc-server :9090  →  project.enriched → exchange
```

## Быстрый старт

Создание проекта: логин, CSRF-токен, `POST /api/projects`.

```bash
# 1. CSRF-токен формы входа
LOGIN_CSRF=$(curl -s -c cookies.txt http://localhost:8080/login \
  | sed -n 's/.*name="_csrf"[^>]*value="\([^"]*\)".*/\1/p' | head -n1)

# 2. Логин с ролями BACKER, CREATOR
curl -s -b cookies.txt -c cookies.txt -X POST http://localhost:8080/login \
  -d "username=creator&password=creator&_csrf=$LOGIN_CSRF"

# 3. CSRF-токен для изменяющих запросов
API_CSRF=$(curl -s -b cookies.txt http://localhost:8080/csrf | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

# 4. Создание проекта
curl -s -b cookies.txt -X POST http://localhost:8080/api/projects \
  -H "Content-Type: application/json" \
  -H "X-XSRF-TOKEN: $API_CSRF" \
  -d '{
        "title": "Умный рюкзак с солнечной батареей",
        "description": "Рюкзак со встроенным powerbank для студентов",
        "goal": 500000.00,
        "deadline": "2026-12-31T23:59:59Z",
        "authorId": 1
      }'
```

Ответ `201 Created` (HATEOAS):

```json
{
  "id": 4,
  "title": "Умный рюкзак с солнечной батареей",
  "description": "Рюкзак со встроенным powerbank для студентов",
  "goal": 500000.00,
  "pledged": 0,
  "status": "DRAFT",
  "deadline": "2026-12-31T23:59:59Z",
  "authorId": 1,
  "_links": {
    "self":       { "href": "http://localhost:8080/api/projects/4" },
    "collection": { "href": "http://localhost:8080/api/projects?page=0&size=10" },
    "update":     { "href": "http://localhost:8080/api/projects/4" }
  }
}
```

## Ключевые решения

- **Доставка событий.** RabbitMQ topic exchange `kickstarter.events` плюс DLQ для упавших сообщений. Доставка at-least-once, поэтому consumer дедуплицирует события по `eventId`.
- **Безопасность.** Гибридная модель. Пользователи — форма и серверные сессии с CSRF-защитой (`CookieCsrfTokenRepository`, токен в заголовке `X-XSRF-TOKEN`). Межсервисное взаимодействие — stateless OAuth 2.0: Keycloak Client Credentials и JWT в заголовке `Authorization`.
- **Аналитика и скоринг.** Вынесены в отдельный сервис на gRPC (HTTP/2, Protobuf), чтобы изолировать ресурсоёмкие расчёты от API-слоя.
- **Уведомления.** Push-рассылка доменных событий в браузер через нативный Spring WebSocket handler на `/ws/notifications`.

## API

### REST: проекты (`/api/projects`)

| Метод | Путь | Роль | Тело запроса | Ответ |
|---|---|---|---|---|
| `POST` | `/api/projects` | `CREATOR`, `ADMIN` | `ProjectRequest` | `201`, проект + `_links` |
| `GET` | `/api/projects?page=0&size=10` | `BACKER`, `CREATOR`, `ADMIN` | — | `200`, `PagedModel` |
| `GET` | `/api/projects/{id}` | `BACKER`, `CREATOR`, `ADMIN` | — | `200`, проект + `_links` |
| `PATCH` | `/api/projects/{id}` | `CREATOR`, `ADMIN` | `PatchProjectRequest` | `200`, проект + `_links` |
| `DELETE` | `/api/projects/{id}` | `ADMIN` | — | `204` |

`ProjectRequest`:

| Поле | Тип | Ограничения |
|---|---|---|
| `title` | string | обязательно, 5–100 символов, уникально |
| `description` | string | обязательно |
| `goal` | decimal | обязательно, > 0 |
| `deadline` | дата-время | обязательно, в будущем |
| `authorId` | long | обязательно, пользователь должен существовать |

`PatchProjectRequest`: опциональные `title`, `description`, `status`. Статус принимает одно из значений `DRAFT`, `ACTIVE`, `SUCCESSFUL`, `FAILED`.

### REST: награды (`/api/rewards`)

| Метод | Путь | Роль | Тело запроса | Ответ |
|---|---|---|---|---|
| `POST` | `/api/rewards` | `CREATOR`, `ADMIN` | `RewardRequest` | `201` |
| `GET` | `/api/rewards?page=0&size=10` | любая авторизованная | — | `200`, `PagedModel` |
| `GET` | `/api/rewards/{id}` | любая авторизованная | — | `200` |
| `PATCH` | `/api/rewards/{id}` | `CREATOR`, `ADMIN` | `PatchRewardRequest` | `200` |

`RewardRequest`: `title` (обязательно), `description` (5–100 символов), `minPrice` (> 0), `projectId` (обязательно).

### REST: взносы (`/api/pledges`)

| Метод | Путь | Роль | Тело запроса | Ответ |
|---|---|---|---|---|
| `POST` | `/api/pledges` | любая авторизованная | `PledgeRequest` | `201` |
| `GET` | `/api/pledges?page=0&size=10` | любая авторизованная | — | `200`, `PagedModel` |
| `GET` | `/api/pledges/{id}` | любая авторизованная | — | `200` |

`PledgeRequest`: `projectId`, `rewardId`, `pledge` (> 0), `userId`.

При создании взноса проверяется условие `pledge >= minPrice` награды. Статус выставляется в `AUTHORIZED`, а поле `pledged` проекта увеличивается на сумму взноса.

### REST: пользователи (`/api/users`)

| Метод | Путь | Роль | Тело запроса | Ответ |
|---|---|---|---|---|
| `POST` | `/api/users` | любая авторизованная | `UserRequest` | `201` |
| `GET` | `/api/users?page=0&size=10` | любая авторизованная | — | `200`, `PagedModel` |
| `GET` | `/api/users/{id}` | любая авторизованная | — | `200` |

`UserRequest`: `username` (3–50 символов, уникально), `email` (валидный, уникальный).

### REST: служебные маршруты

| Метод | Путь | Описание |
|---|---|---|
| `GET` | `/login` | HTML-форма входа |
| `POST` | `/login` | Вход по `username`, `password`, `_csrf` |
| `POST` | `/logout` | Выход, удаляет `JSESSIONID` и `XSRF-TOKEN` |
| `GET` | `/csrf` | Текущий CSRF-токен: `headerName`, `parameterName`, `token` |

### Коды ошибок REST

| Код | Когда возникает |
|---|---|
| `400` | Ошибка валидации полей или бизнес-правило (взнос меньше `minPrice`) |
| `401` | Нет аутентификации (для Bearer-эндпоинтов) |
| `403` | Недостаточно прав по роли либо отсутствует/неверен CSRF-токен |
| `404` | Ресурс не найден |
| `409` | Конфликт уникальности: повторяющиеся `title`, `username` или `email` |

### GraphQL

Эндпоинт `POST /graphql`, интерактивная IDE — `GET /graphiql`.

Запросы:

| Поле | Аргументы | Результат |
|---|---|---|
| `project(id)` | `id: ID!` | `Project` |
| `projects(page, size)` | параметры страницы | `ProjectConnection!` |
| `reward(id)` | `id: ID!` | `Reward` |
| `rewards(page, size)` | параметры страницы | `RewardConnection!` |
| `pledge(id)` | `id: ID!` | `Pledge` |
| `pledges(page, size)` | параметры страницы | `PledgeConnection!` |
| `user(id)` | `id: ID!` | `User` |

Мутации:

| Поле | Входные данные |
|---|---|
| `createProject` | `CreateProjectInput!` |
| `updateProject` | `id: ID!`, `PatchProjectInput!` |
| `createReward` | `CreateRewardInput!` |
| `makePledge` | `CreatePledgeInput!` |
| `registerUser` | `CreateUserInput!` |

Особенности:

- Вложенные связи: `Project.rewards`, `Project.author`, `Pledge.sponsor`, `User.backedProjects`.
- Каждое подключение возвращает `content`, `totalElements` и `pageInfo` (`pageNumber`, `pageSize`, `totalPages`, `last`).
- Защита от злоупотреблений: максимальная глубина запроса — 20, максимальная сложность — 200.
- Ошибки приходят с HTTP `200` в массиве `errors` (классификация: not found, conflict, internal).

### Журнал аудита (`kickstarter-audit-service`, :8081)

| Метод | Путь | Роль | Ответ |
|---|---|---|---|
| `GET` | `/api/audit?limit=100` | `SERVICE` | `totalEntries`, `showing`, `entries` |
| `GET` | `/api/audit/admin/info` | `OPERATOR` | `service`, `status` |

Требуется Bearer-токен с issuer `http://localhost:8085/realms/kickstarter` и audience `audit-service`. Роли `service` и `operator` определены в realm `kickstarter`.

Получить токен сервиса и прочитать журнал:

```bash
TOKEN=$(curl -s -X POST http://localhost:8085/realms/kickstarter/protocol/openid-connect/token \
  -d "grant_type=client_credentials" \
  -d "client_id=kickstarter-service-client" \
  -d "client_secret=service-secret" | grep -o '"access_token":"[^"]*"' | cut -d'"' -f4)

curl -s "http://localhost:8081/api/audit?limit=10" -H "Authorization: Bearer $TOKEN"
```

Keycloak-клиенты, предустановленные в realm:

| client_id | secret | audience | Роль |
|---|---|---|---|
| `kickstarter-service-client` | `service-secret` | `audit-service` | `service` |
| `kickstarter-ops-client` | `ops-secret` | `audit-service` | `operator` |
| `kickstarter-other-client` | `other-secret` | `other-api` | — (для проверки отказа по audience) |

### События

| Routing key | Источник | Потребители |
|---|---|---|
| `project.created` | `kickstarter-rest` | audit, notification, enrichment |
| `project.updated` | `kickstarter-rest` | audit, notification |
| `project.deleted` | `kickstarter-rest` | audit, notification |
| `pledge.created` | `kickstarter-rest` | audit, notification |
| `reward.created` | `kickstarter-rest` | audit, notification |
| `user.created` | `kickstarter-rest` | audit, notification |
| `project.enriched` | `grpc-enrichment-client` | audit, notification |

Топология обмена:

| Сущность | Имя | Назначение |
|---|---|---|
| Topic exchange | `kickstarter.events` | Публикация доменных событий |
| Очередь | `q.audit.events` (binding `#`) | Все события для аудита |
| Очередь | `q.notifications.all` (binding `#`) | Все события для уведомлений |
| Очередь | `q.enrichment.project-created` (binding `project.created`) | Только создание проектов |
| Dead Letter Exchange | `kickstarter.events.dlx` | Приём необработанных сообщений |
| DLQ | `*.dlq` | По одной на каждую очередь |

Формат сообщения:

```json
{
  "metadata": {
    "eventId": "3f1c9b62-9b1a-4e0e-8c1a-1f2e3d4c5b6a",
    "timestamp": "2026-03-01T12:00:00Z",
    "source": "kickstarter-rest",
    "eventType": "project.created"
  },
  "payload": {
    "projectId": 4,
    "title": "Умный рюкзак с солнечной батареей",
    "goal": 500000.00,
    "deadline": "2026-12-31T23:59:59Z",
    "authorId": 1
  }
}
```

### gRPC: `ProjectAnalytics.AnalyzeProject`

Unary RPC на порту `9090`. Контракт описан в [project_analytics.proto](kickstarter-grpc-contract/src/main/proto/project_analytics.proto).

Запрос `AnalyzeProjectRequest`:

| Поле | Тип | Описание |
|---|---|---|
| `project_id` | int64 | Идентификатор проекта |
| `goal` | double | Целевая сумма |
| `days_active` | int32 | Длительность кампании в днях |
| `description_length` | int32 | Длина описания в символах |

Ответ `ProjectAnalysisResponse`:

| Поле | Тип | Описание |
|---|---|---|
| `project_id` | int64 | Идентификатор проекта |
| `success_probability` | double | Шанс успеха в процентах |
| `hype_level` | string | `LOW`, `MEDIUM`, `HIGH` |
| `estimated_backers` | int32 | Оценка числа спонсоров из расчёта 1500 на взнос |

Базовая вероятность 50% корректируется требуемой скоростью сбора (`goal / days`) и длиной описания, результат ограничен диапазоном 5–95%. Ответ публикуется как событие `project.enriched`.

### WebSocket-уведомления

Подключение: `ws://localhost:8084/ws/notifications`. Готовая демонстрационная страница — `http://localhost:8084/`. Каждое событие приходит отдельным JSON-сообщением:

```json
{
  "type": "NOTIFICATION",
  "eventId": "3f1c9b62-9b1a-4e0e-8c1a-1f2e3d4c5b6a",
  "eventType": "project.enriched",
  "title": "Аналитика проекта",
  "description": "Проект ID 4 прошел скоринг! Шанс успеха: 70.0%",
  "icon": "trending-up",
  "level": "info",
  "source": "grpc-enrichment-client",
  "eventTimestamp": "2026-03-01T12:00:00Z",
  "receivedAt": "2026-03-01T12:00:00.123Z"
}
```

## Установка и запуск

Требования: JDK 21+, Docker, Git. Maven не нужен: используется wrapper `./mvnw`.

### 1. Клонирование и переменные окружения

```bash
git clone https://github.com/daniilkostrykin/KickStarter.git
cd KickStarter
cp .env.example .env      # Windows: copy .env.example .env
```

### 2. Инфраструктура

```bash
docker compose up -d
```

Поднимаются три контейнера:

| Контейнер | Порты | Назначение |
|---|---|---|
| `kickstarter-postgres` | `5432` | База `kickstarter_db` |
| `kickstarter-rabbitmq` | `5672`, `15672` | Брокер, консоль на `http://localhost:15672` |
| `keycloak` | `8085` | Realm `kickstarter` из `infra/keycloak/kickstarter-realm.json` |

### 3. Сборка

```bash
./mvnw clean install -DskipTests
```

Команда компилирует все восемь модулей, генерирует gRPC-классы из `.proto` и ставит контракты в локальный репозиторий Maven.

### 4. Запуск сервисов

Каждый сервис запускается в отдельном терминале. REST-сервис стартует последним.

```bash
./mvnw -pl kickstarter-grpc-server           spring-boot:run
./mvnw -pl kickstarter-audit-service         spring-boot:run
./mvnw -pl kickstarter-enrichment-client     spring-boot:run
./mvnw -pl kickstarter-notification-service  spring-boot:run
./mvnw -pl kickstarter-rest                  spring-boot:run
```

Те же шаги автоматизирует PowerShell-скрипт:

```powershell
./start.ps1
```

Полная остановка и очистка:

```powershell
./stop-all.ps1
```

### 5. Проверка

| Сервис | URL | Что показывает |
|---|---|---|
| REST API | `http://localhost:8080/api/projects` | Список проектов (требуется вход) |
| Swagger UI | `http://localhost:8080/swagger-ui/index.html` | OpenAPI-документация |
| GraphiQL | `http://localhost:8080/graphiql` | IDE для GraphQL |
| Аудит | `http://localhost:8081/actuator/health` | Состояние сервиса |
| Обогащение | `http://localhost:8082/actuator/health` | Состояние сервиса |
| gRPC-сервер | `http://localhost:8083/actuator/health` | Состояние сервиса, сам gRPC — порт `9090` |
| Уведомления | `http://localhost:8084/` | Демо-страница уведомлений |
| RabbitMQ | `http://localhost:15672` | Очереди, обмены, DLQ |
| Keycloak | `http://localhost:8085` | Realm `kickstarter` |

Сквозная проверка: войдите как `creator`, создайте проект (см. раздел «Быстрый старт»), откройте `http://localhost:8084/` и убедитесь, что пришли уведомления `project.created` и `project.enriched`.

### Тестовые пользователи

| Логин | Пароль | Роли |
|---|---|---|
| `backer` | `backer` | `BACKER` — только чтение |
| `creator` | `creator` | `BACKER`, `CREATOR` — создание и правка |
| `admin` | `admin` | `BACKER`, `CREATOR`, `ADMIN` — полный доступ, включая удаление |

### Настройка

`kickstarter-rest` читает переменные окружения с дефолтами:

| Переменная | По умолчанию |
|---|---|
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` | `localhost`, `5432`, `kickstarter_db`, `kickstarter_user`, `kickstarter_password` |
| `RABBITMQ_HOST`, `RABBITMQ_USER`, `RABBITMQ_PASSWORD` | `localhost`, `guest`, `guest` |
| `SESSION_COOKIE_SECURE` | `false` (ставьте `true` только при работе по HTTPS) |

`kickstarter-audit-service` дополнительно принимает `OIDC_ISSUER` (по умолчанию `http://localhost:8085/realms/kickstarter`).
