package com.interviewforge.practice;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PracticeSessionRepository extends JpaRepository<PracticeSession, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from PracticeSession s where s.id=:id and s.userId=:userId")
    Optional<PracticeSession> findOwnedForUpdate(@Param("id") UUID id, @Param("userId") UUID userId);
    Optional<PracticeSession> findByIdAndUserId(UUID id, UUID userId);
    Page<PracticeSession> findAllByUserIdOrderByStartedAtDesc(UUID userId, Pageable pageable);
}
