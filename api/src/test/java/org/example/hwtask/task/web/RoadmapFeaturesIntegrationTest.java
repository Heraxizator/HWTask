package org.example.hwtask.task.web;

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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RoadmapFeaturesIntegrationTest {

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

    private UUID createTask(String title) throws Exception {
        String json = mockMvc.perform(post("/api/v1/tasks")
                        .with(authentication(demoAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projectId":"%s","title":"%s","description":null,"status":"TODO","priority":null,"coAssigneeIds":[],"observerIds":[],"tagIds":[]}
                                """.formatted(projectId, title)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return UUID.fromString(objectMapper.readTree(json).get("id").asText());
    }

    @Test
    void projectTagAssignAndFilterList() throws Exception {
        UUID taskId = createTask("Tag flow");

        String tagName = "it-" + UUID.randomUUID();
        String tagJson = mockMvc.perform(post("/api/v1/projects/" + projectId + "/tags")
                        .with(authentication(demoAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + tagName + "\"}"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        UUID tagId = UUID.fromString(objectMapper.readTree(tagJson).get("id").asText());

        mockMvc.perform(put("/api/v1/tasks/" + taskId + "/tags")
                        .with(authentication(demoAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tagIds\":[\"" + tagId + "\"]}"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/tasks/" + taskId).with(authentication(demoAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags[0].name").value(tagName));

        mockMvc.perform(get("/api/v1/tasks")
                        .param("projectId", projectId.toString())
                        .param("tagIds", tagId.toString())
                        .with(authentication(demoAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(taskId.toString()));
    }

    @Test
    void checklistCreateUpdateDelete() throws Exception {
        UUID taskId = createTask("Checklist flow");

        String created = mockMvc.perform(post("/api/v1/tasks/" + taskId + "/checklist-items")
                        .with(authentication(demoAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Step one\"}"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        UUID itemId = UUID.fromString(objectMapper.readTree(created).get("id").asText());

        mockMvc.perform(get("/api/v1/tasks/" + taskId + "/checklist-items").with(authentication(demoAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Step one"));

        mockMvc.perform(put("/api/v1/tasks/" + taskId + "/checklist-items/" + itemId)
                        .with(authentication(demoAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"done\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.done").value(true));

        mockMvc.perform(delete("/api/v1/tasks/" + taskId + "/checklist-items/" + itemId)
                        .with(authentication(demoAuth)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/tasks/" + taskId + "/checklist-items").with(authentication(demoAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void timeTrackingStartStopAndList() throws Exception {
        UUID taskId = createTask("Time flow");

        mockMvc.perform(post("/api/v1/tasks/" + taskId + "/time-entries/start")
                        .with(authentication(demoAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.endedAt").value(nullValue()));

        mockMvc.perform(post("/api/v1/me/time-entries/stop").with(authentication(demoAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.endedAt").exists());

        mockMvc.perform(get("/api/v1/tasks/" + taskId + "/time-entries").with(authentication(demoAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].taskId").value(taskId.toString()));
    }

    @Test
    void trashRestoreAndPermanentPurge() throws Exception {
        UUID taskRestore = createTask("Trash restore");
        UUID taskPurge = createTask("Trash purge");

        mockMvc.perform(delete("/api/v1/tasks/" + taskRestore).with(authentication(demoAuth)))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/v1/tasks/" + taskPurge).with(authentication(demoAuth)))
                .andExpect(status().isNoContent());

        String trashBody = mockMvc.perform(get("/api/v1/projects/" + projectId + "/deleted-tasks")
                        .with(authentication(demoAuth)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode trashContent = objectMapper.readTree(trashBody).path("content");
        assertThat(trashContent.isArray()).isTrue();
        assertThat(containsTaskId(trashContent, taskRestore)).isTrue();
        assertThat(containsTaskId(trashContent, taskPurge)).isTrue();

        mockMvc.perform(post("/api/v1/tasks/" + taskRestore + "/restore").with(authentication(demoAuth)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/tasks/" + taskRestore).with(authentication(demoAuth)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/tasks/" + taskPurge + "/permanent").with(authentication(demoAuth)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/tasks/" + taskPurge).with(authentication(demoAuth)))
                .andExpect(status().isNotFound());
    }

    private static boolean containsTaskId(JsonNode contentArray, UUID taskId) {
        String want = taskId.toString();
        for (JsonNode n : contentArray) {
            if (want.equals(n.path("id").asText())) {
                return true;
            }
        }
        return false;
    }

    @Test
    void notificationsListMarkReadAndTaskMute() throws Exception {
        UUID taskId = createTask("Notif flow");

        String notifPage = mockMvc.perform(get("/api/v1/me/notifications").with(authentication(demoAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode notifArr = objectMapper.readTree(notifPage).path("content");
        for (JsonNode n : notifArr) {
            if (!n.path("read").asBoolean(false)) {
                UUID nid = UUID.fromString(n.get("id").asText());
                mockMvc.perform(patch("/api/v1/me/notifications/" + nid + "/read").with(authentication(demoAuth)))
                        .andExpect(status().isNoContent());
                break;
            }
        }

        mockMvc.perform(get("/api/v1/me/notifications/unread-count").with(authentication(demoAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").exists());

        mockMvc.perform(patch("/api/v1/me/notifications/read-all").with(authentication(demoAuth)))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/v1/tasks/" + taskId + "/notifications/mute").with(authentication(demoAuth)))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/v1/tasks/" + taskId + "/notifications/mute").with(authentication(demoAuth)))
                .andExpect(status().isNoContent());
    }
}
