package org.example.hwtaskbackend.task.service;

import org.example.hwtaskbackend.task.dto.request.CreateTaskRequest;
import org.example.hwtaskbackend.task.dto.request.UpdateTaskRequest;
import org.example.hwtaskbackend.task.persistence.Task;
import org.example.hwtaskbackend.task.persistence.TaskRepository;
import org.example.hwtaskbackend.task.persistence.TaskStatus;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultTaskServiceTest {

    @Mock
    TaskRepository taskRepository;

    DefaultTaskService service;

    @BeforeEach
    void setUp() {
        service = new DefaultTaskService(taskRepository);
    }

    @Test
    void createUsesTodoWhenStatusNull() {
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        service.create(new CreateTaskRequest("Hello", null, null, null));

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(TaskStatus.TODO);
    }

    @Test
    void getThrowsWhenTaskMissing() {
        UUID id = UUID.randomUUID();
        when(taskRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(id))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void listFallsBackToCreatedAtDescWhenSortOnlyHasInvalidProperty() {
        Pageable incoming = PageRequest.of(0, 20, Sort.by("string"));
        when(taskRepository.findAll(any(Pageable.class))).thenAnswer(inv -> {
            Pageable safe = inv.getArgument(0);
            assertThat(safe.getSort().getOrderFor("createdAt")).isNotNull();
            assertThat(safe.getSort().getOrderFor("createdAt").getDirection()).isEqualTo(Sort.Direction.DESC);
            return new PageImpl<Task>(List.of(), safe, 0);
        });

        service.list(incoming);
    }

    @Test
    void listKeepsAllowedSortOrders() {
        Pageable incoming = PageRequest.of(1, 10, Sort.by(Sort.Direction.ASC, "title"));
        when(taskRepository.findAll(any(Pageable.class))).thenAnswer(inv -> {
            Pageable safe = inv.getArgument(0);
            assertThat(safe.getPageNumber()).isEqualTo(1);
            assertThat(safe.getPageSize()).isEqualTo(10);
            assertThat(safe.getSort().getOrderFor("title").getDirection()).isEqualTo(Sort.Direction.ASC);
            return new PageImpl<Task>(List.of(), safe, 0);
        });

        service.list(incoming);
    }

    @Test
    void updateThrowsWhenTaskMissing() {
        UUID id = UUID.randomUUID();
        when(taskRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(id, new UpdateTaskRequest("t", null, TaskStatus.DONE, null)))
                .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    void deleteThrowsWhenTaskMissing() {
        UUID id = UUID.randomUUID();
        when(taskRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(id))
                .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    void deleteCallsRepositoryWhenPresent() {
        UUID id = UUID.randomUUID();
        when(taskRepository.existsById(id)).thenReturn(true);

        service.delete(id);

        verify(taskRepository).deleteById(id);
    }
}
