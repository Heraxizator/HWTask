package org.example.hwtask.checklist.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskChecklistItemRepository extends JpaRepository<TaskChecklistItem, UUID> {

    List<TaskChecklistItem> findByTaskIdOrderBySortOrderAsc(UUID taskId);
}
