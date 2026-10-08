package com.interviewforge.progress;

import com.interviewforge.practice.PracticeDtos.SessionSummary;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class ProgressDtos {
    private ProgressDtos() { }
    public enum TopicLevel { NOT_ENOUGH_DATA, NEEDS_WORK, DEVELOPING, STRONG }
    public record Overview(long completedSessions, long inProgressSessions, long mcqQuestionsAnswered,
            long mcqCorrectAnswers, BigDecimal accuracyPercent, List<SessionSummary> recentAttempts) { }
    public record TopicPerformance(UUID topicId, String topicName, long questionsAnswered, long correctAnswers,
            BigDecimal accuracyPercent, TopicLevel level, Instant lastAttemptAt) { }
    public record TopicProgress(int minimumAttemptsForRating, List<TopicPerformance> topics) { }
    public record ConfidencePerformance(int confidenceRating, long answered, long correct, BigDecimal accuracyPercent) { }
    public record MisconceptionPerformance(String label, long incorrectAnswers, Instant mostRecentAt) { }
    public record LearningInsights(List<ConfidencePerformance> confidenceCalibration,
            List<MisconceptionPerformance> recurringMisconceptions, int minimumAttemptsForInsight) { }
}
