package org.example.hwtask.collaboration.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface TaskActivityRepository extends JpaRepository<TaskActivity, UUID> {

    List<TaskActivity> findByTaskIdOrderByCreatedAtDesc(UUID taskId);

    @Query(value = """
            SELECT DATE(a.created_at) AS day, a.event_type, COUNT(*) AS cnt
            FROM task_activity a
            INNER JOIN tasks t ON t.id = a.task_id
            WHERE t.project_id = :projectId
              AND t.deleted_at IS NULL
              AND a.created_at >= :from
              AND a.created_at < :to
            GROUP BY DATE(a.created_at), a.event_type
            ORDER BY day, a.event_type
            """, nativeQuery = true)
    List<Object[]> countByDayAndTypeForProject(@Param("projectId") UUID projectId, @Param("from") Instant from, @Param("to") Instant to);
}
