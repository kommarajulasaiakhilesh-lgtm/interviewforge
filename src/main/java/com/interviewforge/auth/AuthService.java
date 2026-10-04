package com.interviewforge.auth;

import com.interviewforge.auth.AuthDtos.AuthResponse;
import com.interviewforge.auth.AuthDtos.LoginRequest;
import com.interviewforge.auth.AuthDtos.RegisterRequest;
import com.interviewforge.auth.AuthDtos.UserResponse;
import com.interviewforge.user.UserAccount;
import com.interviewforge.user.UserAccountRepository;
import com.interviewforge.user.UserProfile;
import com.interviewforge.user.UserProfileRepository;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserAccountRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthSessionService sessionService;

    public AuthService(
            UserAccountRepository userRepository,
            UserProfileRepository profileRepository,
            PasswordEncoder passwordEncoder,
            AuthSessionService sessionService) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessionService = sessionService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException();
        }
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new PasswordTooLongException();
        }

        UserAccount user = userRepository.save(new UserAccount(email, passwordEncoder.encode(request.password())));
        UserProfile profile = profileRepository.save(new UserProfile(user, request.displayName().trim()));
        return createAuthResponse(user, profile);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        UserAccount user = userRepository.findByEmail(normalizeEmail(request.email()))
                .filter(UserAccount::isEnabled)
                .filter(account -> passwordEncoder.matches(request.password(), account.getPasswordHash()))
                .orElseThrow(InvalidCredentialsException::new);
        UserProfile profile = profileRepository.findById(user.getId()).orElseThrow();
        return createAuthResponse(user, profile);
    }

    @Transactional(readOnly = true)
    public UserResponse getProfile(AppPrincipal principal) {
        UserAccount user = userRepository.findById(principal.userId()).orElseThrow();
        UserProfile profile = profileRepository.findById(user.getId()).orElseThrow();
        return toUserResponse(user, profile);
    }

    @Transactional
    public UserResponse updateProfile(AppPrincipal principal, String displayName) {
        UserAccount user = userRepository.findById(principal.userId()).orElseThrow();
        UserProfile profile = profileRepository.findById(user.getId()).orElseThrow();
        profile.updateDisplayName(displayName.trim());
        return toUserResponse(user, profile);
    }

    private AuthResponse createAuthResponse(UserAccount user, UserProfile profile) {
        IssuedSession session = sessionService.issue(user);
        return new AuthResponse(session.accessToken(), "Bearer", session.expiresAt(), toUserResponse(user, profile));
    }

    private UserResponse toUserResponse(UserAccount user, UserProfile profile) {
        return new UserResponse(user.getId(), user.getEmail(), user.getRole(), profile.getDisplayName(), user.getCreatedAt());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
