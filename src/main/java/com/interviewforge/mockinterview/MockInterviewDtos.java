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

    public record CreateRequest(@NotNull UUID roleId, @NotNull @Min(1) @Max(20) Integer questionCount,
            InterviewMode mode) { }
    public record SubmitAnswerRequest(@NotNull UUID questionId, @NotBlank @Size(max = 10000) String answerText,
            @NotNull @Min(1) @Max(5) Integer selfRating, Boolean transcriptConsent) { }
    public record SubmitFollowUpRequest(@NotBlank @Size(max = 5000) String answerText, Boolean transcriptConsent) { }
    public record SessionSummary(UUID sessionId, UUID roleId, String roleName, MockInterviewStatus status,
            int questionCount, int answeredCount, Instant startedAt, Instant completedAt,
            InterviewMode mode, boolean audioStored) { }
    public record QuestionPrompt(UUID questionId, int position, String skillName, String title,
            String questionText, Difficulty difficulty) { }
    public record CreateResponse(SessionSummary session, List<QuestionPrompt> questions) { }
    public record AnswerResponse(SessionSummary session, UUID questionId, int selfRating) { }
    public record FollowUpResponse(UUID sessionId, UUID questionId, Instant answeredAt) { }
    public record RubricEvidence(String criterion, List<String> possibleOverlapTerms, String coachingNote) { }
    public record AnswerFeedback(int wordCount, List<String> strengths, List<String> improvements,
            List<RubricEvidence> rubricEvidence, String methodDisclosure) { }
    public record AnswerDetail(UUID questionId, int position, String skillName, String title, String questionText,
            Difficulty difficulty, String answerText, Integer selfRating, Instant answeredAt,
            String evaluationCriteria, String followUpPrompt, AnswerFeedback feedback,
            Instant transcriptConsentAt, String followUpAnswerText, Instant followUpAnsweredAt,
            Instant followUpTranscriptConsentAt) { }
    public record SkillResult(UUID skillId, String skillName, int importance, int questionsAnswered,
            int questionCount, BigDecimal completionPercent, BigDecimal selfRatingAverage) { }
    public record SessionDetail(SessionSummary session, List<AnswerDetail> questions, List<SkillResult> skillResults,
            String assessmentMethod, String privacyNotice) { }
    public record PrivacyPolicy(boolean audioUploadAccepted, boolean audioStored,
            boolean transcriptConsentRequiredForVoice, boolean transcriptDeletionAvailable,
            boolean typedFallbackAvailable, String notice) { }
}
