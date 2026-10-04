package com.interviewforge.practice;

import com.interviewforge.auth.AppPrincipal;
import com.interviewforge.practice.PracticeDtos.*;
import com.interviewforge.questionbank.QuestionBankDtos.PageResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/practice")
public class PracticeController {
    private final PracticeService service;
    public PracticeController(PracticeService service) { this.service = service; }
    @PostMapping("/sessions")
    public CreateSessionResponse create(@AuthenticationPrincipal AppPrincipal principal, @Valid @RequestBody CreateSessionRequest request) {
        return service.create(principal.userId(), request);
    }
    @PostMapping("/sessions/{sessionId}/answers")
    public AnswerResponse answer(@AuthenticationPrincipal AppPrincipal principal, @PathVariable UUID sessionId, @Valid @RequestBody SubmitAnswerRequest request) {
        return service.submit(principal.userId(), sessionId, request);
    }
    @GetMapping("/sessions/{sessionId}")
    public SessionDetail detail(@AuthenticationPrincipal AppPrincipal principal, @PathVariable UUID sessionId) {
        return service.detail(principal.userId(), sessionId);
    }
    @GetMapping("/sessions")
    public PageResponse<SessionSummary> history(@AuthenticationPrincipal AppPrincipal principal,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return service.history(principal.userId(), page, size);
    }
}
