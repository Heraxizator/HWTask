package org.example.hwtask.workspace;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WorkspaceReportsCollaborationIntegrationTest {

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
    void listOrganizationsAndProjects() throws Exception {
        mockMvc.perform(get("/api/v1/organizations").with(authentication(demoAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").exists());

        String orgJson = mockMvc.perform(get("/api/v1/organizations").with(authentication(demoAuth)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String orgId = objectMapper.readTree(orgJson).get(0).get("id").asText();

        mockMvc.perform(get("/api/v1/organizations/" + orgId + "/projects").with(authentication(demoAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Основной проект"));
    }

    @Test
    void taskSummaryReport() throws Exception {
        mockMvc.perform(get("/api/v1/projects/" + projectId + "/reports/tasks-summary")
                        .with(authentication(demoAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").exists())
                .andExpect(jsonPath("$.todo").exists());
    }

    @Test
    void createTaskCommentAndActivity() throws Exception {
        String taskJson = mockMvc.perform(post("/api/v1/tasks")
                        .with(authentication(demoAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projectId":"%s","title":"Интеграция","description":null,"status":"TODO","priority":null,"coAssigneeIds":[],"observerIds":[],"tagIds":[]}
                                """.formatted(projectId)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String taskId = objectMapper.readTree(taskJson).get("id").asText();

        mockMvc.perform(post("/api/v1/tasks/" + taskId + "/comments")
                        .with(authentication(demoAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"body\":\"Тестовый комментарий\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body").value("Тестовый комментарий"));

        mockMvc.perform(get("/api/v1/tasks/" + taskId + "/comments").with(authentication(demoAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].body").value("Тестовый комментарий"));

        mockMvc.perform(get("/api/v1/tasks/" + taskId + "/activity").with(authentication(demoAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        assertThat(taskId).isNotBlank();
    }
}
