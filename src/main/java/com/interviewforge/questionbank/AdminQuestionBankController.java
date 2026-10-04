package com.interviewforge.questionbank;

import com.interviewforge.questionbank.QuestionBankDtos.CatalogInput;
import com.interviewforge.questionbank.QuestionBankDtos.QuestionDetail;
import com.interviewforge.questionbank.QuestionBankDtos.QuestionInput;
import com.interviewforge.questionbank.QuestionBankDtos.PageResponse;
import com.interviewforge.questionbank.QuestionBankDtos.TagResponse;
import com.interviewforge.questionbank.QuestionBankDtos.TopicResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminQuestionBankController {
    private final QuestionBankService service;
    public AdminQuestionBankController(QuestionBankService service) { this.service = service; }

    @GetMapping("/topics")
    public java.util.List<TopicResponse> listTopics() { return service.adminListTopics(); }
    @PostMapping("/topics")
    @ResponseStatus(HttpStatus.CREATED)
    public TopicResponse createTopic(@Valid @RequestBody CatalogInput input) { return service.createTopic(input); }
    @PutMapping("/topics/{id}")
    public TopicResponse updateTopic(@PathVariable UUID id, @Valid @RequestBody CatalogInput input) { return service.updateTopic(id, input); }
    @PostMapping("/tags")
    @ResponseStatus(HttpStatus.CREATED)
    public TagResponse createTag(@Valid @RequestBody CatalogInput input) { return service.createTag(input); }
    @GetMapping("/tags")
    public java.util.List<TagResponse> listTags() { return service.adminListTags(); }
    @PutMapping("/tags/{id}")
    public TagResponse updateTag(@PathVariable UUID id, @Valid @RequestBody CatalogInput input) { return service.updateTag(id, input); }
    @PostMapping("/questions")
    @ResponseStatus(HttpStatus.CREATED)
    public QuestionDetail createQuestion(@Valid @RequestBody QuestionInput input) { return service.createQuestion(input); }
    @GetMapping("/questions")
    public PageResponse<QuestionDetail> listQuestions(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        if (page < 0 || size < 1 || size > 100) throw new IllegalArgumentException("page must be non-negative and size must be between 1 and 100.");
        return service.adminBrowse(page, size);
    }
    @GetMapping("/questions/{id}")
    public QuestionDetail getQuestion(@PathVariable UUID id) { return service.adminGet(id); }
    @PutMapping("/questions/{id}")
    public QuestionDetail updateQuestion(@PathVariable UUID id, @Valid @RequestBody QuestionInput input) { return service.updateQuestion(id, input); }
    @DeleteMapping("/questions/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void archiveQuestion(@PathVariable UUID id) { service.archiveQuestion(id); }
}
