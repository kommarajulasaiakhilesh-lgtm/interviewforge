package com.interviewforge.workplacecase;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WorkplaceCaseRepository extends JpaRepository<WorkplaceCase, UUID>, JpaSpecificationExecutor<WorkplaceCase> {
    boolean existsBySlug(String slug);
    boolean existsBySlugAndIdNot(String slug, UUID id);
    Page<WorkplaceCase> findAllByOrderByCreatedAtDesc(Pageable pageable);
    @Query(value = "select w from WorkplaceCase w, CompanyRole r, Company c, Topic t "
            + "where w.roleId = r.id and r.companyId = c.id and w.topicId = t.id "
            + "and w.published = true and w.archived = false and r.active = true and c.active = true and t.active = true "
            + "and (:roleId is null or w.roleId = :roleId) and (:topicId is null or w.topicId = :topicId) "
            + "and (:difficulty is null or w.difficulty = :difficulty)",
            countQuery = "select count(w) from WorkplaceCase w, CompanyRole r, Company c, Topic t "
            + "where w.roleId = r.id and r.companyId = c.id and w.topicId = t.id "
            + "and w.published = true and w.archived = false and r.active = true and c.active = true and t.active = true "
            + "and (:roleId is null or w.roleId = :roleId) and (:topicId is null or w.topicId = :topicId) "
            + "and (:difficulty is null or w.difficulty = :difficulty)")
    Page<WorkplaceCase> findVisible(@Param("roleId") UUID roleId, @Param("topicId") UUID topicId,
            @Param("difficulty") com.interviewforge.questionbank.Difficulty difficulty, Pageable pageable);
}
