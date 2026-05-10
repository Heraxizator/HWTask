package org.example.hwtask.automation.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AutomationRuleRepository extends JpaRepository<AutomationRule, UUID> {

    List<AutomationRule> findByProjectIdAndEnabledIsTrue(UUID projectId);

    List<AutomationRule> findByProjectId(UUID projectId);
}
