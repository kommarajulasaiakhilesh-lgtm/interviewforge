package com.interviewforge.experience;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExperienceStoryRepository extends JpaRepository<ExperienceStory, UUID> {
    List<ExperienceStory> findAllByUserIdOrderByUpdatedAtDesc(UUID userId);
    Optional<ExperienceStory> findByIdAndUserId(UUID id, UUID userId);
}
