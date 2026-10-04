package com.interviewforge.progress;

import java.util.UUID;

public final class ProgressProjection {
    private ProgressProjection() { }
    public interface OverviewRow {
        Long getCompletedSessions();
        Long getInProgressSessions();
        Long getMcqQuestionsAnswered();
        Long getMcqCorrectAnswers();
    }
    public interface TopicRow {
        UUID getTopicId();
        String getTopicName();
        Long getQuestionsAnswered();
        Long getCorrectAnswers();
        Long getLastAttemptEpoch();
    }
}
