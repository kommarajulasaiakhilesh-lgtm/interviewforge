package com.interviewforge.workplacecase;

import com.interviewforge.questionbank.Difficulty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class WorkplaceCaseDtos {
    private WorkplaceCaseDtos() { }

    public record AdminCaseInput(
            @NotBlank @Size(max = 200) String title,
            @Size(max = 180) String slug,
            @Size(max = 1000) String description,
            @NotNull UUID roleId,
            @NotNull UUID topicId,
            @NotNull Difficulty difficulty,
            @NotNull @Min(1) @Max(240) Integer estimatedMinutes,
            @NotBlank @Size(max = 10000) String scenarioIntro,
            @Size(max = 1000) String learningObjective,
            @Size(max = 30) String sourceType,
            @Size(max = 200) String sourceLabel,
            @Size(max = 1000) String sourceUrl,
            Instant sourceVerifiedAt,
            Boolean published,
            @NotEmpty @Size(max = 100) List<@Valid NodeInput> nodes) { }

    public record NodeInput(
            @NotBlank @Pattern(regexp = "[A-Z0-9_-]{1,60}") String nodeKey,
            @NotNull CaseNodeType nodeType,
            @NotBlank @Size(max = 200) String heading,
            @NotBlank @Size(max = 10000) String situationText,
            @Size(max = 10000) String lessonText,
            @Size(max = 6) List<@Valid ChoiceInput> choices) { }

    public record ChoiceInput(
            @NotBlank @Pattern(regexp = "[A-Z0-9_-]{1,30}") String choiceKey,
            @NotBlank @Size(max = 30) String choiceLabel,
            @NotBlank @Size(max = 2000) String choiceText,
            @NotNull DecisionQuality decisionQuality,
            @NotBlank @Size(max = 10000) String explanation,
            @Size(max = 10000) String whenAppropriate,
            @Size(max = 10000) String tradeoffSummary,
            @Size(max = 160) String misconceptionLabel,
            @NotBlank @Pattern(regexp = "[A-Z0-9_-]{1,60}") String nextNodeKey) { }

    public record CaseSummary(UUID id, String title, String slug, String description, UUID roleId, String roleName,
            UUID topicId, String topicName, Difficulty difficulty, int estimatedMinutes, String learningObjective,
            String sourceType, String sourceLabel, String sourceUrl, Instant sourceVerifiedAt) { }
    public record AdminCaseDetail(CaseSummary workplaceCase, String scenarioIntro, boolean published, boolean archived,
            List<NodeInput> nodes, Instant createdAt, Instant updatedAt) { }
    public record ChoicePrompt(String choiceKey, String choiceLabel, String choiceText) { }
    public record NodePrompt(String nodeKey, CaseNodeType nodeType, String heading, String situationText,
            String lessonText, List<ChoicePrompt> choices) { }
    public record SessionSummary(UUID sessionId, UUID caseId, String caseTitle, UUID roleId, String roleName,
            Difficulty difficulty, WorkplaceCaseStatus status, int decisionCount, int totalPoints,
            BigDecimal decisionScorePercent, Instant startedAt, Instant completedAt) { }
    public record CreateSessionResponse(SessionSummary session, String scenarioIntro, String learningObjective,
            String sourceType, String sourceLabel, String sourceUrl, Instant sourceVerifiedAt, NodePrompt currentNode) { }
    public record DecisionRequest(@NotBlank @Pattern(regexp = "[A-Z0-9_-]{1,60}") String nodeKey,
            @NotBlank @Pattern(regexp = "[A-Z0-9_-]{1,30}") String choiceKey) { }
    public record DecisionReview(int position, String nodeHeading, String choiceLabel, String choiceText,
            DecisionQuality decisionQuality, int qualityPoints, String explanation, String whenAppropriate,
            String tradeoffSummary, String misconceptionLabel, String nextNodeKey, Instant answeredAt,
            List<ChoiceFeedback> choiceFeedback) { }
    public record ChoiceFeedback(String choiceKey, String choiceLabel, String choiceText, DecisionQuality decisionQuality,
            int qualityPoints, String explanation, String whenAppropriate, String tradeoffSummary,
            String misconceptionLabel, boolean selected) { }
    public record SessionDetail(SessionSummary session, String scenarioIntro, String learningObjective,
            NodePrompt currentNode, List<DecisionReview> decisions) { }
    public record DecisionResponse(SessionSummary session, DecisionReview decision, NodePrompt nextNode) { }

    public record CaseGraphSnapshot(String scenarioIntro, String learningObjective, List<NodeSnapshot> nodes) { }
    public record NodeSnapshot(String nodeKey, CaseNodeType nodeType, String heading, String situationText,
            String lessonText, List<ChoiceSnapshot> choices) { }
    public record ChoiceSnapshot(String choiceKey, String choiceLabel, String choiceText, DecisionQuality decisionQuality,
            int qualityPoints, String explanation, String whenAppropriate, String tradeoffSummary,
            String misconceptionLabel, String nextNodeKey) { }
}
