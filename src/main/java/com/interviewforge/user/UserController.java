package com.interviewforge.user;

import com.interviewforge.auth.AppPrincipal;
import com.interviewforge.auth.AuthDtos.UpdateProfileRequest;
import com.interviewforge.auth.AuthDtos.UserResponse;
import com.interviewforge.auth.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/me")
public class UserController {

    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping
    public UserResponse getProfile(@AuthenticationPrincipal AppPrincipal principal) {
        return authService.getProfile(principal);
    }

    @PatchMapping
    public UserResponse updateProfile(
            @AuthenticationPrincipal AppPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest request) {
        return authService.updateProfile(principal, request.displayName());
    }
}
