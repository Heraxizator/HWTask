package org.example.hwtask.timetracking.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
