package org.example.hwtask.task.web;

import org.example.hwtask.config.GlobalExceptionHandler;
import org.example.hwtask.security.UserPrincipal;
import org.example.hwtask.task.dto.response.TaskResponse;
import org.example.hwtask.task.persistence.TaskPriority;
import org.example.hwtask.task.persistence.TaskStatus;
import org.example.hwtask.task.service.TaskNotFoundException;
import org.example.hwtask.task.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
@org.springframework.context.annotation.Import(GlobalExceptionHandler.class)
class TaskControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    TaskService taskService;

    private static Authentication demoAuth() {
        UserPrincipal principal = new UserPrincipal(UUID.randomUUID(), "u@test.local", "User");
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    private static TaskResponse sampleTask(UUID id, UUID projectId, OffsetDateTime t) {
        return new TaskResponse(
                id,
                projectId,
                null,
                null,
                UUID.randomUUID(),
                null,
                "Title",
                null,
                TaskStatus.TODO,
                TaskPriority.LOW,
                t,
                t,
                List.of(),
                List.of()
        );
    }

    @Test
    void createReturns201AndBody() throws Exception {
        UUID id = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        OffsetDateTime t = OffsetDateTime.ofInstant(java.time.Instant.parse("2025-01-01T00:00:00Z"), ZoneOffset.UTC);
        when(taskService.create(any(), any())).thenReturn(sampleTask(id, projectId, t));

        mockMvc.perform(post("/api/v1/tasks")
                        .with(authentication(demoAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projectId":"%s","title":"Title","description":null,"status":"TODO","priority":"LOW","coAssigneeIds":[],"observerIds":[],"tagIds":[]}
                                """.formatted(projectId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.title").value("Title"));
    }

    @Test
    void getNotFoundReturnsProblemDetail() throws Exception {
        UUID id = UUID.randomUUID();
        when(taskService.get(any(), eq(id))).thenThrow(new TaskNotFoundException(id));

        mockMvc.perform(get("/api/v1/tasks/" + id).with(authentication(demoAuth())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Not Found"));
    }

    @Test
    void listReturnsPage() throws Exception {
        UUID id = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        OffsetDateTime t = OffsetDateTime.ofInstant(java.time.Instant.parse("2025-01-01T00:00:00Z"), ZoneOffset.UTC);
        when(taskService.list(any(), eq(projectId), any(), any(), any())).thenReturn(
                new PageImpl<>(List.of(sampleTask(id, projectId, t)), PageRequest.of(0, 20), 1)
        );

        mockMvc.perform(get("/api/v1/tasks").param("projectId", projectId.toString()).with(authentication(demoAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Title"));
    }

    @Test
    void deleteReturns204() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(delete("/api/v1/tasks/" + id).with(authentication(demoAuth())))
                .andExpect(status().isNoContent());
        verify(taskService).delete(any(), eq(id));
    }

    @Test
    void createWithBlankTitleReturns400WithErrors() throws Exception {
        UUID projectId = UUID.randomUUID();
        mockMvc.perform(post("/api/v1/tasks")
                        .with(authentication(demoAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projectId":"%s","title":"","description":null,"status":"TODO","priority":null,"coAssigneeIds":[],"observerIds":[],"tagIds":[]}
                                """.formatted(projectId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists());
    }

    @Test
    void putUpdates() throws Exception {
        UUID id = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        OffsetDateTime t = OffsetDateTime.ofInstant(java.time.Instant.parse("2025-01-01T00:00:00Z"), ZoneOffset.UTC);
        when(taskService.update(any(), eq(id), any())).thenReturn(
                new TaskResponse(
                        id,
                        projectId,
                        null,
                        null,
                        UUID.randomUUID(),
                        null,
                        "New",
                        "d",
                        TaskStatus.IN_PROGRESS,
                        TaskPriority.HIGH,
                        t,
                        t,
                        List.of(),
                        List.of()
                )
        );

        mockMvc.perform(put("/api/v1/tasks/" + id)
                        .with(authentication(demoAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"New","description":"d","status":"IN_PROGRESS","priority":"HIGH","assigneeId":null,"dueAt":null,"coAssigneeIds":[],"observerIds":[]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }
}
