package com.interviewforge.progress;

import com.interviewforge.auth.AppPrincipal;
import com.interviewforge.progress.ProgressDtos.Overview;
import com.interviewforge.progress.ProgressDtos.TopicProgress;
import com.interviewforge.progress.ProgressDtos.LearningInsights;
import com.interviewforge.practice.PracticeDtos.SessionSummary;
import com.interviewforge.questionbank.QuestionBankDtos.PageResponse;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/progress")
public class ProgressController {
    private final ProgressService service;
    public ProgressController(ProgressService service) { this.service = service; }
    @GetMapping("/overview")
    public Overview overview(@AuthenticationPrincipal AppPrincipal principal) { return service.overview(principal.userId()); }
    @GetMapping("/topics")
    public TopicProgress topics(@AuthenticationPrincipal AppPrincipal principal) { return service.topics(principal.userId()); }
    @GetMapping("/learning-insights")
    public LearningInsights learningInsights(@AuthenticationPrincipal AppPrincipal principal) { return service.learningInsights(principal.userId()); }
    @GetMapping("/attempts")
    public PageResponse<SessionSummary> attempts(@AuthenticationPrincipal AppPrincipal principal,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return service.attempts(principal.userId(), page, size);
    }
}
