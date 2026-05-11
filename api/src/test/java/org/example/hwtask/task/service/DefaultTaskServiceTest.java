package org.example.hwtask.task.service;

import org.example.hwtask.automation.service.AutomationRuleProcessor;
import org.example.hwtask.collaboration.service.ActivityRecorder;
import org.example.hwtask.identity.persistence.ProjectMemberRepository;
import org.example.hwtask.identity.service.AccessControlService;
import org.example.hwtask.notification.service.NotificationService;
import org.example.hwtask.tag.service.TagService;
import org.example.hwtask.task.dto.request.CreateTaskRequest;
import org.example.hwtask.task.dto.request.UpdateTaskRequest;
import org.example.hwtask.task.persistence.Task;
import org.example.hwtask.task.persistence.TaskMemberRepository;
import org.example.hwtask.task.persistence.TaskRepository;
import org.example.hwtask.task.persistence.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultTaskServiceTest {

    @Mock
    TaskRepository taskRepository;

    @Mock
    TaskMemberRepository taskMemberRepository;

    @Mock
    ProjectMemberRepository projectMemberRepository;

    @Mock
    AccessControlService accessControlService;

    @Mock
    ActivityRecorder activityRecorder;

    @Mock
    AutomationRuleProcessor automationRuleProcessor;

    @Mock
    NotificationService notificationService;

    @Mock
    TagService tagService;

    DefaultTaskService service;

    @BeforeEach
    void setUp() {
        service = new DefaultTaskService(
                taskRepository,
                taskMemberRepository,
                projectMemberRepository,
                accessControlService,
                activityRecorder,
                automationRuleProcessor,
                notificationService,
                tagService
        );
        doNothing().when(accessControlService).assertProjectMember(any(), any());
        when(tagService.tagsGroupedByTaskId(any())).thenReturn(Map.of());
        when(taskMemberRepository.findByIdTaskId(any())).thenReturn(List.of());
    }

    @Test
    void createUsesTodoWhenStatusNull() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(projectMemberRepository.existsByIdProjectIdAndIdUserId(any(), any())).thenReturn(true);
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        service.create(userId, new CreateTaskRequest(
                projectId,
                null,
                null,
                null,
                List.of(),
                List.of(),
                "Hello",
                null,
                null,
                null,
                List.of()
        ));

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(TaskStatus.TODO);
    }

    @Test
    void getThrowsWhenTaskMissing() {
        UUID id = UUID.randomUUID();
        when(taskRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(UUID.randomUUID(), id))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void listFallsBackToCreatedAtDescWhenSortOnlyHasInvalidProperty() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Pageable incoming = PageRequest.of(0, 20, Sort.by("string"));
        when(taskRepository.findAll(any(Specification.class), any(Pageable.class))).thenAnswer(inv -> {
            Pageable safe = inv.getArgument(1);
            assertThat(safe.getSort().getOrderFor("createdAt")).isNotNull();
            assertThat(safe.getSort().getOrderFor("createdAt").getDirection()).isEqualTo(Sort.Direction.DESC);
            return new PageImpl<Task>(List.of(), safe, 0);
        });

        service.list(userId, projectId, incoming, null, null);
    }

    @Test
    void updateThrowsWhenTaskMissing() {
        UUID id = UUID.randomUUID();
        when(taskRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(UUID.randomUUID(), id,
                new UpdateTaskRequest("t", null, TaskStatus.DONE, null, null, null, null, null)))
                .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    void deleteThrowsWhenTaskMissing() {
        UUID id = UUID.randomUUID();
        when(taskRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(UUID.randomUUID(), id))
                .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    void deleteSoftDeletesWhenPresent() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Task task = new Task(UUID.randomUUID(), null, null, userId, null, "x", null, TaskStatus.TODO, null);
        when(taskRepository.findById(id)).thenReturn(Optional.of(task));
        when(taskRepository.countByParentTaskId(id)).thenReturn(0L);

        service.delete(userId, id);

        verify(taskRepository).delete(task);
    }
}
