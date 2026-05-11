package org.example.hwtask.tag.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.hwtask.tag.dto.SetTaskTagsRequest;
import org.example.hwtask.tag.service.TagService;
import org.example.hwtask.security.UserPrincipal;
import org.example.hwtask.task.persistence.Task;
import org.example.hwtask.task.persistence.TaskRepository;
import org.example.hwtask.task.service.TaskNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tasks/{taskId}/tags")
@Tag(name = "Task tags")
public class TaskTagAssignmentController {

    private final TagService tagService;
    private final TaskRepository taskRepository;

    public TaskTagAssignmentController(TagService tagService, TaskRepository taskRepository) {
        this.tagService = tagService;
        this.taskRepository = taskRepository;
    }

    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Назначить теги задаче")
    public void setTags(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID taskId,
            @Valid @RequestBody SetTaskTagsRequest request
    ) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new TaskNotFoundException(taskId));
        tagService.setTaskTags(taskId, task.getProjectId(), request.tagIds(), user.getId());
    }
}
