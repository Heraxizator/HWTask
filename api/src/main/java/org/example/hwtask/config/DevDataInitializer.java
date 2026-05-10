package org.example.hwtask.config;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile({"dev", "docker", "test"})
public class DevDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DevDataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public DevDataInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            OrganizationRepository organizationRepository,
            OrganizationMemberRepository organizationMemberRepository,
            ProjectRepository projectRepository,
            ProjectMemberRepository projectMemberRepository
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.organizationRepository = organizationRepository;
        this.organizationMemberRepository = organizationMemberRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            return;
        }
        User demo = new User(
                "demo@hwtask.local",
                passwordEncoder.encode("demo"),
                "Демо пользователь"
        );
        userRepository.save(demo);

        Organization org = new Organization("Демо-компания");
        organizationRepository.save(org);
        organizationMemberRepository.save(new OrganizationMember(org.getId(), demo.getId(), OrgRole.OWNER));

        Project project = new Project(org.getId(), "Основной проект");
        projectRepository.save(project);
        projectMemberRepository.save(new ProjectMember(project.getId(), demo.getId(), ProjectRole.MANAGER));

        log.info("Создан демо-пользователь demo@hwtask.local / demo и проект {}", project.getId());
    }
}
