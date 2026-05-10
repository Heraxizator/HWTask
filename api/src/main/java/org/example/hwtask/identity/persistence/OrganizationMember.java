package org.example.hwtask.identity.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "organization_members")
public class OrganizationMember {

    @EmbeddedId
    private OrganizationMemberId id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private OrgRole role;

    protected OrganizationMember() {
    }

    public OrganizationMember(UUID organizationId, UUID userId, OrgRole role) {
        this.id = new OrganizationMemberId(organizationId, userId);
        this.role = role;
    }

    public OrganizationMemberId getId() {
        return id;
    }

    public OrgRole getRole() {
        return role;
    }

    public void setRole(OrgRole role) {
        this.role = role;
    }
}
