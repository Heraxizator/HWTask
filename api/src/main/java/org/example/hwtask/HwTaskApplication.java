package org.example.hwtask;

import org.example.hwtask.security.AttachmentStorageProperties;
import org.example.hwtask.security.JwtProperties;
import org.example.hwtask.identity.service.AuthCookieProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({JwtProperties.class, AttachmentStorageProperties.class, AuthCookieProperties.class})
public class HwTaskApplication {

    public static void main(String[] args) {
        SpringApplication.run(HwTaskApplication.class, args);
    }
}
