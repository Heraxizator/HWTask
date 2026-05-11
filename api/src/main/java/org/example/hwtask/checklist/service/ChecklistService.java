package org.example.hwtask.checklist.service;

import org.example.hwtask.checklist.dto.ChecklistItemResponse;
import org.example.hwtask.checklist.dto.CreateChecklistItemRequest;
import org.example.hwtask.checklist.dto.UpdateChecklistItemRequest;
import org.example.hwtask.checklist.persistence.TaskChecklistItem;
import org.example.hwtask.checklist.persistence.TaskChecklistItemRepository;
import org.example.hwtask.identity.service.AccessControlService;
import org.example.hwtask.task.persistence.Task;
import org.example.hwtask.task.persistence.TaskRepository;
import org.example.hwtask.task.service.TaskNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ChecklistService {

    private final TaskChecklistItemRepository checklistItemRepository;
    private final TaskRepository taskRepository;
    private final AccessControlService accessControlService;

    public ChecklistService(
            TaskChecklistItemRepository checklistItemRepository,
            TaskRepository taskRepository,
            AccessControlService accessControlService
    ) {
        this.checklistItemRepository = checklistItemRepository;
        this.taskRepository = taskRepository;
        this.accessControlService = accessControlService;
    }

    @Transactional(readOnly = true)
    public List<ChecklistItemResponse> list(UUID taskId, UUID actorUserId) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new TaskNotFoundException(taskId));
        accessControlService.assertProjectMember(task.getProjectId(), actorUserId);
        return checklistItemRepository.findByTaskIdOrderBySortOrderAsc(taskId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ChecklistItemResponse create(UUID taskId, UUID actorUserId, CreateChecklistItemRequest request) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new TaskNotFoundException(taskId));
        accessControlService.assertProjectMember(task.getProjectId(), actorUserId);
        int nextOrder = checklistItemRepository.findByTaskIdOrderBySortOrderAsc(taskId).stream()
                .mapToInt(TaskChecklistItem::getSortOrder)
                .max()
                .orElse(-1) + 1;
        TaskChecklistItem saved = checklistItemRepository.save(
                new TaskChecklistItem(taskId, request.title().trim(), false, nextOrder));
        return toResponse(saved);
    }

    @Transactional
    public ChecklistItemResponse update(UUID taskId, UUID itemId, UUID actorUserId, UpdateChecklistItemRequest request) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new TaskNotFoundException(taskId));
        accessControlService.assertProjectMember(task.getProjectId(), actorUserId);
        TaskChecklistItem item = checklistItemRepository.findById(itemId).orElseThrow();
        if (!item.getTaskId().equals(taskId)) {
            throw new IllegalArgumentException("Пункт не относится к задаче");
        }
        if (request.title() != null) {
            item.setTitle(request.title().trim());
        }
        if (request.done() != null) {
            item.setDone(request.done());
        }
        if (request.sortOrder() != null) {
            item.setSortOrder(request.sortOrder());
        }
        return toResponse(checklistItemRepository.save(item));
    }

    @Transactional
    public void delete(UUID taskId, UUID itemId, UUID actorUserId) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new TaskNotFoundException(taskId));
        accessControlService.assertProjectMember(task.getProjectId(), actorUserId);
        TaskChecklistItem item = checklistItemRepository.findById(itemId).orElseThrow();
        if (!item.getTaskId().equals(taskId)) {
            throw new IllegalArgumentException("Пункт не относится к задаче");
        }
        checklistItemRepository.delete(item);
    }

    private ChecklistItemResponse toResponse(TaskChecklistItem i) {
        return new ChecklistItemResponse(i.getId(), i.getTitle(), i.isDone(), i.getSortOrder());
    }
}
