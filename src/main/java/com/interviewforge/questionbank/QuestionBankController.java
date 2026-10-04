package com.interviewforge.questionbank;

import com.interviewforge.questionbank.QuestionBankDtos.PageResponse;
import com.interviewforge.questionbank.QuestionBankDtos.QuestionSummary;
import com.interviewforge.questionbank.QuestionBankDtos.TagResponse;
import com.interviewforge.questionbank.QuestionBankDtos.TopicResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class QuestionBankController {
    private final QuestionBankService service;
    public QuestionBankController(QuestionBankService service) { this.service = service; }

    @GetMapping("/topics")
    public List<TopicResponse> topics() { return service.listTopics(); }

    @GetMapping("/tags")
    public List<TagResponse> tags() { return service.listTags(); }

    @GetMapping("/questions")
    public PageResponse<QuestionSummary> questions(
            @RequestParam(required = false) UUID topicId,
            @RequestParam(required = false) UUID tagId,
            @RequestParam(required = false) Difficulty difficulty,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (page < 0 || size < 1 || size > 100) throw new IllegalArgumentException("page must be non-negative and size must be between 1 and 100.");
        return service.browse(topicId, tagId, difficulty, search, page, size);
    }

    @GetMapping("/questions/{id}")
    public QuestionSummary question(@PathVariable UUID id) { return service.getPublishedQuestion(id); }
}
