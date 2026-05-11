package org.example.hwtask.task.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.hwtask.security.UserPrincipal;
import org.example.hwtask.task.dto.response.TaskTrashEntryResponse;
import org.example.hwtask.task.service.TaskTrashService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Deleted tasks")
public class TaskTrashController {

    private final TaskTrashService taskTrashService;

    public TaskTrashController(TaskTrashService taskTrashService) {
        this.taskTrashService = taskTrashService;
    }

    @GetMapping("/projects/{projectId}/deleted-tasks")
    @Operation(summary = "Корзина: удалённые задачи проекта")
    public Page<TaskTrashEntryResponse> listTrash(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID projectId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return taskTrashService.listDeleted(projectId, user.getId(), pageable);
    }

    @PostMapping("/tasks/{taskId}/restore")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Восстановить задачу из корзины")
    public void restore(@AuthenticationPrincipal UserPrincipal user, @PathVariable UUID taskId) {
        taskTrashService.restore(user.getId(), taskId);
    }

    @DeleteMapping("/tasks/{taskId}/permanent")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить задачу безвозвратно")
    public void purge(@AuthenticationPrincipal UserPrincipal user, @PathVariable UUID taskId) {
        taskTrashService.purgePermanent(user.getId(), taskId);
    }
}
