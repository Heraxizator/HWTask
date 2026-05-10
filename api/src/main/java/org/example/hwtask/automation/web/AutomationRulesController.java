package org.example.hwtask.automation.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.hwtask.automation.service.AutomationRuleAdminService;
import org.example.hwtask.automation.web.dto.AutomationRuleResponse;
import org.example.hwtask.automation.web.dto.CreateAutomationRuleRequest;
import org.example.hwtask.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/v1/projects/{projectId}/automation-rules")
@Validated
@Tag(name = "Automation rules")
public class AutomationRulesController {

    private final AutomationRuleAdminService automationRuleAdminService;

    public AutomationRulesController(AutomationRuleAdminService automationRuleAdminService) {
        this.automationRuleAdminService = automationRuleAdminService;
    }

    @GetMapping
    @Operation(summary = "Список правил")
    public List<AutomationRuleResponse> list(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID projectId
    ) {
        return automationRuleAdminService.list(projectId, user.getId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать правило")
    public AutomationRuleResponse create(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateAutomationRuleRequest request
    ) {
        return automationRuleAdminService.create(projectId, user.getId(), request);
    }

    @DeleteMapping("/{ruleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить правило")
    public void delete(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID projectId,
            @PathVariable UUID ruleId
    ) {
        automationRuleAdminService.delete(projectId, ruleId, user.getId());
    }
}
