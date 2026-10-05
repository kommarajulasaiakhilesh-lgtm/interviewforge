package com.interviewforge.mockinterview;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MockInterviewSessionRepository extends JpaRepository<MockInterviewSession, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from MockInterviewSession s where s.id=:id and s.userId=:userId")
    Optional<MockInterviewSession> findOwnedForUpdate(@Param("id") UUID id, @Param("userId") UUID userId);
    Optional<MockInterviewSession> findByIdAndUserId(UUID id, UUID userId);
    Page<MockInterviewSession> findAllByUserIdOrderByStartedAtDesc(UUID userId, Pageable pageable);
}
