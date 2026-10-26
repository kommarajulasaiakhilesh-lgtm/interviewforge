package com.interviewforge.preparation;

import com.interviewforge.auth.AppPrincipal;
import com.interviewforge.preparation.JobRequirementEvidenceDtos.EvidenceMap;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/me/preparation/evidence-map")
public class JobRequirementEvidenceController {
    private final JobRequirementEvidenceService service;

    public JobRequirementEvidenceController(JobRequirementEvidenceService service) {
        this.service = service;
    }

    @GetMapping
    public EvidenceMap get(@AuthenticationPrincipal AppPrincipal principal) {
        UUID userId = principal.userId();
        return service.get(userId);
    }
}
