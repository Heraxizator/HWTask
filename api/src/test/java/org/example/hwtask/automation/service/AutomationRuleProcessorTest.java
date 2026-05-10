package org.example.hwtask.automation.service;

import org.example.hwtask.automation.persistence.AutomationRule;
import org.example.hwtask.automation.persistence.AutomationRuleRepository;
import org.example.hwtask.automation.persistence.RuleActionType;
import org.example.hwtask.automation.persistence.RuleTriggerType;
import org.example.hwtask.collaboration.persistence.TaskActivityType;
import org.example.hwtask.collaboration.service.ActivityRecorder;
import org.example.hwtask.task.persistence.Task;
import org.example.hwtask.task.persistence.TaskStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutomationRuleProcessorTest {

    @Mock
    private AutomationRuleRepository automationRuleRepository;

    @Mock
    private ActivityRecorder activityRecorder;

    @InjectMocks
    private AutomationRuleProcessor processor;

    @Test
    void onStatusChanged_recordsActivityWhenAddNoteRuleMatches() {
        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        Task task = mock(Task.class);
        when(task.getProjectId()).thenReturn(projectId);
        when(task.getId()).thenReturn(taskId);
        when(task.getStatus()).thenReturn(TaskStatus.IN_PROGRESS);

        AutomationRule rule = new AutomationRule(
                projectId, RuleTriggerType.ON_STATUS_CHANGE, RuleActionType.ADD_ACTIVITY_NOTE, true);
        when(automationRuleRepository.findByProjectIdAndEnabledIsTrue(projectId)).thenReturn(List.of(rule));

        processor.onStatusChanged(task, TaskStatus.TODO, actorId);

        verify(activityRecorder).record(
                eq(taskId),
                eq(actorId),
                eq(TaskActivityType.RULE_TRIGGERED),
                contains("TODO → IN_PROGRESS"));
    }

    @Test
    void onStatusChanged_skipsWhenTriggerIsNotStatusChange() {
        UUID projectId = UUID.randomUUID();
        Task task = mock(Task.class);
        when(task.getProjectId()).thenReturn(projectId);

        AutomationRule rule = new AutomationRule(
                projectId, RuleTriggerType.ON_TASK_OVERDUE, RuleActionType.ADD_ACTIVITY_NOTE, true);
        when(automationRuleRepository.findByProjectIdAndEnabledIsTrue(projectId)).thenReturn(List.of(rule));

        processor.onStatusChanged(task, TaskStatus.TODO, UUID.randomUUID());

        verify(activityRecorder, never()).record(any(), any(), any(), any());
    }

    @Test
    void onStatusChanged_notifyAssigneeRecordsOnlyWhenAssigneeSet() {
        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID assigneeId = UUID.randomUUID();

        Task task = mock(Task.class);
        when(task.getProjectId()).thenReturn(projectId);
        when(task.getId()).thenReturn(taskId);
        when(task.getAssigneeId()).thenReturn(assigneeId);

        AutomationRule rule = new AutomationRule(
                projectId, RuleTriggerType.ON_STATUS_CHANGE, RuleActionType.NOTIFY_ASSIGNEE, true);
        when(automationRuleRepository.findByProjectIdAndEnabledIsTrue(projectId)).thenReturn(List.of(rule));

        processor.onStatusChanged(task, TaskStatus.TODO, actorId);

        verify(activityRecorder).record(
                eq(taskId),
                eq(actorId),
                eq(TaskActivityType.RULE_TRIGGERED),
                contains("уведомление"));
    }

    @Test
    void onStatusChanged_notifyAssigneeDoesNothingWhenNoAssignee() {
        UUID projectId = UUID.randomUUID();
        Task task = mock(Task.class);
        when(task.getProjectId()).thenReturn(projectId);
        when(task.getAssigneeId()).thenReturn(null);

        AutomationRule rule = new AutomationRule(
                projectId, RuleTriggerType.ON_STATUS_CHANGE, RuleActionType.NOTIFY_ASSIGNEE, true);
        when(automationRuleRepository.findByProjectIdAndEnabledIsTrue(projectId)).thenReturn(List.of(rule));

        processor.onStatusChanged(task, TaskStatus.TODO, UUID.randomUUID());

        verify(activityRecorder, never()).record(any(), any(), any(), any());
    }
}
