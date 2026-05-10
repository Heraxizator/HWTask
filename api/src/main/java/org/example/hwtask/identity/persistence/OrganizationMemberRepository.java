package org.example.hwtask.identity.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, OrganizationMemberId> {

    List<OrganizationMember> findByIdUserId(UUID userId);

    Optional<OrganizationMember> findByIdOrganizationIdAndIdUserId(UUID organizationId, UUID userId);

    boolean existsByIdOrganizationIdAndIdUserId(UUID organizationId, UUID userId);
}
