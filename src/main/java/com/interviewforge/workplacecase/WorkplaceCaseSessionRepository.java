package com.interviewforge.workplacecase;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WorkplaceCaseSessionRepository extends JpaRepository<WorkplaceCaseSession, UUID> {
    Optional<WorkplaceCaseSession> findByIdAndUserId(UUID id, UUID userId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from WorkplaceCaseSession s where s.id = :sessionId and s.userId = :userId")
    Optional<WorkplaceCaseSession> findOwnedForUpdate(@Param("sessionId") UUID sessionId, @Param("userId") UUID userId);
    org.springframework.data.domain.Page<WorkplaceCaseSession> findAllByUserIdOrderByStartedAtDesc(UUID userId,
            org.springframework.data.domain.Pageable pageable);
}
