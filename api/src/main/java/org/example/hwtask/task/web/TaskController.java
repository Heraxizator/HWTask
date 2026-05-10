package org.example.hwtask.task.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.hwtask.security.UserPrincipal;
import org.example.hwtask.task.dto.request.CreateTaskRequest;
import org.example.hwtask.task.dto.request.UpdateTaskRequest;
import org.example.hwtask.task.dto.response.TaskResponse;
import org.example.hwtask.task.service.TaskService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tasks")
@Validated
@Tag(name = "Tasks v1")
public class TaskController {

    private final TaskService taskService;

    TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать задачу")
    public TaskResponse create(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody CreateTaskRequest request
    ) {
        return taskService.create(user.getId(), request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить задачу")
    public TaskResponse get(@AuthenticationPrincipal UserPrincipal user, @PathVariable UUID id) {
        return taskService.get(user.getId(), id);
    }

    @GetMapping
    @Operation(summary = "Список задач проекта (постранично)")
    public Page<TaskResponse> list(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam UUID projectId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return taskService.list(user.getId(), projectId, pageable);
    }

    @GetMapping("/{taskId}/subtasks")
    @Operation(summary = "Подзадачи")
    public List<TaskResponse> subtasks(@AuthenticationPrincipal UserPrincipal user, @PathVariable UUID taskId) {
        return taskService.listSubtasks(user.getId(), taskId);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить задачу")
    public TaskResponse update(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTaskRequest request
    ) {
        return taskService.update(user.getId(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить задачу")
    public void delete(@AuthenticationPrincipal UserPrincipal user, @PathVariable UUID id) {
        taskService.delete(user.getId(), id);
    }
}
