package org.example.hwtaskbackend.task.persistence;

import org.example.hwtaskbackend.support.PostgresTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class TaskRepositoryTest {

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        PostgresTestSupport.registerDatasource(registry);
    }

    @Autowired
    TaskRepository taskRepository;

    @Test
    void saveAndFindById() {
        Task task = new Task("Doc", "desc", TaskStatus.TODO, TaskPriority.MEDIUM);
        Task saved = taskRepository.save(task);

        assertThat(taskRepository.findById(saved.getId())).isPresent();
        assertThat(taskRepository.findAll()).hasSize(1);
    }

    @Test
    void deleteRemovesEntity() {
        Task saved = taskRepository.save(new Task("Temp", null, TaskStatus.TODO, null));
        var id = saved.getId();

        taskRepository.deleteById(id);

        assertThat(taskRepository.findById(id)).isEmpty();
    }

    @Test
    void saveTwoTasksListsBoth() {
        taskRepository.save(new Task("A", null, TaskStatus.TODO, null));
        taskRepository.save(new Task("B", null, TaskStatus.IN_PROGRESS, null));

        assertThat(taskRepository.findAll()).hasSize(2);
    }
}
