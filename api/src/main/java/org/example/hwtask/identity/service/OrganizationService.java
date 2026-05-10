package org.example.hwtask.identity.service;

import org.example.hwtask.identity.persistence.OrgRole;
import org.example.hwtask.identity.persistence.Organization;
import org.example.hwtask.identity.persistence.OrganizationMember;
import org.example.hwtask.identity.persistence.OrganizationMemberRepository;
import org.example.hwtask.identity.persistence.OrganizationRepository;
import org.example.hwtask.identity.web.dto.CreateOrganizationRequest;
import org.example.hwtask.identity.web.dto.OrganizationResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;

    public OrganizationService(
            OrganizationRepository organizationRepository,
            OrganizationMemberRepository organizationMemberRepository
    ) {
        this.organizationRepository = organizationRepository;
        this.organizationMemberRepository = organizationMemberRepository;
    }

    @Transactional
    public OrganizationResponse create(CreateOrganizationRequest request, UUID creatorUserId) {
        Organization org = new Organization(request.name().trim());
        organizationRepository.save(org);
        organizationMemberRepository.save(new OrganizationMember(org.getId(), creatorUserId, OrgRole.OWNER));
        return toResponse(org);
    }

    @Transactional(readOnly = true)
    public List<OrganizationResponse> listForUser(UUID userId) {
        return organizationMemberRepository.findByIdUserId(userId).stream()
                .map(m -> organizationRepository.findById(m.getId().getOrganizationId()).orElseThrow())
                .map(this::toResponse)
                .toList();
    }

    private OrganizationResponse toResponse(Organization org) {
        return new OrganizationResponse(
                org.getId(),
                org.getName(),
                OffsetDateTime.ofInstant(org.getCreatedAt(), ZoneOffset.UTC)
        );
    }
}
