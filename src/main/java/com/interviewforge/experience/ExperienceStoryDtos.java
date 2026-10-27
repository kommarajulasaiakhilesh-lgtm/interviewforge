package com.interviewforge.experience;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class ExperienceStoryDtos {
    private ExperienceStoryDtos() { }

    public record StoryInput(
            @NotBlank @Size(max = 160) String title,
            @NotNull ExperienceType experienceType,
            @Size(max = 1000) String context,
            @NotBlank @Size(max = 6000) String situation,
            @NotBlank @Size(max = 6000) String task,
            @NotBlank @Size(max = 6000) String actions,
            @NotBlank @Size(max = 6000) String result,
            @Size(max = 4000) String learning,
            @NotNull @Size(max = 10) List<@NotNull UUID> skillIds) { }

    public record Competency(UUID id, String name, String description) { }

    public record StoryResponse(UUID id, String title, ExperienceType experienceType, String context,
            String situation, String task, String actions, String result, String learning,
            List<Competency> competencies, Instant createdAt, Instant updatedAt) { }
}
