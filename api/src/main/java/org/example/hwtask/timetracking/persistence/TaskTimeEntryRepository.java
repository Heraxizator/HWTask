package org.example.hwtask.timetracking.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskTimeEntryRepository extends JpaRepository<TaskTimeEntry, UUID> {

    List<TaskTimeEntry> findByTaskIdOrderByStartedAtDesc(UUID taskId);

    Optional<TaskTimeEntry> findFirstByUserIdAndEndedAtIsNullOrderByStartedAtDesc(UUID userId);

    @Query(value = """
            SELECT COALESCE(SUM(EXTRACT(EPOCH FROM (e.ended_at - e.started_at))), 0)
            FROM task_time_entries e
            INNER JOIN tasks t ON t.id = e.task_id
            WHERE t.project_id = :projectId
            AND e.ended_at IS NOT NULL
            AND t.deleted_at IS NULL
            """, nativeQuery = true)
    double sumSecondsForProject(@Param("projectId") UUID projectId);

    @Query(value = """
            SELECT e.user_id, COALESCE(SUM(EXTRACT(EPOCH FROM (e.ended_at - e.started_at))), 0)
            FROM task_time_entries e
            INNER JOIN tasks t ON t.id = e.task_id
            WHERE t.project_id = :projectId
            AND e.ended_at IS NOT NULL
            AND t.deleted_at IS NULL
            GROUP BY e.user_id
            """, nativeQuery = true)
    List<Object[]> sumSecondsByUserForProject(@Param("projectId") UUID projectId);

    @Query(value = """
            SELECT DATE(e.ended_at) AS day, COALESCE(SUM(EXTRACT(EPOCH FROM (e.ended_at - e.started_at))), 0) AS seconds
            FROM task_time_entries e
            INNER JOIN tasks t ON t.id = e.task_id
            WHERE t.project_id = :projectId
              AND t.deleted_at IS NULL
              AND e.ended_at IS NOT NULL
              AND e.ended_at >= :from
              AND e.ended_at < :to
            GROUP BY DATE(e.ended_at)
            ORDER BY day
            """, nativeQuery = true)
    List<Object[]> sumSecondsByDayForProject(@Param("projectId") UUID projectId, @Param("from") Instant from, @Param("to") Instant to);
}
