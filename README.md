# HWTask

Монорепозиторий: **`api/`** — REST API на Spring Boot (**Java 25**), **`ui/`** — SPA (**React + TypeScript + Vite**) с интерфейсом задач. Данные в **PostgreSQL**, схема через **Flyway**, модульная структура — **Spring Modulith**, описание HTTP API — **OpenAPI / Swagger UI**.

## Структура

| Каталог | Содержимое |
|---------|------------|
| [`api/`](api/) | Maven (`pom.xml`, `src/`, `mvnw`), Dockerfile API |
| [`ui/`](ui/) | Фронтенд (`package.json`, `src/`), Dockerfile nginx + статика |

Корень: [`compose.yaml`](compose.yaml), [`.env.example`](.env.example).

## Стек

| Компонент | Технология |
|-----------|------------|
| API | Java 25, Spring Boot 4, Spring MVC, Jakarta Validation |
| UI | React 19, TypeScript, Vite, TanStack Query |
| Данные | Spring Data JPA, PostgreSQL, Flyway |
| Документация API | springdoc-openapi |
| Тесты API | JUnit 5, Mockito, Testcontainers (PostgreSQL), MockMvc |

## Требования

- **JDK 25** для локальной сборки API (см. [`api/pom.xml`](api/pom.xml), свойство `java.version`)
- **Node.js 22+** и **npm** — для разработки UI
- **Docker** и **Docker Compose** — полный стек и интеграционные тесты (Testcontainers)

## Быстрый старт (Docker: Postgres + API + UI)

Секреты не хранятся в `compose.yaml`: скопируйте шаблон и задайте пароль БД.

```bash
cp .env.example .env
# Отредактируйте .env — задайте POSTGRES_PASSWORD и при необходимости порты
docker compose up --build
```

| Сервис | URL (порты по умолчанию из `.env`) |
|--------|--------------------------------------|
| **Веб-интерфейс** | `http://localhost:${UI_PORT:-3000}/` |
| REST API (напрямую) | `http://localhost:${API_PORT:-8080}/api/v1/...` |
| Swagger UI | `http://localhost:${API_PORT:-8080}/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:${API_PORT:-8080}/v3/api-docs` |
| Health (API) | `http://localhost:${API_PORT:-8080}/actuator/health` |

Через порт **UI** запросы к `/api/...` уходят на контейнер `api` (nginx прокси); отдельный CORS для прода не нужен.

Профиль **`docker`** ([`api/src/main/resources/application-docker.yml`](api/src/main/resources/application-docker.yml)): встроенный Spring Docker Compose отключён, чтобы приложение в контейнере не конфликтовало с уже поднятым Compose.

## Разработка UI локально

Из каталога [`ui/`](ui/):

```bash
cd ui
npm ci
npm run dev
```

Vite по умолчанию слушает порт **5173** и проксирует `/api` на `http://127.0.0.1:8080`. Запустите API отдельно (профиль **`dev`**, CORS для `http://localhost:5173` задан в [`DevCorsConfiguration`](api/src/main/java/org/example/hwtaskbackend/config/DevCorsConfiguration.java)). При другом адресе API задайте переменную `VITE_API_PROXY_TARGET` или измените [`ui/vite.config.ts`](ui/vite.config.ts).

## Только PostgreSQL в Docker, API локально

```bash
docker compose up postgres
```

Запуск API из **корня репозитория** (важно для `compose.yaml` в профиле `dev`, см. [`api/src/main/resources/application-dev.yml`](api/src/main/resources/application-dev.yml)):

```bash
# Windows PowerShell
.\api\mvnw.cmd -f api\pom.xml spring-boot:run
```

JDBC: `jdbc:postgresql://localhost:<POSTGRES_PORT>/<POSTGRES_DB>` — те же `POSTGRES_*`, что в `.env`.

## Переменные окружения (`.env`)

Используются при `docker compose up`. Шаблон — [`.env.example`](.env.example).

| Переменная | Назначение |
|------------|------------|
| `POSTGRES_DB` | Имя базы |
| `POSTGRES_USER` | Пользователь БД |
| `POSTGRES_PASSWORD` | Пароль (смените с примера) |
| `POSTGRES_PORT` | Проброс Postgres на хост (по умолчанию `5432`) |
| `API_PORT` | Проброс REST/Swagger (по умолчанию `8080`) |
| `UI_PORT` | Проброс веб-интерфейса (по умолчанию `3000`) |

Файл `.env` не коммитится ([`.gitignore`](.gitignore)).

## Профили Spring

| Профиль | Когда использовать |
|---------|-------------------|
| `dev` | По умолчанию в IDE; CORS для Vite; Spring Docker Compose может поднять Postgres из корневого `compose.yaml` (рабочий каталог процесса — **корень репозитория**) |
| `prod` | Прод: `SPRING_DATASOURCE_*`, при необходимости `MANAGEMENT_PORT` ([`api/src/main/resources/application-prod.yml`](api/src/main/resources/application-prod.yml)) |
| `docker` | Контейнер API: источник данных из переменных Compose |

## Сборка и тесты

Сборка JAR API:

```bash
cd api
./mvnw -DskipTests package
```

Windows: `.\mvnw.cmd -DskipTests package` из каталога `api`.

Сборка UI:

```bash
cd ui
npm ci
npm run build
```

Тесты API (`Testcontainers` требует **Docker**):

```bash
cd api
./mvnw verify
```

CI (GitHub Actions): job **`api`** — `mvn verify` в `api/`, job **`ui`** — `npm ci && npm run lint && npm run build` в `ui/`.

## Лицензия

Определяется владельцем репозитория.
