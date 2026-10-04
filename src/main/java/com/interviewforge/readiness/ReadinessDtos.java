package com.interviewforge.readiness;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public final class ReadinessDtos {
    private ReadinessDtos() { }

    public record ReadinessAssessment(UUID roleId, String roleName, BigDecimal readinessScore,
            BigDecimal dataCoveragePercent, int minimumAttemptsForReliableAccuracy,
            List<SkillReadiness> skills, String scoringMethod) { }

    public record SkillReadiness(UUID skillId, String skillName, int importance,
            BigDecimal readinessScore, List<TopicReadiness> topics) { }

    public record TopicReadiness(UUID topicId, String topicName, int relevance,
            BigDecimal combinedWeight, long questionsAnswered, long correctAnswers,
            BigDecimal accuracyPercent, boolean reliableSample) { }

    public record StudyPlan(UUID roleId, String roleName, int targetAccuracyPercent,
            int minimumAttemptsForReliableAccuracy, List<StudyTask> tasks) { }

    public record StudyTask(UUID topicId, String topicName, String skillName,
            int importance, int relevance, BigDecimal combinedWeight,
            long questionsAnswered, BigDecimal currentAccuracyPercent,
            int recommendedQuestions, int estimatedMinutes, int priority,
            List<String> reasons) { }
}
