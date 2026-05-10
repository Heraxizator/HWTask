package org.example.hwtask.task.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.hwtask.config.RequestIdFilter;
import org.example.hwtask.identity.persistence.ProjectRepository;
import org.example.hwtask.identity.persistence.UserRepository;
import org.example.hwtask.security.UserPrincipal;
import org.example.hwtask.support.PostgresTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskIntegrationTest {

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        PostgresTestSupport.registerDatasource(registry);
    }

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProjectRepository projectRepository;

    private UsernamePasswordAuthenticationToken demoAuth;

    private UUID projectId;

    @BeforeEach
    void setUp() {
        var user = userRepository.findByEmailIgnoreCase("demo@hwtask.local").orElseThrow();
        UserPrincipal principal = new UserPrincipal(user.getId(), user.getEmail(), user.getDisplayName());
        demoAuth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        projectId = projectRepository.findAll().stream().findFirst().orElseThrow().getId();
    }

    @Test
    void requestIdEchoedWhenProvided() throws Exception {
        mockMvc.perform(get("/api/v1/tasks")
                        .param("projectId", projectId.toString())
                        .header(RequestIdFilter.REQUEST_ID_HEADER, "fixed-id")
                        .with(authentication(demoAuth)))
                .andExpect(status().isOk())
                .andExpect(header().string(RequestIdFilter.REQUEST_ID_HEADER, "fixed-id"));
    }

    @Test
    void listIgnoresInvalidSortParameterLikeSwaggerPlaceholder() throws Exception {
        mockMvc.perform(get("/api/v1/tasks")
                        .param("projectId", projectId.toString())
                        .queryParam("sort", "string")
                        .with(authentication(demoAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void createAndGetRoundTrip() throws Exception {
        String json = mockMvc.perform(post("/api/v1/tasks")
                        .with(authentication(demoAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projectId":"%s","title":"Hello","description":null,"status":"TODO","priority":null,"coAssigneeIds":[],"observerIds":[]}
                                """.formatted(projectId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Hello"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode node = objectMapper.readTree(json);
        String id = node.get("id").asText();

        mockMvc.perform(get("/api/v1/tasks/" + id).with(authentication(demoAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Hello"));

        assertThat(id).isNotBlank();
    }

    @Test
    void getMissingReturns404ProblemDetail() throws Exception {
        mockMvc.perform(get("/api/v1/tasks/" + UUID.randomUUID()).with(authentication(demoAuth)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Not Found"));
    }

    @Test
    void createWithBlankTitleReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/tasks")
                        .with(authentication(demoAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projectId":"%s","title":"","description":null,"status":"TODO","priority":null,"coAssigneeIds":[],"observerIds":[]}
                                """.formatted(projectId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists());
    }

    @Test
    void updateThenDeleteReturns404OnGet() throws Exception {
        String json = mockMvc.perform(post("/api/v1/tasks")
                        .with(authentication(demoAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projectId":"%s","title":"Original","description":null,"status":"TODO","priority":null,"coAssigneeIds":[],"observerIds":[]}
                                """.formatted(projectId)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = objectMapper.readTree(json).get("id").asText();

        mockMvc.perform(put("/api/v1/tasks/" + id)
                        .with(authentication(demoAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Updated","description":"x","status":"DONE","priority":"LOW","assigneeId":null,"dueAt":null,"coAssigneeIds":[],"observerIds":[]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"))
                .andExpect(jsonPath("$.status").value("DONE"));

        mockMvc.perform(delete("/api/v1/tasks/" + id).with(authentication(demoAuth)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/tasks/" + id).with(authentication(demoAuth)))
                .andExpect(status().isNotFound());
    }
}
