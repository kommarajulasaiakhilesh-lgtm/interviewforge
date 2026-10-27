package com.interviewforge.experience;

import com.interviewforge.auth.AppPrincipal;
import com.interviewforge.experience.ExperienceStoryDtos.Competency;
import com.interviewforge.experience.ExperienceStoryDtos.StoryInput;
import com.interviewforge.experience.ExperienceStoryDtos.StoryResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/experience-stories")
public class ExperienceStoryController {
    private final ExperienceStoryService service;

    public ExperienceStoryController(ExperienceStoryService service) {
        this.service = service;
    }

    @GetMapping
    public List<StoryResponse> list(@AuthenticationPrincipal AppPrincipal principal) {
        return service.list(principal.userId());
    }

    @GetMapping("/competencies")
    public List<Competency> competencies() {
        return service.competencies();
    }

    @GetMapping("/{storyId}")
    public StoryResponse get(@AuthenticationPrincipal AppPrincipal principal, @PathVariable UUID storyId) {
        return service.get(principal.userId(), storyId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StoryResponse create(@AuthenticationPrincipal AppPrincipal principal, @Valid @RequestBody StoryInput input) {
        return service.create(principal.userId(), input);
    }

    @PutMapping("/{storyId}")
    public StoryResponse update(@AuthenticationPrincipal AppPrincipal principal, @PathVariable UUID storyId,
            @Valid @RequestBody StoryInput input) {
        return service.update(principal.userId(), storyId, input);
    }

    @DeleteMapping("/{storyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal AppPrincipal principal, @PathVariable UUID storyId) {
        service.delete(principal.userId(), storyId);
    }
}
