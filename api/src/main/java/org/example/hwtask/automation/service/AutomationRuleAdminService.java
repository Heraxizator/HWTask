package org.example.hwtask.automation.service;

import org.example.hwtask.automation.persistence.AutomationRule;
import org.example.hwtask.automation.persistence.AutomationRuleRepository;
import org.example.hwtask.automation.web.dto.AutomationRuleResponse;
import org.example.hwtask.automation.web.dto.CreateAutomationRuleRequest;
import org.example.hwtask.identity.service.AccessControlService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class AutomationRuleAdminService {

    private final AutomationRuleRepository automationRuleRepository;
    private final AccessControlService accessControlService;

    public AutomationRuleAdminService(
            AutomationRuleRepository automationRuleRepository,
            AccessControlService accessControlService
    ) {
        this.automationRuleRepository = automationRuleRepository;
        this.accessControlService = accessControlService;
    }

    @Transactional
    public AutomationRuleResponse create(UUID projectId, UUID actorUserId, CreateAutomationRuleRequest request) {
        accessControlService.assertProjectManager(projectId, actorUserId);
        AutomationRule rule = new AutomationRule(projectId, request.triggerType(), request.actionType(), request.enabled());
        automationRuleRepository.save(rule);
        return toResponse(rule);
    }

    @Transactional(readOnly = true)
    public List<AutomationRuleResponse> list(UUID projectId, UUID actorUserId) {
        accessControlService.assertProjectMember(projectId, actorUserId);
        return automationRuleRepository.findByProjectId(projectId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void delete(UUID projectId, UUID ruleId, UUID actorUserId) {
        accessControlService.assertProjectManager(projectId, actorUserId);
        AutomationRule rule = automationRuleRepository.findById(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("Правило не найдено"));
        if (!rule.getProjectId().equals(projectId)) {
            throw new IllegalArgumentException("Правило принадлежит другому проекту");
        }
        automationRuleRepository.delete(rule);
    }

    private AutomationRuleResponse toResponse(AutomationRule r) {
        return new AutomationRuleResponse(
                r.getId(),
                r.getProjectId(),
                r.getTriggerType(),
                r.getActionType(),
                r.isEnabled(),
                OffsetDateTime.ofInstant(r.getCreatedAt(), ZoneOffset.UTC)
        );
    }
}
