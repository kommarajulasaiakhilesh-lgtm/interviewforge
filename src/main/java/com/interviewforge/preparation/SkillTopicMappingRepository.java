package com.interviewforge.preparation;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
public interface SkillTopicMappingRepository extends JpaRepository<SkillTopicMapping,UUID> {
    List<SkillTopicMapping> findAllBySkillId(UUID skillId);
    void deleteAllBySkillId(UUID skillId);
}
