package com.interviewforge.preparation;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
public interface SkillRepository extends JpaRepository<Skill,UUID> {
    List<Skill> findAllByActiveTrueOrderByNameAsc();
    List<Skill> findAllByOrderByNameAsc();
}
