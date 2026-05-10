package org.example.hwtask.task.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskMemberRepository extends JpaRepository<TaskMember, TaskMemberId> {

    List<TaskMember> findByIdTaskId(UUID taskId);

    void deleteByIdTaskId(UUID taskId);
}
