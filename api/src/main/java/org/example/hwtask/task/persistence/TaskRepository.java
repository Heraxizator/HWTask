package org.example.hwtask.task.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID>, JpaSpecificationExecutor<Task> {

    long countByProjectId(UUID projectId);

    Page<Task> findByProjectId(UUID projectId, Pageable pageable);

    List<Task> findByParentTaskIdOrderByCreatedAtAsc(UUID parentTaskId);

    long countByParentTaskId(UUID parentTaskId);

    long countByProjectIdAndStatus(UUID projectId, TaskStatus status);

    @Query("""
            select count(t) from Task t
            where t.projectId = :projectId
            and t.dueAt is not null
            and t.dueAt < :now
            and t.status <> :done
            """)
    long countOverdue(@Param("projectId") UUID projectId, @Param("now") Instant now, @Param("done") TaskStatus done);

    @Query(value = """
            SELECT DATE(t.created_at) AS day, COUNT(*) AS cnt
            FROM tasks t
            WHERE t.project_id = :projectId
              AND t.deleted_at IS NULL
              AND t.created_at >= :from
              AND t.created_at < :to
            GROUP BY DATE(t.created_at)
            ORDER BY day
            """, nativeQuery = true)
    List<Object[]> countCreatedByDay(@Param("projectId") UUID projectId, @Param("from") Instant from, @Param("to") Instant to);

    @Query(value = """
            SELECT DATE(t.updated_at) AS day, COUNT(*) AS cnt
            FROM tasks t
            WHERE t.project_id = :projectId
              AND t.deleted_at IS NULL
              AND t.status = 'DONE'
              AND t.updated_at >= :from
              AND t.updated_at < :to
            GROUP BY DATE(t.updated_at)
            ORDER BY day
            """, nativeQuery = true)
    List<Object[]> countDoneByDay(@Param("projectId") UUID projectId, @Param("from") Instant from, @Param("to") Instant to);

    @Query(value = """
            SELECT COALESCE(AVG(EXTRACT(EPOCH FROM (t.updated_at - t.created_at)) / 3600.0), 0)
            FROM tasks t
            WHERE t.project_id = :projectId
              AND t.deleted_at IS NULL
              AND t.status = 'DONE'
              AND t.updated_at >= :from
              AND t.updated_at < :to
            """, nativeQuery = true)
    double avgLeadTimeHoursForDoneTasks(@Param("projectId") UUID projectId, @Param("from") Instant from, @Param("to") Instant to);
}
