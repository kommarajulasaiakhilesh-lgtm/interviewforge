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
}
