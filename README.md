# HWTask-Backend

Spring Boot API for the HWTask project: JPA, Flyway, OpenAPI, Spring Modulith.

## Requirements

- **JDK 25** (or the version in `pom.xml` `java.version`) for local runs
- **Docker** and **Docker Compose** for the full stack (PostgreSQL + API)

## Full stack (Docker)

Secrets and DB credentials are **not** stored in `compose.yaml`. Copy the example env file and set a strong password:

```bash
cp .env.example .env
# Edit .env — at minimum set POSTGRES_PASSWORD
```

From the project root:

```bash
docker compose up --build
```

- API: [http://localhost:8080](http://localhost:8080)
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- Health: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

The API image uses profile `docker` ([`application-docker.yml`](src/main/resources/application-docker.yml)): Docker Compose support inside the app is disabled so it does not clash with the Compose-managed PostgreSQL service.

## Postgres only + API locally

Use Compose only for the database, run the app from the IDE or Maven (same `.env` as above):

```bash
docker compose up postgres
```

Use the same `POSTGRES_DB`, `POSTGRES_USER`, and `POSTGRES_PASSWORD` as in `.env`. JDBC from the host machine is typically `jdbc:postgresql://localhost:<POSTGRES_PORT>/<POSTGRES_DB>` (default port `5432` if you did not set `POSTGRES_PORT`).

## Configuration

| Profile | Notes |
|--------|--------|
| `dev` | Default; optional Docker Compose for Postgres when available |
| `prod` | Requires `SPRING_DATASOURCE_*` and optional `MANAGEMENT_PORT` |
| `docker` | Used in the API container: Compose disabled, datasource from env / defaults |

## Build

```bash
./mvnw -DskipTests package
```

On Windows PowerShell or CMD use `.\mvnw.cmd -DskipTests package` instead.

## License

Project license as defined by the repository owner.
