package org.example.hwtaskbackend.task.service;

import org.example.hwtaskbackend.task.dto.request.CreateTaskRequest;
import org.example.hwtaskbackend.task.dto.request.UpdateTaskRequest;
import org.example.hwtaskbackend.task.dto.response.TaskResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TaskService {

    TaskResponse create(CreateTaskRequest request);

    TaskResponse get(UUID id);

    Page<TaskResponse> list(Pageable pageable);

    TaskResponse update(UUID id, UpdateTaskRequest request);

    void delete(UUID id);
}
