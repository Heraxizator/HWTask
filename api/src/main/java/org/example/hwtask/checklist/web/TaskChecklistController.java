package org.example.hwtask.checklist.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.hwtask.checklist.dto.ChecklistItemResponse;
import org.example.hwtask.checklist.dto.CreateChecklistItemRequest;
import org.example.hwtask.checklist.dto.UpdateChecklistItemRequest;
import org.example.hwtask.checklist.service.ChecklistService;
import org.example.hwtask.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tasks/{taskId}/checklist-items")
@Tag(name = "Checklist")
public class TaskChecklistController {

    private final ChecklistService checklistService;

    public TaskChecklistController(ChecklistService checklistService) {
        this.checklistService = checklistService;
    }

    @GetMapping
    @Operation(summary = "Чеклист задачи")
    public List<ChecklistItemResponse> list(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID taskId
    ) {
        return checklistService.list(taskId, user.getId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Добавить пункт")
    public ChecklistItemResponse create(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID taskId,
            @Valid @RequestBody CreateChecklistItemRequest request
    ) {
        return checklistService.create(taskId, user.getId(), request);
    }

    @PutMapping("/{itemId}")
    @Operation(summary = "Изменить пункт")
    public ChecklistItemResponse update(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID taskId,
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateChecklistItemRequest request
    ) {
        return checklistService.update(taskId, itemId, user.getId(), request);
    }

    @DeleteMapping("/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить пункт")
    public void delete(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID taskId,
            @PathVariable UUID itemId
    ) {
        checklistService.delete(taskId, itemId, user.getId());
    }
}
