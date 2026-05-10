package org.example.hwtask.identity.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "project_members")
public class ProjectMember {

    @EmbeddedId
    private ProjectMemberId id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ProjectRole role;

    protected ProjectMember() {
    }

    public ProjectMember(UUID projectId, UUID userId, ProjectRole role) {
        this.id = new ProjectMemberId(projectId, userId);
        this.role = role;
    }

    public ProjectMemberId getId() {
        return id;
    }

    public ProjectRole getRole() {
        return role;
    }

    public void setRole(ProjectRole role) {
        this.role = role;
    }
}
