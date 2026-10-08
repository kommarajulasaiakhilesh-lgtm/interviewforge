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
            @Size(max = 10) List<@Size(max = 2000) String> optionExplanations,
            @Size(max = 10000) String theoryNotes,
            @Size(max = 10000) String workplaceExample,
            @Size(max = 160) String misconceptionLabel,
            @Size(max = 10000) String scenarioContext,
            @Size(max = 40) String interviewStage,
            @Size(max = 30) String sourceType,
            @Size(max = 200) String sourceLabel,
            @Size(max = 1000) String sourceUrl,
            Instant sourceVerifiedAt,
            @Size(max = 10000) String evaluationCriteria,
            @Size(max = 10000) String followUpPrompt,
            @Size(max = 20) List<@NotNull UUID> tagIds,
            Boolean published) { }

    public record QuestionSummary(UUID id, String title, String questionText, Difficulty difficulty,
                                  QuestionType type, List<String> options, TopicResponse topic,
                                  List<TagResponse> tags, Instant createdAt, String scenarioContext,
                                  String interviewStage, String sourceType, String sourceLabel,
                                  String sourceUrl, Instant sourceVerifiedAt) { }
    public record QuestionDetail(UUID id, String title, String questionText, Difficulty difficulty,
                                 QuestionType type, List<String> options, TopicResponse topic,
                                 List<TagResponse> tags, Instant createdAt, Instant updatedAt,
                                 boolean published, Integer correctOptionIndex, String answerText, String explanation,
                                 List<String> optionExplanations, String theoryNotes, String workplaceExample,
                                 String misconceptionLabel, String scenarioContext, String interviewStage, String sourceType,
                                 String sourceLabel, String sourceUrl, Instant sourceVerifiedAt,
                                 String evaluationCriteria, String followUpPrompt) { }
    public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) { }
}
