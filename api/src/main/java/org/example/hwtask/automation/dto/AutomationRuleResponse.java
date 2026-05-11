package org.example.hwtask.automation.dto;

import org.example.hwtask.automation.persistence.RuleActionType;
import org.example.hwtask.automation.persistence.RuleTriggerType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AutomationRuleResponse(
        UUID id,
        UUID projectId,
        RuleTriggerType triggerType,
        RuleActionType actionType,
        boolean enabled,
        OffsetDateTime createdAt
) {
}
