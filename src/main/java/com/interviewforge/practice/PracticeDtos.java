package com.interviewforge.practice;

import com.interviewforge.questionbank.Difficulty;
import com.interviewforge.questionbank.QuestionType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class PracticeDtos {
    private PracticeDtos() { }
    public record CreateSessionRequest(UUID topicId, Difficulty difficulty, QuestionType type,
            @NotNull @Min(1) @Max(20) Integer questionCount) { }
    public record SubmitAnswerRequest(@NotNull UUID questionId, Integer selectedOptionIndex,
            @Size(max = 10000) String answerText, @Min(1) @Max(5) Integer confidenceRating) { }
    public record QuestionPrompt(UUID questionId, int position, String title, String questionText,
            Difficulty difficulty, QuestionType type, List<String> options, String scenarioContext, String interviewStage,
            String sourceType, String sourceLabel, String sourceUrl, Instant sourceVerifiedAt) { }
    public record SessionSummary(UUID sessionId, PracticeSessionStatus status, Instant startedAt, Instant completedAt,
            QuestionType type, int questionCount, int answeredCount, int correctCount, BigDecimal scorePercent) { }
    public record CreateSessionResponse(SessionSummary session, List<QuestionPrompt> questions) { }
    public record AnswerResponse(UUID sessionId, UUID questionId, PracticeSessionStatus status, Boolean correct,
            Integer correctOptionIndex, String answerText, String explanation, Instant answeredAt,
            int answeredCount, int questionCount, BigDecimal scorePercent, List<String> optionExplanations,
            String theoryNotes, String workplaceExample, String misconceptionLabel, Integer confidenceRating) { }
    public record QuestionReview(UUID questionId, int position, String title, String questionText, Difficulty difficulty,
            QuestionType type, List<String> options, Integer selectedOptionIndex, String submittedAnswerText,
            Boolean correct, Integer correctOptionIndex, String answerText, String explanation, Instant answeredAt,
            List<String> optionExplanations, String theoryNotes, String workplaceExample, String misconceptionLabel,
            String scenarioContext, String interviewStage, String sourceType, String sourceLabel, String sourceUrl,
            Instant sourceVerifiedAt, Integer confidenceRating) { }
    public record ReviewQueueItem(UUID questionId, String title, String questionText, Difficulty difficulty,
            List<String> options, Instant dueAt, int intervalDays, Integer lastConfidence, String misconceptionLabel,
            String sourceType, String sourceLabel, String sourceUrl) { }
    public record SessionDetail(SessionSummary session, List<QuestionReview> questions) { }
}
