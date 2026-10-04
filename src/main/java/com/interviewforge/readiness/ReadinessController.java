package com.interviewforge.readiness;

import com.interviewforge.auth.AppPrincipal;
import com.interviewforge.readiness.ReadinessDtos.ReadinessAssessment;
import com.interviewforge.readiness.ReadinessDtos.StudyPlan;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/readiness/roles/{roleId}")
public class ReadinessController {
    private final ReadinessService service;
    public ReadinessController(ReadinessService service) { this.service = service; }

    @GetMapping
    public ReadinessAssessment assessment(@AuthenticationPrincipal AppPrincipal principal, @PathVariable UUID roleId) {
        return service.assessment(principal.userId(), roleId);
    }

    @GetMapping("/study-plan")
    public StudyPlan studyPlan(@AuthenticationPrincipal AppPrincipal principal, @PathVariable UUID roleId,
            @RequestParam(defaultValue = "10") int maxTasks) {
        return service.studyPlan(principal.userId(), roleId, maxTasks);
    }
}
