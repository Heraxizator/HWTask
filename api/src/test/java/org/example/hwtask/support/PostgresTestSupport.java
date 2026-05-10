package org.example.hwtask.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Lazy-started Postgres for integration tests so JVM classes load even when Docker is unavailable;
 * tests that call {@link #registerDatasource} still require a working Docker environment.
 */
public final class PostgresTestSupport {

    private static PostgreSQLContainer<?> postgres;

    private PostgresTestSupport() {
    }

    private static synchronized PostgreSQLContainer<?> container() {
        if (postgres == null) {
            postgres = new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("hwtask")
                    .withUsername("hwtask")
                    .withPassword("hwtask");
            postgres.start();
        }
        return postgres;
    }

    public static void registerDatasource(DynamicPropertyRegistry registry) {
        PostgreSQLContainer<?> c = container();
        registry.add("spring.datasource.url", c::getJdbcUrl);
        registry.add("spring.datasource.username", c::getUsername);
        registry.add("spring.datasource.password", c::getPassword);
    }
}
