package org.example.hwtask.automation.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface TaskReminderRepository extends JpaRepository<TaskReminder, UUID> {

    List<TaskReminder> findByRemindAtLessThanEqualAndFiredAtIsNull(Instant now);

    List<TaskReminder> findByTaskIdOrderByRemindAtAsc(UUID taskId);
}
