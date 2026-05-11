package org.example.hwtask.notification.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TaskNotificationMuteRepository extends JpaRepository<TaskNotificationMute, TaskNotificationMuteId> {

    boolean existsByIdTaskIdAndIdUserId(UUID taskId, UUID userId);

    void deleteByIdTaskIdAndIdUserId(UUID taskId, UUID userId);
}
