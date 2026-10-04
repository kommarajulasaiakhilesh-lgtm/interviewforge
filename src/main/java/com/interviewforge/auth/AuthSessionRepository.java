package com.interviewforge.auth;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthSessionRepository extends JpaRepository<AuthSession, UUID> {

    Optional<AuthSession> findByTokenHashAndRevokedAtIsNull(String tokenHash);

    Optional<AuthSession> findByIdAndUser_Id(UUID id, UUID userId);
}
