package com.interviewforge.auth;

import com.interviewforge.user.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank @Email @Size(max = 320) String email,
            @NotBlank @Size(min = 12, max = 72) String password,
            @NotBlank @Size(max = 80) String displayName) {
    }

    public record LoginRequest(
            @NotBlank @Email @Size(max = 320) String email,
            @NotBlank @Size(max = 72) String password) {
    }

    public record UpdateProfileRequest(@NotBlank @Size(max = 80) String displayName) {
    }

    public record UserResponse(UUID id, String email, UserRole role, String displayName, Instant createdAt) {
    }

    public record AuthResponse(String accessToken, String tokenType, Instant expiresAt, UserResponse user) {
    }
}
