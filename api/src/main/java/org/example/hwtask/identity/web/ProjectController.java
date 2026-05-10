package org.example.hwtask.identity.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.hwtask.identity.service.ProjectService;
import org.example.hwtask.identity.web.dto.AddProjectMemberRequest;
import org.example.hwtask.identity.web.dto.CreateProjectRequest;
import org.example.hwtask.identity.web.dto.ProjectMemberResponse;
import org.example.hwtask.identity.web.dto.ProjectResponse;
import org.example.hwtask.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping("/organizations/{organizationId}/projects")
    @Operation(summary = "Проекты организации")
    public List<ProjectResponse> listByOrg(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID organizationId
    ) {
        return projectService.listByOrganization(organizationId, user.getId());
    }

    @PostMapping("/organizations/{organizationId}/projects")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать проект")
    public ProjectResponse create(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID organizationId,
            @Valid @RequestBody CreateProjectRequest request
    ) {
        return projectService.create(organizationId, request, user.getId());
    }

    @GetMapping("/projects/{projectId}/members")
    @Operation(summary = "Участники проекта")
    public List<ProjectMemberResponse> members(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID projectId
    ) {
        return projectService.listMembers(projectId, user.getId());
    }

    @PostMapping("/projects/{projectId}/members")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Добавить участника проекта")
    public ProjectMemberResponse addMember(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID projectId,
            @Valid @RequestBody AddProjectMemberRequest request
    ) {
        return projectService.addMember(projectId, request, user.getId());
    }
}
