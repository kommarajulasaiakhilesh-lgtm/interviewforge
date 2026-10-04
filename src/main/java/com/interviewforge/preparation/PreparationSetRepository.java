package com.interviewforge.preparation;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
public interface PreparationSetRepository extends JpaRepository<PreparationSet,UUID> {
    List<PreparationSet> findAllByRoleIdAndPublishedTrueAndArchivedFalseOrderByTitleAsc(UUID roleId);
    List<PreparationSet> findAllByOrderByTitleAsc();
}
