package com.interviewforge.auth;

import com.interviewforge.user.UserAccount;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthSessionService {

    private static final Duration SESSION_LIFETIME = Duration.ofHours(12);
    private final SecureRandom secureRandom = new SecureRandom();
    private final AuthSessionRepository sessionRepository;

    public AuthSessionService(AuthSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Transactional
    public IssuedSession issue(UserAccount user) {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        Instant now = Instant.now();
        Instant expiresAt = now.plus(SESSION_LIFETIME);
        sessionRepository.save(new AuthSession(user, hash(rawToken), now, expiresAt));
        return new IssuedSession(rawToken, expiresAt);
    }

    @Transactional(readOnly = true)
    public Optional<AppPrincipal> authenticate(String rawToken) {
        if (rawToken == null || rawToken.isBlank() || rawToken.length() > 128) {
            return Optional.empty();
        }
        return sessionRepository.findByTokenHashAndRevokedAtIsNull(hash(rawToken))
                .filter(session -> session.getExpiresAt().isAfter(Instant.now()))
                .filter(session -> session.getUser().isEnabled())
                .map(session -> new AppPrincipal(
                        session.getUser().getId(), session.getId(), session.getUser().getEmail(), session.getUser().getRole()));
    }

    @Transactional
    public void revoke(UUID sessionId, UUID userId) {
        sessionRepository.findByIdAndUser_Id(sessionId, userId)
                .ifPresent(session -> session.revoke(Instant.now()));
    }

    private String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
