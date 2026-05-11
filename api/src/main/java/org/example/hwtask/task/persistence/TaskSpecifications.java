package org.example.hwtask.task.persistence;

import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.example.hwtask.tag.persistence.TaskTag;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;
import java.util.Locale;
import java.util.UUID;

public final class TaskSpecifications {

    private TaskSpecifications() {
    }

    public static Specification<Task> projectEquals(UUID projectId) {
        return (root, query, cb) -> cb.equal(root.get("projectId"), projectId);
    }

    public static Specification<Task> titleOrDescriptionContains(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        String p = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), p),
                cb.like(cb.lower(cb.coalesce(root.get("description"), cb.literal(""))), p)
        );
    }

    public static Specification<Task> hasAnyTag(Collection<UUID> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return null;
        }
        return (root, query, cb) -> {
            query.distinct(true);
            Subquery<Long> sq = query.subquery(Long.class);
            Root<TaskTag> tt = sq.from(TaskTag.class);
            sq.select(cb.literal(1L));
            sq.where(
                    cb.equal(tt.get("id").get("taskId"), root.get("id")),
                    tt.get("id").get("tagId").in(tagIds)
            );
            return cb.exists(sq);
        };
    }
}
