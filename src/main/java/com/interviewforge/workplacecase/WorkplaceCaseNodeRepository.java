package com.interviewforge.workplacecase;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WorkplaceCaseNodeRepository extends JpaRepository<WorkplaceCaseNode, UUID> {
    List<WorkplaceCaseNode> findAllByCaseIdOrderByPosition(UUID caseId);
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from WorkplaceCaseNode n where n.caseId = :caseId")
    void deleteForCase(@Param("caseId") UUID caseId);
}
