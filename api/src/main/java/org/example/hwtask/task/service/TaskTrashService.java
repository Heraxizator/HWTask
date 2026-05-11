package org.example.hwtask.task.service;

import org.example.hwtask.collaboration.service.AttachmentPurgeService;
import org.example.hwtask.identity.service.AccessControlService;
import org.example.hwtask.task.dto.response.TaskTrashEntryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class TaskTrashService {

    private final JdbcTemplate jdbcTemplate;
    private final AccessControlService accessControlService;
    private final AttachmentPurgeService attachmentPurgeService;

    public TaskTrashService(
            JdbcTemplate jdbcTemplate,
            AccessControlService accessControlService,
            AttachmentPurgeService attachmentPurgeService
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.accessControlService = accessControlService;
        this.attachmentPurgeService = attachmentPurgeService;
    }

    @Transactional(readOnly = true)
    public Page<TaskTrashEntryResponse> listDeleted(UUID projectId, UUID actorUserId, Pageable pageable) {
        accessControlService.assertProjectMember(projectId, actorUserId);
        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tasks WHERE project_id = ? AND deleted_at IS NOT NULL",
                Long.class,
                projectId
        );
        long t = total != null ? total : 0;
        List<TaskTrashEntryResponse> rows = jdbcTemplate.query(
                """
                        SELECT id, project_id, title, deleted_at FROM tasks
                        WHERE project_id = ? AND deleted_at IS NOT NULL
                        ORDER BY deleted_at DESC
                        LIMIT ? OFFSET ?
                        """,
                (rs, rowNum) -> {
                    Timestamp del = rs.getTimestamp("deleted_at");
                    Instant di = del != null ? del.toInstant() : null;
                    return new TaskTrashEntryResponse(
                            rs.getObject("id", UUID.class),
                            rs.getObject("project_id", UUID.class),
                            rs.getString("title"),
                            di == null ? null : OffsetDateTime.ofInstant(di, ZoneOffset.UTC)
                    );
                },
                projectId,
                pageable.getPageSize(),
                pageable.getOffset()
        );
        return new PageImpl<>(rows, pageable, t);
    }

    @Transactional
    public void restore(UUID actorUserId, UUID taskId) {
        UUID projectId = jdbcTemplate.query(
                "SELECT project_id FROM tasks WHERE id = ? AND deleted_at IS NOT NULL",
                rs -> rs.next() ? rs.getObject("project_id", UUID.class) : null,
                taskId
        );
        if (projectId == null) {
            throw new TaskNotFoundException(taskId);
        }
        accessControlService.assertProjectMember(projectId, actorUserId);
        int n = jdbcTemplate.update("UPDATE tasks SET deleted_at = NULL WHERE id = ?", taskId);
        if (n == 0) {
            throw new TaskNotFoundException(taskId);
        }
    }

    @Transactional
    public void purgePermanent(UUID actorUserId, UUID taskId) {
        UUID projectId = jdbcTemplate.query(
                "SELECT project_id FROM tasks WHERE id = ? AND deleted_at IS NOT NULL",
                rs -> rs.next() ? rs.getObject("project_id", UUID.class) : null,
                taskId
        );
        if (projectId == null) {
            throw new TaskNotFoundException(taskId);
        }
        accessControlService.assertProjectMember(projectId, actorUserId);
        Long subs = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tasks WHERE parent_task_id = ? AND deleted_at IS NULL",
                Long.class,
                taskId
        );
        if (subs != null && subs > 0) {
            throw new IllegalArgumentException("Сначала удалите или восстановите подзадачи");
        }
        attachmentPurgeService.deleteStoredFilesForTask(taskId);
        jdbcTemplate.update("DELETE FROM tasks WHERE id = ? AND deleted_at IS NOT NULL", taskId);
    }
}
