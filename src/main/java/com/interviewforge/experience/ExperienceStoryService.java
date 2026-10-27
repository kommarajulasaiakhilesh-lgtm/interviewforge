package com.interviewforge.experience;

import com.interviewforge.preparation.Skill;
import com.interviewforge.preparation.SkillRepository;
import com.interviewforge.questionbank.ResourceNotFoundException;
import com.interviewforge.experience.ExperienceStoryDtos.Competency;
import com.interviewforge.experience.ExperienceStoryDtos.StoryInput;
import com.interviewforge.experience.ExperienceStoryDtos.StoryResponse;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ExperienceStoryService {
    private final ExperienceStoryRepository stories;
    private final SkillRepository skills;

    public ExperienceStoryService(ExperienceStoryRepository stories, SkillRepository skills) {
        this.stories = stories;
        this.skills = skills;
    }

    @Transactional(readOnly = true)
    public List<StoryResponse> list(UUID userId) {
        return stories.findAllByUserIdOrderByUpdatedAtDesc(userId).stream().map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public StoryResponse get(UUID userId, UUID storyId) {
        return response(ownedStory(userId, storyId));
    }

    public StoryResponse create(UUID userId, StoryInput input) {
        ExperienceStory story = new ExperienceStory(userId, input.title().trim(), input.experienceType(),
                input.context(), input.situation().trim(), input.task().trim(), input.actions().trim(),
                input.result().trim(), input.learning(), resolveSkills(input.skillIds()));
        return response(stories.save(story));
    }

    public StoryResponse update(UUID userId, UUID storyId, StoryInput input) {
        ExperienceStory story = ownedStory(userId, storyId);
        story.update(input.title().trim(), input.experienceType(), input.context(), input.situation().trim(),
                input.task().trim(), input.actions().trim(), input.result().trim(), input.learning(),
                resolveSkills(input.skillIds()));
        return response(story);
    }

    public void delete(UUID userId, UUID storyId) {
        stories.delete(ownedStory(userId, storyId));
    }

    @Transactional(readOnly = true)
    public List<Competency> competencies() {
        return skills.findAllByActiveTrueOrderByNameAsc().stream().map(this::competency).toList();
    }

    private Set<Skill> resolveSkills(List<UUID> skillIds) {
        if (skillIds.stream().distinct().count() != skillIds.size())
            throw new IllegalArgumentException("Duplicate competency IDs are not allowed.");
        List<Skill> found = skills.findAllById(skillIds);
        if (found.size() != skillIds.size()) {
            Set<UUID> foundIds = found.stream().map(Skill::getId).collect(java.util.stream.Collectors.toSet());
            UUID missingId = skillIds.stream().filter(id -> !foundIds.contains(id)).findFirst().orElse(null);
            throw new ResourceNotFoundException("Competency", missingId);
        }
        if (found.stream().anyMatch(skill -> !skill.isActive()))
            throw new IllegalArgumentException("Only active competencies can be attached to a story.");
        return new LinkedHashSet<>(found);
    }

    private ExperienceStory ownedStory(UUID userId, UUID storyId) {
        return stories.findByIdAndUserId(storyId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Experience story", storyId));
    }

    private StoryResponse response(ExperienceStory story) {
        return new StoryResponse(story.getId(), story.getTitle(), story.getExperienceType(), story.getContext(),
                story.getSituation(), story.getTask(), story.getActions(), story.getResult(), story.getLearning(),
                story.getSkills().stream().map(this::competency).toList(), story.getCreatedAt(), story.getUpdatedAt());
    }

    private Competency competency(Skill skill) {
        return new Competency(skill.getId(), skill.getName(), skill.getDescription());
    }
}
