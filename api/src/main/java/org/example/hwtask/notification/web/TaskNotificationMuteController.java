package org.example.hwtask.notification.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.hwtask.identity.service.AccessControlService;
import org.example.hwtask.notification.service.NotificationService;
import org.example.hwtask.security.UserPrincipal;
import org.example.hwtask.task.persistence.Task;
import org.example.hwtask.task.persistence.TaskRepository;
import org.example.hwtask.task.service.TaskNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tasks/{taskId}/notifications")
@Tag(name = "Task notification settings")
public class TaskNotificationMuteController {

    private final NotificationService notificationService;
    private final TaskRepository taskRepository;
    private final AccessControlService accessControlService;

    public TaskNotificationMuteController(
            NotificationService notificationService,
            TaskRepository taskRepository,
            AccessControlService accessControlService
    ) {
        this.notificationService = notificationService;
        this.taskRepository = taskRepository;
        this.accessControlService = accessControlService;
    }

    @PostMapping("/mute")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Не получать уведомления по задаче")
    public void mute(@AuthenticationPrincipal UserPrincipal user, @PathVariable UUID taskId) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new TaskNotFoundException(taskId));
        accessControlService.assertProjectMember(task.getProjectId(), user.getId());
        notificationService.muteTask(taskId, user.getId());
    }

    @DeleteMapping("/mute")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Снова получать уведомления по задаче")
    public void unmute(@AuthenticationPrincipal UserPrincipal user, @PathVariable UUID taskId) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new TaskNotFoundException(taskId));
        accessControlService.assertProjectMember(task.getProjectId(), user.getId());
        notificationService.unmuteTask(taskId, user.getId());
    }
}
