package com.interviewforge.questionbank;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class QuestionBankDtos {
    private QuestionBankDtos() { }

    public record CatalogInput(
            @NotBlank @Size(max = 100) String name,
            @Size(max = 120) String slug,
            @Size(max = 500) String description,
            Boolean active) { }

    public record TopicResponse(UUID id, String name, String slug, String description, boolean active) { }
    public record TagResponse(UUID id, String name, String slug, boolean active) { }
    public record QuestionInput(
            @NotNull UUID topicId,
            @NotBlank @Size(max = 200) String title,
            @NotBlank @Size(max = 10000) String questionText,
            @NotNull Difficulty difficulty,
            @NotNull QuestionType type,
            @Size(max = 10) List<@NotBlank @Size(max = 500) String> options,
            Integer correctOptionIndex,
            @Size(max = 10000) String answerText,
            @Size(max = 10000) String explanation,
            @Size(max = 20) List<@NotNull UUID> tagIds,
            Boolean published) { }

    public record QuestionSummary(UUID id, String title, String questionText, Difficulty difficulty,
                                  QuestionType type, List<String> options, TopicResponse topic,
                                  List<TagResponse> tags, Instant createdAt) { }
    public record QuestionDetail(UUID id, String title, String questionText, Difficulty difficulty,
                                 QuestionType type, List<String> options, TopicResponse topic,
                                 List<TagResponse> tags, Instant createdAt, Instant updatedAt,
                                 boolean published, Integer correctOptionIndex, String answerText, String explanation) { }
    public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) { }
}
