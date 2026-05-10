package org.example.hwtask.identity.service;

import org.example.hwtask.identity.persistence.OrgRole;
import org.example.hwtask.identity.persistence.OrganizationMember;
import org.example.hwtask.identity.persistence.OrganizationMemberRepository;
import org.example.hwtask.identity.persistence.Project;
import org.example.hwtask.identity.persistence.ProjectMember;
import org.example.hwtask.identity.persistence.ProjectMemberRepository;
import org.example.hwtask.identity.persistence.ProjectRepository;
import org.example.hwtask.identity.persistence.ProjectRole;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AccessControlService {

    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;
    private final OrganizationMemberRepository organizationMemberRepository;

    public AccessControlService(
            ProjectMemberRepository projectMemberRepository,
            ProjectRepository projectRepository,
            OrganizationMemberRepository organizationMemberRepository
    ) {
        this.projectMemberRepository = projectMemberRepository;
        this.projectRepository = projectRepository;
        this.organizationMemberRepository = organizationMemberRepository;
    }

    public void assertProjectMember(UUID projectId, UUID userId) {
        if (!projectMemberRepository.existsByIdProjectIdAndIdUserId(projectId, userId)) {
            throw new ForbiddenException("Нет доступа к проекту");
        }
    }

    public void assertProjectManager(UUID projectId, UUID userId) {
        ProjectMember pm = projectMemberRepository.findByIdProjectIdAndIdUserId(projectId, userId)
                .orElseThrow(() -> new ForbiddenException("Нет доступа к проекту"));
        if (pm.getRole() != ProjectRole.MANAGER) {
            throw new ForbiddenException("Требуются права руководителя проекта");
        }
    }

    public void assertOrgAdmin(UUID organizationId, UUID userId) {
        OrganizationMember om = organizationMemberRepository
                .findByIdOrganizationIdAndIdUserId(organizationId, userId)
                .orElseThrow(() -> new ForbiddenException("Нет доступа к организации"));
        if (om.getRole() != OrgRole.OWNER && om.getRole() != OrgRole.ADMIN) {
            throw new ForbiddenException("Требуются права администратора организации");
        }
    }

    /**
     * Добавление участников проекта: руководитель проекта или админ организации.
     */
    public void assertCanManageProjectMembers(UUID projectId, UUID actorUserId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Проект не найден"));
        ProjectMember pm = projectMemberRepository.findByIdProjectIdAndIdUserId(projectId, actorUserId)
                .orElseThrow(() -> new ForbiddenException("Нет доступа к проекту"));
        if (pm.getRole() == ProjectRole.MANAGER) {
            return;
        }
        assertOrgAdmin(project.getOrganizationId(), actorUserId);
    }

    public UUID organizationIdOfProject(UUID projectId) {
        return projectRepository.findById(projectId)
                .map(Project::getOrganizationId)
                .orElseThrow(() -> new IllegalArgumentException("Проект не найден"));
    }

    public void assertOrganizationMember(UUID organizationId, UUID userId) {
        if (!organizationMemberRepository.existsByIdOrganizationIdAndIdUserId(organizationId, userId)) {
            throw new ForbiddenException("Нет доступа к организации");
        }
    }
}
