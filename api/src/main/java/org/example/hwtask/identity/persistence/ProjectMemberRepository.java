package org.example.hwtask.identity.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, ProjectMemberId> {

    Optional<ProjectMember> findByIdProjectIdAndIdUserId(UUID projectId, UUID userId);

    boolean existsByIdProjectIdAndIdUserId(UUID projectId, UUID userId);

    List<ProjectMember> findByIdProjectId(UUID projectId);
}
