package org.example.hwtask.collaboration.service;

import org.example.hwtask.collaboration.persistence.TaskActivity;
import org.example.hwtask.collaboration.persistence.TaskActivityRepository;
import org.example.hwtask.collaboration.persistence.TaskActivityType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ActivityRecorder {

    private final TaskActivityRepository taskActivityRepository;

    public ActivityRecorder(TaskActivityRepository taskActivityRepository) {
        this.taskActivityRepository = taskActivityRepository;
    }

    @Transactional
    public void record(UUID taskId, UUID actorId, TaskActivityType type, String summary) {
        taskActivityRepository.save(new TaskActivity(taskId, actorId, type, summary));
    }
}
