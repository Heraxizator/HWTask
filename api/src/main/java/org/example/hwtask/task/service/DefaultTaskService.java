package org.example.hwtask.task.service;

import org.example.hwtask.collaboration.persistence.TaskActivityType;
import org.example.hwtask.collaboration.service.ActivityRecorder;
import org.example.hwtask.identity.persistence.ProjectMemberRepository;
import org.example.hwtask.identity.service.AccessControlService;
import org.example.hwtask.identity.service.ForbiddenException;
import org.example.hwtask.task.dto.TaskDtoMapper;
import org.example.hwtask.task.dto.request.CreateTaskRequest;
import org.example.hwtask.task.dto.request.UpdateTaskRequest;
import org.example.hwtask.task.dto.response.TaskResponse;
import org.example.hwtask.task.persistence.Task;
import org.example.hwtask.task.persistence.TaskMember;
import org.example.hwtask.task.persistence.TaskMemberRepository;
import org.example.hwtask.task.persistence.TaskMemberRole;
import org.example.hwtask.task.persistence.TaskRepository;
import org.example.hwtask.task.persistence.TaskStatus;
import org.example.hwtask.automation.service.AutomationRuleProcessor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
class DefaultTaskService implements TaskService {

    private static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of(
            "id", "title", "description", "status", "priority", "createdAt", "updatedAt",
            "dueAt", "projectId"
    );

    private final TaskRepository taskRepository;
    private final TaskMemberRepository taskMemberRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final AccessControlService accessControlService;
    private final ActivityRecorder activityRecorder;
    private final AutomationRuleProcessor automationRuleProcessor;

    DefaultTaskService(
            TaskRepository taskRepository,
            TaskMemberRepository taskMemberRepository,
            ProjectMemberRepository projectMemberRepository,
            AccessControlService accessControlService,
            ActivityRecorder activityRecorder,
            AutomationRuleProcessor automationRuleProcessor
    ) {
        this.taskRepository = taskRepository;
        this.taskMemberRepository = taskMemberRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.accessControlService = accessControlService;
        this.activityRecorder = activityRecorder;
        this.automationRuleProcessor = automationRuleProcessor;
    }

    @Override
    @Transactional
    public TaskResponse create(UUID currentUserId, CreateTaskRequest request) {
        UUID projectId = request.projectId();
        accessControlService.assertProjectMember(projectId, currentUserId);

        if (request.assigneeId() != null) {
            assertProjectMember(projectId, request.assigneeId());
        }
        validateExtraMembers(projectId, request.coAssigneeIds(), request.observerIds());

        if (request.parentTaskId() != null) {
            Task parent = taskRepository.findById(request.parentTaskId())
                    .orElseThrow(() -> new IllegalArgumentException("Родительская задача не найдена"));
            if (!parent.getProjectId().equals(projectId)) {
                throw new ForbiddenException("Подзадача должна быть в том же проекте");
            }
        }

        TaskStatus status = request.status() != null ? request.status() : TaskStatus.TODO;
        Task task = new Task(
                projectId,
                request.parentTaskId(),
                request.assigneeId(),
                currentUserId,
                request.dueAt(),
                request.title(),
                request.description(),
                status,
                request.priority()
        );
        Task saved = taskRepository.save(task);
        syncExtraMembers(saved.getId(), request.coAssigneeIds(), request.observerIds());

        activityRecorder.record(saved.getId(), currentUserId, TaskActivityType.CREATED,
                "Задача создана: " + saved.getTitle());

        return map(saved.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse get(UUID currentUserId, UUID taskId) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new TaskNotFoundException(taskId));
        accessControlService.assertProjectMember(task.getProjectId(), currentUserId);
        return map(taskId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponse> list(UUID currentUserId, UUID projectId, Pageable pageable) {
        accessControlService.assertProjectMember(projectId, currentUserId);
        Pageable safe = sanitizePageable(pageable);
        return taskRepository.findByProjectId(projectId, safe).map(t -> map(t.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> listSubtasks(UUID currentUserId, UUID parentTaskId) {
        Task parent = taskRepository.findById(parentTaskId).orElseThrow(() -> new TaskNotFoundException(parentTaskId));
        accessControlService.assertProjectMember(parent.getProjectId(), currentUserId);
        return taskRepository.findByParentTaskIdOrderByCreatedAtAsc(parentTaskId).stream()
                .map(t -> map(t.getId()))
                .toList();
    }

    @Override
    @Transactional
    public TaskResponse update(UUID currentUserId, UUID taskId, UpdateTaskRequest request) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new TaskNotFoundException(taskId));
        accessControlService.assertProjectMember(task.getProjectId(), currentUserId);

        TaskStatus oldStatus = task.getStatus();

        if (request.assigneeId() != null) {
            assertProjectMember(task.getProjectId(), request.assigneeId());
        }
        if (request.coAssigneeIds() != null && request.observerIds() != null) {
            validateExtraMembers(task.getProjectId(), request.coAssigneeIds(), request.observerIds());
        }

        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status());
        task.setPriority(request.priority());
        task.setAssigneeId(request.assigneeId());
        task.setDueAt(request.dueAt());

        taskRepository.save(task);

        if (request.coAssigneeIds() != null && request.observerIds() != null) {
            syncExtraMembers(taskId, request.coAssigneeIds(), request.observerIds());
        }

        activityRecorder.record(taskId, currentUserId, TaskActivityType.UPDATED, "Задача обновлена");
        if (oldStatus != task.getStatus()) {
            activityRecorder.record(taskId, currentUserId, TaskActivityType.STATUS_CHANGED,
                    "Статус: " + oldStatus + " → " + task.getStatus());
            automationRuleProcessor.onStatusChanged(task, oldStatus, currentUserId);
        }

        return map(taskId);
    }

    @Override
    @Transactional
    public void delete(UUID currentUserId, UUID taskId) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new TaskNotFoundException(taskId));
        accessControlService.assertProjectMember(task.getProjectId(), currentUserId);
        if (taskRepository.countByParentTaskId(taskId) > 0) {
            throw new IllegalArgumentException("Сначала удалите подзадачи");
        }
        taskRepository.deleteById(taskId);
    }

