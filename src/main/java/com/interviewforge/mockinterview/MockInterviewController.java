package com.interviewforge.mockinterview;

import com.interviewforge.auth.AppPrincipal;
import com.interviewforge.mockinterview.MockInterviewDtos.*;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/mock-interviews")
public class MockInterviewController {
    private final MockInterviewService service;
    public MockInterviewController(MockInterviewService service) { this.service = service; }

    @PostMapping
    public CreateResponse create(@AuthenticationPrincipal AppPrincipal principal, @Valid @RequestBody CreateRequest request) {
        return service.create(principal.userId(), request);
    }

    @PostMapping("/{sessionId}/answers")
    public AnswerResponse answer(@AuthenticationPrincipal AppPrincipal principal, @PathVariable UUID sessionId,
            @Valid @RequestBody SubmitAnswerRequest request) {
        return service.submit(principal.userId(), sessionId, request);
    }

    @GetMapping("/{sessionId}")
    public SessionDetail detail(@AuthenticationPrincipal AppPrincipal principal, @PathVariable UUID sessionId) {
        return service.detail(principal.userId(), sessionId);
    }

    @GetMapping
    public com.interviewforge.questionbank.QuestionBankDtos.PageResponse<SessionSummary> history(
            @AuthenticationPrincipal AppPrincipal principal, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.history(principal.userId(), page, size);
    }
}
