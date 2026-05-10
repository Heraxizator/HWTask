package org.example.hwtask.automation.web.dto;

import jakarta.validation.constraints.NotNull;
import org.example.hwtask.automation.persistence.RuleActionType;
import org.example.hwtask.automation.persistence.RuleTriggerType;

public record CreateAutomationRuleRequest(
        @NotNull RuleTriggerType triggerType,
        @NotNull RuleActionType actionType,
        boolean enabled
) {
}
