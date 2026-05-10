package org.example.hwtaskbackend.task.web;

import org.example.hwtaskbackend.config.GlobalExceptionHandler;
import org.example.hwtaskbackend.task.dto.response.TaskResponse;
import org.example.hwtaskbackend.task.persistence.TaskPriority;
import org.example.hwtaskbackend.task.persistence.TaskStatus;
import org.example.hwtaskbackend.task.service.TaskNotFoundException;
import org.example.hwtaskbackend.task.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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

    @Test
    void createReturns201AndBody() throws Exception {
        UUID id = UUID.randomUUID();
        OffsetDateTime t = OffsetDateTime.ofInstant(java.time.Instant.parse("2025-01-01T00:00:00Z"), ZoneOffset.UTC);
        when(taskService.create(any())).thenReturn(
                new TaskResponse(id, "Title", null, TaskStatus.TODO, TaskPriority.LOW, t, t)
        );

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Title","description":null,"status":"TODO","priority":"LOW"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.title").value("Title"));
    }

    @Test
    void getNotFoundReturnsProblemDetail() throws Exception {
        UUID id = UUID.randomUUID();
        when(taskService.get(id)).thenThrow(new TaskNotFoundException(id));

        mockMvc.perform(get("/api/v1/tasks/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Not Found"));
    }

    @Test
    void listReturnsPage() throws Exception {
        UUID id = UUID.randomUUID();
        OffsetDateTime t = OffsetDateTime.ofInstant(java.time.Instant.parse("2025-01-01T00:00:00Z"), ZoneOffset.UTC);
        when(taskService.list(any())).thenReturn(
                new PageImpl<>(List.of(new TaskResponse(id, "A", null, TaskStatus.DONE, null, t, t)), PageRequest.of(0, 20), 1)
        );

        mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("A"));
    }

    @Test
    void deleteReturns204() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(delete("/api/v1/tasks/" + id))
                .andExpect(status().isNoContent());
        verify(taskService).delete(id);
    }

    @Test
    void createWithBlankTitleReturns400WithErrors() throws Exception {
        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"","description":null,"status":"TODO","priority":null}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists());
    }

    @Test
    void putUpdates() throws Exception {
        UUID id = UUID.randomUUID();
        OffsetDateTime t = OffsetDateTime.ofInstant(java.time.Instant.parse("2025-01-01T00:00:00Z"), ZoneOffset.UTC);
        when(taskService.update(eq(id), any())).thenReturn(
                new TaskResponse(id, "New", "d", TaskStatus.IN_PROGRESS, TaskPriority.HIGH, t, t)
        );

        mockMvc.perform(put("/api/v1/tasks/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"New","description":"d","status":"IN_PROGRESS","priority":"HIGH"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }
}