    private TaskResponse map(UUID taskId) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new TaskNotFoundException(taskId));
        List<TaskMember> extras = taskMemberRepository.findByIdTaskId(taskId);
        return TaskDtoMapper.toResponse(task, extras);
    }

    private void assertProjectMember(UUID projectId, UUID userId) {
        if (!projectMemberRepository.existsByIdProjectIdAndIdUserId(projectId, userId)) {
            throw new IllegalArgumentException("Пользователь не входит в проект: " + userId);
        }
    }

    private void validateExtraMembers(UUID projectId, List<UUID> coIds, List<UUID> observerIds) {
        Set<UUID> seenCo = new HashSet<>();
        for (UUID uid : coIds) {
            if (!seenCo.add(uid)) {
                throw new IllegalArgumentException("Дубликат в соисполнителях");
            }
            assertProjectMember(projectId, uid);
        }
        Set<UUID> seenObs = new HashSet<>();
        for (UUID uid : observerIds) {
            if (!seenObs.add(uid)) {
                throw new IllegalArgumentException("Дубликат в наблюдателях");
            }
            if (coIds.contains(uid)) {
                throw new IllegalArgumentException("Пользователь не может быть и соисполнителем, и наблюдателем");
            }
            assertProjectMember(projectId, uid);
        }
    }

    private void syncExtraMembers(UUID taskId, List<UUID> coAssigneeIds, List<UUID> observerIds) {
        taskMemberRepository.deleteByIdTaskId(taskId);
        for (UUID uid : coAssigneeIds) {
            taskMemberRepository.save(new TaskMember(taskId, uid, TaskMemberRole.CO_ASSIGNEE));
        }
        for (UUID uid : observerIds) {
            taskMemberRepository.save(new TaskMember(taskId, uid, TaskMemberRole.OBSERVER));
        }
    }

    private static Pageable sanitizePageable(Pageable pageable) {
        Sort sort = pageable.getSort();
        if (sort.isEmpty()) {
            return PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "createdAt")
            );
        }
        List<Sort.Order> orders = new ArrayList<>();
        for (Sort.Order order : sort) {
            if (ALLOWED_SORT_PROPERTIES.contains(order.getProperty())) {
                orders.add(order);
            }
        }
        Sort safeSort = orders.isEmpty()
                ? Sort.by(Sort.Direction.DESC, "createdAt")
                : Sort.by(orders);
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), safeSort);
    }
}
