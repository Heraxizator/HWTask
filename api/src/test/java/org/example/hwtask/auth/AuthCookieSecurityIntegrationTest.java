package org.example.hwtask.auth;

import org.example.hwtask.support.PostgresTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockCookie;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "hwtask.security.relaxed-authorization=false",
        "hwtask.security.allow-bearer-header=false",
        "hwtask.auth.cookie.access-name=hwtask_access",
        "hwtask.auth.cookie.refresh-name=hwtask_refresh",
        "hwtask.auth.cookie.access-ttl-seconds=900",
        "hwtask.auth.cookie.refresh-ttl-seconds=2592000",
        "hwtask.auth.cookie.secure=true",
        "hwtask.auth.cookie.same-site=Strict",
        "hwtask.auth.cookie.domain="
})
class AuthCookieSecurityIntegrationTest {

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        PostgresTestSupport.registerDatasource(registry);
    }

    @Autowired
    MockMvc mockMvc;

    @Test
    void loginSetsHttpOnlySecureSameSiteCookies() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "cookie-" + suffix + "@test.hwtask.local";

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"secretpass","displayName":"Тест"}
                                """.formatted(email)))
                .andExpect(status().isCreated());

        MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"secretpass"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn();

        List<String> setCookies = login.getResponse().getHeaders("Set-Cookie");
        assertThat(setCookies).isNotEmpty();

        assertThat(setCookies.stream().anyMatch(v -> v.startsWith("hwtask_access="))).isTrue();
        assertThat(setCookies.stream().anyMatch(v -> v.startsWith("hwtask_refresh="))).isTrue();

        for (String c : setCookies) {
            if (c.startsWith("hwtask_access=") || c.startsWith("hwtask_refresh=")) {
                assertThat(c).contains("HttpOnly");
                assertThat(c).contains("SameSite=Strict");
                assertThat(c).contains("Secure");
            }
        }
    }

    @Test
    void bearerHeaderDoesNotAuthenticateWhenDisabled() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "bearer-" + suffix + "@test.hwtask.local";

        MvcResult reg = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"secretpass","displayName":"Тест"}
                                """.formatted(email)))
                .andExpect(status().isCreated())
                .andReturn();

        String access = reg.getResponse().getCookie("hwtask_access").getValue();
        assertThat(access).isNotBlank();

        // Header-only auth must be rejected in this test (allow-bearer-header=false).
        mockMvc.perform(get("/api/v1/me")
                        .header("Authorization", "Bearer " + access))
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isIn(401, 403));

        // Cookie auth must work.
        mockMvc.perform(get("/api/v1/me")
                        .cookie(new MockCookie("hwtask_access", access)))
                .andExpect(status().isOk());
    }
}

