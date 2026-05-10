# HWTask

Монорепозиторий **HWTask**: **`api/`** — REST API на Spring Boot (**Java 25**), **`ui/`** — SPA (**React + TypeScript + Vite**). Данные в **PostgreSQL**, схема через **Flyway**, описание HTTP API — **OpenAPI / Swagger UI**.

Функциональность в духе Bitrix24-задач: **организации и проекты**, **участники и роли**, **JWT-аутентификация**, **задачи с исполнителем, сроком и подзадачами**, **соисполнители и наблюдатели**, **комментарии, вложения, лента активности**, **напоминания и простые правила автоматизации**, **сводка по задачам**.

В профилях **`dev`**, **`docker`** и **`test`** при пустой базе создаётся демо-пользователь **`demo@hwtask.local` / `demo`** и проект «Основной проект».

## Структура

| Каталог | Содержимое |
|---------|------------|
| [`api/`](api/) | Maven (`pom.xml`, `src/`, `mvnw`), Dockerfile API |
| [`ui/`](ui/) | Фронтенд (`package.json`, `src/`), Dockerfile nginx + статика |

Корень: [`compose.yaml`](compose.yaml), [`.env.example`](.env.example).

## Стек

| Компонент | Технология |
|-----------|------------|
| API | Java 25, Spring Boot 4, Spring MVC, Spring Security, JWT, Jakarta Validation |
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
# Обязательно задайте JWT_SECRET (openssl rand -base64 48) и пароль БД; без JWT_SECRET API не стартует.
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

Vite по умолчанию слушает порт **5173** и проксирует `/api` на `http://127.0.0.1:8080`. Запустите API отдельно (профиль **`dev`**, CORS для `http://localhost:5173` задан в [`DevCorsConfiguration`](api/src/main/java/org/example/hwtask/config/DevCorsConfiguration.java)). При другом адресе API задайте переменную `VITE_API_PROXY_TARGET` или измените [`ui/vite.config.ts`](ui/vite.config.ts).

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

## Переменные окружения и секреты

Шаблон — [`.env.example`](.env.example). Файл `.env` не коммитится ([`.gitignore`](.gitignore)).

**Где что задаётся**

- В **[`application.yml`](api/src/main/resources/application.yml)** секретов JWT **нет** — только общие нечувствительные настройки (например интервал напоминаний).
- Профиль **`dev`** ([`application-dev.yml`](api/src/main/resources/application-dev.yml)): локальный дефолт JWT только для разработки (`JWT_SECRET` можно переопределить через окружение).
- Профили **`docker`** и **`prod`**: `JWT_SECRET` и строка БД — **только из переменных окружения**, без дефолтов в YAML.

**Docker Compose:** перед `docker compose up` скопируйте `.env.example` → `.env` и задайте **`JWT_SECRET`** (длинная случайная строка) и **`POSTGRES_PASSWORD`**. Без `JWT_SECRET` сервис `api` не получит ключ подписи и не стартует.

| Переменная | Назначение |
|------------|------------|
| `POSTGRES_DB` | Имя базы |
| `POSTGRES_USER` | Пользователь БД |
| `POSTGRES_PASSWORD` | Пароль БД (**обязательно смените**) |
| `POSTGRES_PORT` | Проброс Postgres на хост (по умолчанию `5432`) |
| `API_PORT` | Проброс REST/Swagger (по умолчанию `8080`) |
| `UI_PORT` | Проброс веб-интерфейса (по умолчанию `3000`) |
| `JWT_SECRET` | Подпись JWT (**обязательно для Compose и prod**) |
| `JWT_EXPIRATION_MS` | Опционально, время жизни токена |
| `HWTASK_ATTACHMENTS_DIR` | Каталог вложений (prod/docker при необходимости) |

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
