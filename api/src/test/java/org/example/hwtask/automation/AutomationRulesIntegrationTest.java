package org.example.hwtask.automation;

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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AutomationRulesIntegrationTest {

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
    void createListDeleteAutomationRule() throws Exception {
        String created = mockMvc.perform(post("/api/v1/projects/" + projectId + "/automation-rules")
                        .with(authentication(demoAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"triggerType":"ON_STATUS_CHANGE","actionType":"ADD_ACTIVITY_NOTE","enabled":true}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.triggerType").value("ON_STATUS_CHANGE"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String ruleId = objectMapper.readTree(created).get("id").asText();

        mockMvc.perform(get("/api/v1/projects/" + projectId + "/automation-rules")
                        .with(authentication(demoAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(ruleId));

        mockMvc.perform(delete("/api/v1/projects/" + projectId + "/automation-rules/" + ruleId)
                        .with(authentication(demoAuth)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/projects/" + projectId + "/automation-rules")
                        .with(authentication(demoAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}
