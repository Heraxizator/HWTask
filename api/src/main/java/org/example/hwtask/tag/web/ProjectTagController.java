package org.example.hwtask.tag.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.hwtask.tag.dto.CreateTagRequest;
import org.example.hwtask.tag.dto.TagResponse;
import org.example.hwtask.tag.service.TagService;
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
@RequestMapping("/api/v1/projects/{projectId}/tags")
@Tag(name = "Tags")
public class ProjectTagController {

    private final TagService tagService;

    public ProjectTagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    @Operation(summary = "Теги проекта")
    public List<TagResponse> list(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID projectId
    ) {
        return tagService.listProjectTags(projectId, user.getId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать тег")
    public TagResponse create(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateTagRequest request
    ) {
        return tagService.createTag(projectId, request.name(), user.getId());
    }
}
