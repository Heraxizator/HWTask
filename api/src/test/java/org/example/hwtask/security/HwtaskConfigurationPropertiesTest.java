package org.example.hwtask.security;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class HwtaskConfigurationPropertiesTest {

    @Nested
    class JwtPropertiesBinding {

        private final ApplicationContextRunner runner = new ApplicationContextRunner()
                .withUserConfiguration(EnableJwtProperties.class)
                .withConfiguration(ValidationAutoConfiguration.class);

        @Test
        void bindsSecretAndExpirationFromProperties() {
            runner.withPropertyValues(
                            "hwtask.jwt.secret=test-secret-for-unit-tests-minimum-length-32chars!!",
                            "hwtask.jwt.expiration-ms=3600000")
                    .run(context -> {
                        assertThat(context).hasSingleBean(JwtProperties.class);
                        JwtProperties p = context.getBean(JwtProperties.class);
                        assertThat(p.secret()).startsWith("test-secret-for-unit-tests");
                        assertThat(p.expirationMs()).isEqualTo(3_600_000L);
                    });
        }

        @Test
        void rejectsBlankSecret() {
            runner.withPropertyValues(
                            "hwtask.jwt.secret=",
                            "hwtask.jwt.expiration-ms=3600000")
                    .run(context -> assertThat(context).hasFailed());
        }

        @Test
        void rejectsNonPositiveExpiration() {
            runner.withPropertyValues(
                            "hwtask.jwt.secret=test-secret-for-unit-tests-minimum-length-32chars!!",
                            "hwtask.jwt.expiration-ms=0")
                    .run(context -> assertThat(context).hasFailed());
        }
    }

    @Nested
    class AttachmentStoragePropertiesBinding {

        private final ApplicationContextRunner runner = new ApplicationContextRunner()
                .withUserConfiguration(EnableStorageProperties.class)
                .withConfiguration(ValidationAutoConfiguration.class);

        @Test
        void bindsAttachmentsDir() {
            runner.withPropertyValues("hwtask.storage.attachments-dir=/tmp/hwtask-unit-test-attachments")
                    .run(context -> {
                        assertThat(context).hasSingleBean(AttachmentStorageProperties.class);
                        assertThat(context.getBean(AttachmentStorageProperties.class).attachmentsDir())
                                .isEqualTo("/tmp/hwtask-unit-test-attachments");
                    });
        }

        @Test
        void rejectsBlankAttachmentsDir() {
            runner.withPropertyValues("hwtask.storage.attachments-dir=")
                    .run(context -> assertThat(context).hasFailed());
        }
    }

    @Configuration
    @EnableConfigurationProperties(JwtProperties.class)
    static class EnableJwtProperties {}

    @Configuration
    @EnableConfigurationProperties(AttachmentStorageProperties.class)
    static class EnableStorageProperties {}
}
