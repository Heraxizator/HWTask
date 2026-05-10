package org.example.hwtaskbackend.task.service;

import org.example.hwtaskbackend.task.dto.TaskDtoMapper;
import org.example.hwtaskbackend.task.dto.request.CreateTaskRequest;
import org.example.hwtaskbackend.task.dto.request.UpdateTaskRequest;
import org.example.hwtaskbackend.task.dto.response.TaskResponse;
import org.example.hwtaskbackend.task.persistence.Task;
import org.example.hwtaskbackend.task.persistence.TaskRepository;
import org.example.hwtaskbackend.task.persistence.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
class DefaultTaskService implements TaskService {

    /**
     * Only entity fields; rejects Swagger/example placeholders like "string" that break JPA Sort resolution.
     */
    private static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of(
            "id", "title", "description", "status", "priority", "createdAt", "updatedAt"
    );

    private final TaskRepository taskRepository;

    DefaultTaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    @Transactional
    public TaskResponse create(CreateTaskRequest request) {
        TaskStatus status = request.status() != null ? request.status() : TaskStatus.TODO;
        Task task = new Task(request.title(), request.description(), status, request.priority());
        Task saved = taskRepository.save(task);
        return TaskDtoMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse get(UUID id) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
        return TaskDtoMapper.toResponse(task);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponse> list(Pageable pageable) {
        Pageable safe = sanitizePageable(pageable);
        return taskRepository.findAll(safe).map(TaskDtoMapper::toResponse);
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

    @Override
    @Transactional
    public TaskResponse update(UUID id, UpdateTaskRequest request) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status());
        task.setPriority(request.priority());
        Task saved = taskRepository.save(task);
        return TaskDtoMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException(id);
        }
        taskRepository.deleteById(id);
    }
}
