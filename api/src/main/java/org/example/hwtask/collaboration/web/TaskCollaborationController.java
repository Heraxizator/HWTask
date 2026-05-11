package org.example.hwtask.collaboration.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.hwtask.automation.dto.CreateReminderRequest;
import org.example.hwtask.automation.dto.ReminderResponse;
import org.example.hwtask.collaboration.dto.ActivityEntryResponse;
import org.example.hwtask.collaboration.dto.AttachmentResponse;
import org.example.hwtask.collaboration.dto.CommentResponse;
import org.example.hwtask.collaboration.dto.CreateCommentRequest;
import org.example.hwtask.collaboration.service.CollaborationService;
import org.example.hwtask.security.UserPrincipal;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tasks/{taskId}")
@Validated
@Tag(name = "Task collaboration")
public class TaskCollaborationController {

    private final CollaborationService collaborationService;

    public TaskCollaborationController(CollaborationService collaborationService) {
        this.collaborationService = collaborationService;
    }

    @GetMapping("/comments")
    @Operation(summary = "Комментарии")
    public List<CommentResponse> comments(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID taskId
    ) {
        return collaborationService.listComments(taskId, user.getId());
    }

    @PostMapping("/comments")
    @Operation(summary = "Добавить комментарий")
    public CommentResponse addComment(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID taskId,
            @Valid @RequestBody CreateCommentRequest request
    ) {
        return collaborationService.addComment(taskId, user.getId(), request);
    }

    @GetMapping("/activity")
    @Operation(summary = "Лента активности")
    public List<ActivityEntryResponse> activity(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID taskId
    ) {
        return collaborationService.listActivity(taskId, user.getId());
    }

    @GetMapping("/attachments")
    @Operation(summary = "Вложения")
    public List<AttachmentResponse> attachments(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID taskId
    ) {
        return collaborationService.listAttachments(taskId, user.getId());
    }

    @PostMapping(value = "/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Загрузить файл")
    public AttachmentResponse upload(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID taskId,
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        return collaborationService.storeAttachment(taskId, user.getId(), file);
    }

    @GetMapping("/attachments/{attachmentId}/file")
    @Operation(summary = "Скачать файл")
    public ResponseEntity<Resource> download(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID taskId,
            @PathVariable UUID attachmentId
    ) throws IOException {
        CollaborationService.ResourceAttachment ra =
                collaborationService.loadAttachmentFile(taskId, attachmentId, user.getId());
        return ResponseEntity.ok()
                .contentType(ra.mediaType())
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + ra.fileName() + "\"")
                .body(ra.resource());
    }

    @GetMapping("/reminders")
    @Operation(summary = "Напоминания по задаче")
    public List<ReminderResponse> reminders(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID taskId
    ) {
        return collaborationService.listReminders(taskId, user.getId());
    }

    @PostMapping("/reminders")
    @Operation(summary = "Запланировать напоминание")
    public ReminderResponse addReminder(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID taskId,
            @Valid @RequestBody CreateReminderRequest request
    ) {
        return collaborationService.scheduleReminder(taskId, user.getId(), request);
    }
}
