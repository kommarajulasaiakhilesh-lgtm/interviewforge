package com.interviewforge.mockinterview;

import com.interviewforge.questionbank.Difficulty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class MockInterviewDtos {
    private MockInterviewDtos() { }

    public record CreateRequest(@NotNull UUID roleId, @NotNull @Min(1) @Max(20) Integer questionCount) { }
    public record SubmitAnswerRequest(@NotNull UUID questionId, @NotBlank @Size(max = 10000) String answerText,
            @NotNull @Min(1) @Max(5) Integer selfRating) { }
    public record SessionSummary(UUID sessionId, UUID roleId, String roleName, MockInterviewStatus status,
            int questionCount, int answeredCount, Instant startedAt, Instant completedAt) { }
    public record QuestionPrompt(UUID questionId, int position, String skillName, String title,
            String questionText, Difficulty difficulty) { }
    public record CreateResponse(SessionSummary session, List<QuestionPrompt> questions) { }
    public record AnswerResponse(SessionSummary session, UUID questionId, int selfRating) { }
    public record AnswerDetail(UUID questionId, int position, String skillName, String title, String questionText,
            Difficulty difficulty, String answerText, Integer selfRating, Instant answeredAt,
            String evaluationCriteria, String followUpPrompt) { }
    public record SkillResult(UUID skillId, String skillName, int importance, int questionsAnswered,
            int questionCount, BigDecimal completionPercent, BigDecimal selfRatingAverage) { }
    public record SessionDetail(SessionSummary session, List<AnswerDetail> questions, List<SkillResult> skillResults,
            String assessmentMethod) { }
}
