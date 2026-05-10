package org.example.hwtask.identity.service;

import org.example.hwtask.identity.persistence.Project;
import org.example.hwtask.identity.persistence.ProjectMember;
import org.example.hwtask.identity.persistence.ProjectMemberRepository;
import org.example.hwtask.identity.persistence.ProjectRepository;
import org.example.hwtask.identity.persistence.ProjectRole;
import org.example.hwtask.identity.persistence.User;
import org.example.hwtask.identity.persistence.UserRepository;
import org.example.hwtask.identity.web.dto.AddProjectMemberRequest;
import org.example.hwtask.identity.web.dto.CreateProjectRequest;
import org.example.hwtask.identity.web.dto.ProjectMemberResponse;
import org.example.hwtask.identity.web.dto.ProjectResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final AccessControlService accessControlService;

    public ProjectService(
            ProjectRepository projectRepository,
            ProjectMemberRepository projectMemberRepository,
            UserRepository userRepository,
            AccessControlService accessControlService
    ) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
        this.accessControlService = accessControlService;
    }

    @Transactional
    public ProjectResponse create(UUID organizationId, CreateProjectRequest request, UUID actorUserId) {
        accessControlService.assertOrgAdmin(organizationId, actorUserId);
        Project project = new Project(organizationId, request.name().trim());
        projectRepository.save(project);
        projectMemberRepository.save(new ProjectMember(project.getId(), actorUserId, ProjectRole.MANAGER));
        return toResponse(project);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> listByOrganization(UUID organizationId, UUID actorUserId) {
        accessControlService.assertOrganizationMember(organizationId, actorUserId);
        return projectRepository.findByOrganizationId(organizationId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProjectMemberResponse> listMembers(UUID projectId, UUID actorUserId) {
        accessControlService.assertProjectMember(projectId, actorUserId);
        return projectMemberRepository.findByIdProjectId(projectId).stream()
                .map(pm -> {
                    User u = userRepository.findById(pm.getId().getUserId()).orElseThrow();
                    return new ProjectMemberResponse(u.getId(), u.getEmail(), u.getDisplayName(), pm.getRole());
                })
                .toList();
    }

    @Transactional
    public ProjectMemberResponse addMember(UUID projectId, AddProjectMemberRequest request, UUID actorUserId) {
        accessControlService.assertCanManageProjectMembers(projectId, actorUserId);
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        ProjectMember member = new ProjectMember(projectId, user.getId(), request.role());
        projectMemberRepository.save(member);
        return new ProjectMemberResponse(user.getId(), user.getEmail(), user.getDisplayName(), member.getRole());
    }

    private ProjectResponse toResponse(Project p) {
        return new ProjectResponse(
                p.getId(),
                p.getOrganizationId(),
                p.getName(),
                OffsetDateTime.ofInstant(p.getCreatedAt(), ZoneOffset.UTC)
        );
    }
}
