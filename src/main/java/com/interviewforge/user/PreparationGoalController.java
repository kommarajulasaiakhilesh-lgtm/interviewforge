package com.interviewforge.user;

import com.interviewforge.auth.AppPrincipal;
import com.interviewforge.preparation.CompanyRoleRepository;
import com.interviewforge.questionbank.ResourceNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/me/preparation")
public class PreparationGoalController {
    private final UserProfileRepository profiles;
    private final CompanyRoleRepository roles;

    public PreparationGoalController(UserProfileRepository profiles, CompanyRoleRepository roles) {
        this.profiles = profiles;
        this.roles = roles;
    }

    @GetMapping
    public GoalResponse get(@AuthenticationPrincipal AppPrincipal principal) {
        return response(profile(principal.userId()));
    }

    @PatchMapping
    public GoalResponse update(@AuthenticationPrincipal AppPrincipal principal, @Valid @RequestBody GoalRequest request) {
        UserProfile profile = profile(principal.userId());
        if (request.targetRoleId() != null && !roles.existsActiveRole(request.targetRoleId())) {
            throw new ResourceNotFoundException("Active role", request.targetRoleId());
        }
        profile.updatePreparation(request.targetRoleId(), request.interviewDate(), request.weeklyStudyMinutes(), request.jobDescription());
        return response(profile);
    }

    private UserProfile profile(UUID userId) {
        return profiles.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User profile", userId));
    }
    private GoalResponse response(UserProfile p) {
        return new GoalResponse(p.getTargetRoleId(), p.getInterviewDate(), p.getWeeklyStudyMinutes(), p.getJobDescription());
    }

    public record GoalRequest(UUID targetRoleId, LocalDate interviewDate,
            @Min(15) @Max(1200) Integer weeklyStudyMinutes, @Size(max = 12000) String jobDescription) { }
    public record GoalResponse(UUID targetRoleId, LocalDate interviewDate, Integer weeklyStudyMinutes, String jobDescription) { }
}
