package com.interviewforge.preparation;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public final class JobRequirementEvidenceDtos {
    private JobRequirementEvidenceDtos() { }

    public record EvidenceMap(UUID roleId, String roleName, String companyName, int requirementCount,
            int mappedRequirementCount, int unmappedRequirementCount, List<RequirementEvidence> requirements,
            String matchingMethod, String guidance) { }

    public record RequirementEvidence(String requirementText, String matchStatus, List<SkillEvidence> skills) { }

    public record SkillEvidence(UUID skillId, String skillName, int roleImportance, String practiceStatus,
            List<TopicEvidence> topics) { }

    public record TopicEvidence(UUID topicId, String topicName, int relevance, int publishedQuestionCount,
            long questionsAnswered, BigDecimal accuracyPercent, boolean reliableSample) { }
}
