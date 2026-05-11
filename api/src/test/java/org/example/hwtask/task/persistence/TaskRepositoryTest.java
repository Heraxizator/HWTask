package org.example.hwtask.task.persistence;

import org.example.hwtask.identity.persistence.OrgRole;
import org.example.hwtask.identity.persistence.Organization;
import org.example.hwtask.identity.persistence.OrganizationMember;
import org.example.hwtask.identity.persistence.OrganizationMemberRepository;
import org.example.hwtask.identity.persistence.OrganizationRepository;
import org.example.hwtask.identity.persistence.Project;
import org.example.hwtask.identity.persistence.ProjectMember;
import org.example.hwtask.identity.persistence.ProjectMemberRepository;
import org.example.hwtask.identity.persistence.ProjectRepository;
import org.example.hwtask.identity.persistence.ProjectRole;
import org.example.hwtask.identity.persistence.User;
import org.example.hwtask.identity.persistence.UserRepository;
import org.example.hwtask.support.PostgresTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.UUID;

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

    @Autowired
    UserRepository userRepository;

    @Autowired
    OrganizationRepository organizationRepository;

    @Autowired
    OrganizationMemberRepository organizationMemberRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    ProjectMemberRepository projectMemberRepository;

    private UUID projectId;

    @BeforeEach
    void seedProject() {
        User user = userRepository.save(new User("repo@test.local", "{noop}x", "Repo"));
        Organization org = organizationRepository.save(new Organization("Org"));
        organizationMemberRepository.save(new OrganizationMember(org.getId(), user.getId(), OrgRole.OWNER));
        Project project = projectRepository.save(new Project(org.getId(), "Proj"));
        projectMemberRepository.save(new ProjectMember(project.getId(), user.getId(), ProjectRole.MANAGER));
        projectId = project.getId();
    }

    @Test
    void saveAndFindById() {
        Task task = new Task(projectId, null, null, null, null, "Doc", "desc", TaskStatus.TODO, TaskPriority.MEDIUM);
        Task saved = taskRepository.save(task);

        assertThat(taskRepository.findById(saved.getId())).isPresent();
        assertThat(taskRepository.findAll()).hasSize(1);
    }

    @Test
    void softDeleteHidesTaskFromQueries() {
        Task saved = taskRepository.save(new Task(projectId, null, null, null, null, "Temp", null, TaskStatus.TODO, null));
        var id = saved.getId();

        taskRepository.deleteById(id);

        assertThat(taskRepository.findById(id)).isEmpty();
    }

    @Test
    void saveTwoTasksListsBoth() {
        taskRepository.save(new Task(projectId, null, null, null, null, "A", null, TaskStatus.TODO, null));
        taskRepository.save(new Task(projectId, null, null, null, null, "B", null, TaskStatus.IN_PROGRESS, null));

        assertThat(taskRepository.findAll()).hasSize(2);
    }
}
