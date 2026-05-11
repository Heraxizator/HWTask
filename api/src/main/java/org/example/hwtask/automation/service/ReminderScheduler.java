package org.example.hwtask.automation.service;

import org.example.hwtask.automation.persistence.TaskReminder;
import org.example.hwtask.automation.persistence.TaskReminderRepository;
import org.example.hwtask.collaboration.persistence.TaskActivityType;
import org.example.hwtask.collaboration.service.ActivityRecorder;
import org.example.hwtask.task.persistence.TaskRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
public class ReminderScheduler {

    private final TaskReminderRepository taskReminderRepository;
    private final TaskRepository taskRepository;
    private final ActivityRecorder activityRecorder;

    public ReminderScheduler(
            TaskReminderRepository taskReminderRepository,
            TaskRepository taskRepository,
            ActivityRecorder activityRecorder
    ) {
        this.taskReminderRepository = taskReminderRepository;
        this.taskRepository = taskRepository;
        this.activityRecorder = activityRecorder;
    }

    @Scheduled(fixedDelayString = "${hwtask.reminders.poll-interval-ms:60000}")
    @Transactional
    public void fireDueReminders() {
        Instant now = Instant.now();
        List<TaskReminder> due = taskReminderRepository.findByRemindAtLessThanEqualAndFiredAtIsNull(now);
        for (TaskReminder r : due) {
            if (taskRepository.findById(r.getTaskId()).isPresent()) {
                activityRecorder.record(r.getTaskId(), r.getUserId(), TaskActivityType.REMINDER_FIRED, "Напоминание по задаче");
            }
            r.setFiredAt(now);
        }
        taskReminderRepository.saveAll(due);
    }
}
