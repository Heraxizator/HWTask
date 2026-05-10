package org.example.hwtaskbackend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "HWTask API",
                version = "v1",
                description = "Task tracker API. All timestamps are in UTC (RFC 3339)."
        )
)
public class OpenApiConfiguration {
}
