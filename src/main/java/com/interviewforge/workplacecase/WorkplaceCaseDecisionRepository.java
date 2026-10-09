package com.interviewforge.workplacecase;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkplaceCaseDecisionRepository extends JpaRepository<WorkplaceCaseDecision, UUID> {
    List<WorkplaceCaseDecision> findAllBySessionIdOrderByPosition(UUID sessionId);
}
