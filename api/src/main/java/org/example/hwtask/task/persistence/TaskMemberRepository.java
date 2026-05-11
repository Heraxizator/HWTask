package org.example.hwtask.task.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface TaskMemberRepository extends JpaRepository<TaskMember, TaskMemberId> {

    List<TaskMember> findByIdTaskId(UUID taskId);

    List<TaskMember> findByIdTaskIdIn(Collection<UUID> taskIds);

    void deleteByIdTaskId(UUID taskId);
}
