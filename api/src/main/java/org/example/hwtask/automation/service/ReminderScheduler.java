package org.example.hwtask.automation.service;

import org.example.hwtask.automation.persistence.TaskReminder;
import org.example.hwtask.automation.persistence.TaskReminderRepository;
import org.example.hwtask.collaboration.persistence.TaskActivityType;
import org.example.hwtask.collaboration.service.ActivityRecorder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
public class ReminderScheduler {

    private final TaskReminderRepository taskReminderRepository;
    private final ActivityRecorder activityRecorder;

    public ReminderScheduler(TaskReminderRepository taskReminderRepository, ActivityRecorder activityRecorder) {
        this.taskReminderRepository = taskReminderRepository;
        this.activityRecorder = activityRecorder;
    }

    @Scheduled(fixedDelayString = "${hwtask.reminders.poll-interval-ms:60000}")
    @Transactional
    public void fireDueReminders() {
        Instant now = Instant.now();
        List<TaskReminder> due = taskReminderRepository.findByRemindAtLessThanEqualAndFiredAtIsNull(now);
        for (TaskReminder r : due) {
            r.setFiredAt(now);
            activityRecorder.record(r.getTaskId(), r.getUserId(), TaskActivityType.REMINDER_FIRED, "Напоминание по задаче");
        }
        taskReminderRepository.saveAll(due);
    }
}
