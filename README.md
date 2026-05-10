# HWTask

REST API для домена задач: создание, просмотр, постраничный список, обновление и удаление. Хранение в **PostgreSQL**, схема БД через **Flyway**, модульная структура — **Spring Modulith**, описание HTTP API — **OpenAPI / Swagger UI**.

## Стек

| Компонент | Технология |
|-----------|------------|
| Runtime | Java 25, Spring Boot 4 |
| Данные | Spring Data JPA, PostgreSQL, Flyway |
| HTTP | Spring MVC, Jakarta Validation |
| Документация API | springdoc-openapi |
| Тесты | JUnit 5, Mockito, Testcontainers (PostgreSQL), MockMvc |

## Требования

- **JDK 25** (или версия из [`pom.xml`](pom.xml), свойство `java.version`)
- **Docker** и **Docker Compose** — для полного стека и для интеграционных тестов с Testcontainers

## Быстрый старт

### Полный стек в Docker (PostgreSQL + API)

Секреты не хранятся в `compose.yaml`: скопируйте шаблон и задайте пароль БД.

```bash
cp .env.example .env
# Отредактируйте .env — обязательно задайте POSTGRES_PASSWORD
docker compose up --build
```

После запуска:

| Что | URL |
|-----|-----|
| API | [http://localhost:8080](http://localhost:8080) |
| Swagger UI | [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) |
| OpenAPI JSON | [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs) |
| Health | [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health) |

Профиль **`docker`** ([`application-docker.yml`](src/main/resources/application-docker.yml)): встроенный Spring Docker Compose отключён, чтобы приложение в контейнере не конфликтовало с уже поднятым Compose.

### Только PostgreSQL в Docker, приложение локально

```bash
docker compose up postgres
```

В IDE или Maven запустите приложение с JDBC на хост:  
`jdbc:postgresql://localhost:<POSTGRES_PORT>/<POSTGRES_DB>` — те же `POSTGRES_*`, что в `.env` (по умолчанию порт хоста `5432`, см. [`.env.example`](.env.example)).

## Переменные окружения (`.env`)

Используются при `docker compose up`. Пример имен переменных — в [`.env.example`](.env.example).

| Переменная | Назначение |
|------------|------------|
| `POSTGRES_DB` | Имя базы |
| `POSTGRES_USER` | Пользователь БД |
| `POSTGRES_PASSWORD` | Пароль (обязательно смените с примера) |
| `POSTGRES_PORT` | Проброс порта Postgres на хост (по умолчанию `5432`) |
| `API_PORT` | Проброс порта API на хост (по умолчанию `8080`) |

Файл `.env` в репозиторий не коммитится (см. `.gitignore`).

## Профили Spring

| Профиль | Когда использовать |
|---------|-------------------|
| `dev` | По умолчанию; при наличии Compose может подключаться dev-база через Spring Docker Compose |
| `prod` | Продакшен: задайте `SPRING_DATASOURCE_*`, при необходимости `MANAGEMENT_PORT` ([`application-prod.yml`](src/main/resources/application-prod.yml)) |
| `docker` | Контейнер API: источник данных из переменных окружения Compose |

## Сборка и тесты

Сборка JAR:

```bash
./mvnw -DskipTests package
```

В Windows: `.\mvnw.cmd -DskipTests package`.

Запуск тестов:

```bash
./mvnw test
```

Интеграционные тесты с БД используют **Testcontainers** и требуют **работающего Docker**.

## Лицензия

Определяется владельцем репозитория.
