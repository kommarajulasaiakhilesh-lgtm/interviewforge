package com.interviewforge.practice;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface PracticeSessionItemRepository extends JpaRepository<PracticeSessionItem, UUID> {
    List<PracticeSessionItem> findAllBySessionIdOrderByPosition(UUID sessionId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<PracticeSessionItem> findBySessionIdAndQuestionId(UUID sessionId, UUID questionId);
}
