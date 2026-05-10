package org.example.hwtask.collaboration.service;

import org.example.hwtask.automation.persistence.TaskReminder;
import org.example.hwtask.automation.persistence.TaskReminderRepository;
import org.example.hwtask.collaboration.persistence.TaskActivity;
import org.example.hwtask.collaboration.persistence.TaskActivityRepository;
import org.example.hwtask.collaboration.persistence.TaskActivityType;
import org.example.hwtask.collaboration.persistence.TaskAttachment;
import org.example.hwtask.collaboration.persistence.TaskAttachmentRepository;
import org.example.hwtask.collaboration.persistence.TaskComment;
import org.example.hwtask.collaboration.persistence.TaskCommentRepository;
import org.example.hwtask.collaboration.web.dto.ActivityEntryResponse;
import org.example.hwtask.collaboration.web.dto.AttachmentResponse;
import org.example.hwtask.automation.web.dto.CreateReminderRequest;
import org.example.hwtask.automation.web.dto.ReminderResponse;
import org.example.hwtask.collaboration.web.dto.CommentResponse;
import org.example.hwtask.collaboration.web.dto.CreateCommentRequest;
import org.example.hwtask.identity.service.AccessControlService;
import org.example.hwtask.security.AttachmentStorageProperties;
import org.example.hwtask.task.persistence.Task;
import org.example.hwtask.task.persistence.TaskRepository;
import org.example.hwtask.task.service.TaskNotFoundException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class CollaborationService {

    private final TaskRepository taskRepository;
    private final AccessControlService accessControlService;
    private final TaskCommentRepository taskCommentRepository;
    private final TaskActivityRepository taskActivityRepository;
    private final TaskAttachmentRepository taskAttachmentRepository;
    private final TaskReminderRepository taskReminderRepository;
    private final ActivityRecorder activityRecorder;
    private final AttachmentStorageProperties storageProperties;

    public CollaborationService(
            TaskRepository taskRepository,
            AccessControlService accessControlService,
            TaskCommentRepository taskCommentRepository,
            TaskActivityRepository taskActivityRepository,
            TaskAttachmentRepository taskAttachmentRepository,
            TaskReminderRepository taskReminderRepository,
            ActivityRecorder activityRecorder,
            AttachmentStorageProperties storageProperties
    ) {
        this.taskRepository = taskRepository;
        this.accessControlService = accessControlService;
        this.taskCommentRepository = taskCommentRepository;
        this.taskActivityRepository = taskActivityRepository;
        this.taskAttachmentRepository = taskAttachmentRepository;
        this.taskReminderRepository = taskReminderRepository;
        this.activityRecorder = activityRecorder;
        this.storageProperties = storageProperties;
    }

    @Transactional
    public CommentResponse addComment(UUID taskId, UUID currentUserId, CreateCommentRequest request) {
        Task task = loadTask(taskId);
        accessControlService.assertProjectMember(task.getProjectId(), currentUserId);
        TaskComment c = new TaskComment(taskId, currentUserId, request.body().trim());
        taskCommentRepository.save(c);
        activityRecorder.record(taskId, currentUserId, TaskActivityType.COMMENT_ADDED, "Комментарий добавлен");
        return toCommentResponse(c);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> listComments(UUID taskId, UUID currentUserId) {
        Task task = loadTask(taskId);
        accessControlService.assertProjectMember(task.getProjectId(), currentUserId);
        return taskCommentRepository.findByTaskIdOrderByCreatedAtAsc(taskId).stream()
                .map(this::toCommentResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ActivityEntryResponse> listActivity(UUID taskId, UUID currentUserId) {
        Task task = loadTask(taskId);
        accessControlService.assertProjectMember(task.getProjectId(), currentUserId);
        return taskActivityRepository.findByTaskIdOrderByCreatedAtDesc(taskId).stream()
                .map(this::toActivityResponse)
                .toList();
    }

    @Transactional
    public AttachmentResponse storeAttachment(UUID taskId, UUID currentUserId, MultipartFile file) throws IOException {
        Task task = loadTask(taskId);
        accessControlService.assertProjectMember(task.getProjectId(), currentUserId);
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Пустой файл");
        }
        Path base = Path.of(storageProperties.attachmentsDir()).toAbsolutePath().normalize();
        Files.createDirectories(base);
        Path taskDir = base.resolve(taskId.toString());
        Files.createDirectories(taskDir);
        String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
        String storedName = UUID.randomUUID() + "_" + original.replaceAll("[^a-zA-Z0-9._-]", "_");
        Path target = taskDir.resolve(storedName);
        file.transferTo(target);

        TaskAttachment att = new TaskAttachment(
                taskId,
                currentUserId,
                original,
                file.getContentType(),
                file.getSize(),
                target.toString()
        );
        taskAttachmentRepository.save(att);
        activityRecorder.record(taskId, currentUserId, TaskActivityType.FILE_ATTACHED, "Файл: " + original);
        return toAttachmentResponse(att);
    }

    @Transactional(readOnly = true)
    public List<AttachmentResponse> listAttachments(UUID taskId, UUID currentUserId) {
        Task task = loadTask(taskId);
        accessControlService.assertProjectMember(task.getProjectId(), currentUserId);
        return taskAttachmentRepository.findByTaskIdOrderByCreatedAtDesc(taskId).stream()
                .map(this::toAttachmentResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ResourceAttachment loadAttachmentFile(UUID taskId, UUID attachmentId, UUID currentUserId) throws IOException {
        Task task = loadTask(taskId);
        accessControlService.assertProjectMember(task.getProjectId(), currentUserId);
        TaskAttachment att = taskAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new IllegalArgumentException("Вложение не найдено"));
        if (!att.getTaskId().equals(taskId)) {
            throw new IllegalArgumentException("Вложение не относится к задаче");
        }
        Path path = Path.of(att.getStoragePath());
        Resource resource = new UrlResource(path.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            throw new IllegalArgumentException("Файл недоступен");
        }
        MediaType mediaType = att.getContentType() != null
                ? MediaType.parseMediaType(att.getContentType())
                : MediaType.APPLICATION_OCTET_STREAM;
        return new ResourceAttachment(resource, att.getFileName(), mediaType);
    }

    @Transactional
    public ReminderResponse scheduleReminder(UUID taskId, UUID currentUserId, CreateReminderRequest request) {
        Task task = loadTask(taskId);
        accessControlService.assertProjectMember(task.getProjectId(), currentUserId);
        TaskReminder r = new TaskReminder(taskId, currentUserId, request.remindAt());
        taskReminderRepository.save(r);
        return new ReminderResponse(
                r.getId(),
                r.getTaskId(),
                r.getUserId(),
                OffsetDateTime.ofInstant(r.getRemindAt(), ZoneOffset.UTC),
                r.getFiredAt() == null ? null : OffsetDateTime.ofInstant(r.getFiredAt(), ZoneOffset.UTC)
        );
    }

    @Transactional(readOnly = true)
    public List<ReminderResponse> listReminders(UUID taskId, UUID currentUserId) {
        Task task = loadTask(taskId);
        accessControlService.assertProjectMember(task.getProjectId(), currentUserId);
        return taskReminderRepository.findByTaskIdOrderByRemindAtAsc(taskId).stream()
                .map(r -> new ReminderResponse(
                        r.getId(),
                        r.getTaskId(),
                        r.getUserId(),
                        OffsetDateTime.ofInstant(r.getRemindAt(), ZoneOffset.UTC),
                        r.getFiredAt() == null ? null : OffsetDateTime.ofInstant(r.getFiredAt(), ZoneOffset.UTC)
                ))
                .toList();
    }

    private Task loadTask(UUID taskId) {
        return taskRepository.findById(taskId).orElseThrow(() -> new TaskNotFoundException(taskId));
    }

    private CommentResponse toCommentResponse(TaskComment c) {
        return new CommentResponse(
                c.getId(),
                c.getAuthorId(),
                c.getBody(),
                OffsetDateTime.ofInstant(c.getCreatedAt(), ZoneOffset.UTC)
        );
    }

    private ActivityEntryResponse toActivityResponse(TaskActivity a) {
        return new ActivityEntryResponse(
                a.getId(),
                a.getActorId(),
                a.getEventType(),
                a.getSummary(),
                OffsetDateTime.ofInstant(a.getCreatedAt(), ZoneOffset.UTC)
        );
    }

    private AttachmentResponse toAttachmentResponse(TaskAttachment a) {
        return new AttachmentResponse(
                a.getId(),
                a.getUploadedBy(),
                a.getFileName(),
                a.getContentType(),
                a.getSizeBytes(),
                OffsetDateTime.ofInstant(a.getCreatedAt(), ZoneOffset.UTC)
        );
    }

    public record ResourceAttachment(Resource resource, String fileName, MediaType mediaType) {
    }
}
