package org.example.hwtask.automation.service;

import org.example.hwtask.automation.persistence.AutomationRule;
import org.example.hwtask.automation.persistence.AutomationRuleRepository;
import org.example.hwtask.automation.persistence.RuleActionType;
import org.example.hwtask.automation.persistence.RuleTriggerType;
import org.example.hwtask.collaboration.persistence.TaskActivityType;
import org.example.hwtask.collaboration.service.ActivityRecorder;
import org.example.hwtask.task.persistence.Task;
import org.example.hwtask.task.persistence.TaskStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AutomationRuleProcessor {

    private final AutomationRuleRepository automationRuleRepository;
    private final ActivityRecorder activityRecorder;

    public AutomationRuleProcessor(
            AutomationRuleRepository automationRuleRepository,
            ActivityRecorder activityRecorder
    ) {
        this.automationRuleRepository = automationRuleRepository;
        this.activityRecorder = activityRecorder;
    }

    public void onStatusChanged(Task task, TaskStatus oldStatus, UUID actorId) {
        List<AutomationRule> rules = automationRuleRepository.findByProjectIdAndEnabledIsTrue(task.getProjectId());
        for (AutomationRule rule : rules) {
            if (rule.getTriggerType() != RuleTriggerType.ON_STATUS_CHANGE || !rule.isEnabled()) {
                continue;
            }
            switch (rule.getActionType()) {
                case ADD_ACTIVITY_NOTE -> activityRecorder.record(
                        task.getId(),
                        actorId,
                        TaskActivityType.RULE_TRIGGERED,
                        "Автоматизация: статус изменён (" + oldStatus + " → " + task.getStatus() + ")"
                );
                case NOTIFY_ASSIGNEE -> {
                    if (task.getAssigneeId() != null) {
                        activityRecorder.record(
                                task.getId(),
                                actorId,
                                TaskActivityType.RULE_TRIGGERED,
                                "Автоматизация: уведомление назначенному исполнителю"
                        );
                    }
                }
            }
        }
    }
}
