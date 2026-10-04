package com.interviewforge.auth;

import java.time.Instant;

public record IssuedSession(String accessToken, Instant expiresAt) {
}
