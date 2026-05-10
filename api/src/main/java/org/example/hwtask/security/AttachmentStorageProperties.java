package org.example.hwtask.security;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "hwtask.storage")
@Validated
public record AttachmentStorageProperties(
        @NotBlank String attachmentsDir
) {
}
