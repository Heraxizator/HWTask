package org.example.hwtask.task.service;

import org.example.hwtask.task.dto.request.CreateTaskRequest;
import org.example.hwtask.task.dto.request.UpdateTaskRequest;
import org.example.hwtask.task.dto.response.TaskResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface TaskService {

    TaskResponse create(UUID currentUserId, CreateTaskRequest request);

    TaskResponse get(UUID currentUserId, UUID taskId);

    Page<TaskResponse> list(UUID currentUserId, UUID projectId, Pageable pageable, List<UUID> tagIds, String search);

    List<TaskResponse> listSubtasks(UUID currentUserId, UUID parentTaskId);

    TaskResponse update(UUID currentUserId, UUID taskId, UpdateTaskRequest request);

    void delete(UUID currentUserId, UUID taskId);
}
