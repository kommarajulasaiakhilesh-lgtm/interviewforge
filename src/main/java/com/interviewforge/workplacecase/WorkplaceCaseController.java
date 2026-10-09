package com.interviewforge.workplacecase;

import com.interviewforge.auth.AppPrincipal;
import com.interviewforge.questionbank.Difficulty;
import com.interviewforge.questionbank.QuestionBankDtos.PageResponse;
import com.interviewforge.workplacecase.WorkplaceCaseDtos.CaseSummary;
import com.interviewforge.workplacecase.WorkplaceCaseDtos.CreateSessionResponse;
import com.interviewforge.workplacecase.WorkplaceCaseDtos.DecisionRequest;
import com.interviewforge.workplacecase.WorkplaceCaseDtos.DecisionResponse;
import com.interviewforge.workplacecase.WorkplaceCaseDtos.SessionDetail;
import com.interviewforge.workplacecase.WorkplaceCaseDtos.SessionSummary;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/workplace-cases")
public class WorkplaceCaseController {
    private final WorkplaceCaseService service;
    public WorkplaceCaseController(WorkplaceCaseService service) { this.service = service; }

    @GetMapping
    public PageResponse<CaseSummary> browse(@RequestParam(required = false) UUID roleId,
            @RequestParam(required = false) UUID topicId, @RequestParam(required = false) Difficulty difficulty,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return service.browse(roleId, topicId, difficulty, page, size);
    }

    @GetMapping("/{caseId}")
    public CaseSummary get(@PathVariable UUID caseId) { return service.getPublished(caseId); }

    @PostMapping("/{caseId}/sessions")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateSessionResponse start(@AuthenticationPrincipal AppPrincipal principal, @PathVariable UUID caseId) {
        return service.start(principal.userId(), caseId);
    }

    @PostMapping("/sessions/{sessionId}/decisions")
    public DecisionResponse decide(@AuthenticationPrincipal AppPrincipal principal, @PathVariable UUID sessionId,
            @Valid @RequestBody DecisionRequest request) {
        return service.submit(principal.userId(), sessionId, request);
    }

    @GetMapping("/sessions/{sessionId}")
    public SessionDetail session(@AuthenticationPrincipal AppPrincipal principal, @PathVariable UUID sessionId) {
        return service.detail(principal.userId(), sessionId);
    }

    @GetMapping("/sessions")
    public PageResponse<SessionSummary> history(@AuthenticationPrincipal AppPrincipal principal,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return service.history(principal.userId(), page, size);
    }
}
