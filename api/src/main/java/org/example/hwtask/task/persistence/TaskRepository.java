package org.example.hwtask.task.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
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
}
