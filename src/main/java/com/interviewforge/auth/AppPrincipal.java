package com.interviewforge.auth;

import com.interviewforge.user.UserRole;
import java.util.UUID;

public record AppPrincipal(UUID userId, UUID sessionId, String email, UserRole role) {
}
