package com.interviewforge.preparation;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
public interface RoleSkillMappingRepository extends JpaRepository<RoleSkillMapping,UUID> {
    List<RoleSkillMapping> findAllByRoleIdOrderByImportanceDesc(UUID roleId);
    void deleteAllByRoleId(UUID roleId);
}
