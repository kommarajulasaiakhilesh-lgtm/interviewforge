package com.interviewforge.preparation;

import com.interviewforge.progress.ProgressProjection.TopicRow;
import com.interviewforge.progress.ProgressRepository;
import com.interviewforge.questionbank.QuestionRepository;
import com.interviewforge.questionbank.ResourceNotFoundException;
import com.interviewforge.questionbank.Topic;
import com.interviewforge.questionbank.TopicRepository;
import com.interviewforge.user.UserProfile;
import com.interviewforge.user.UserProfileRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.interviewforge.preparation.JobRequirementEvidenceDtos.*;

@Service
@Transactional(readOnly = true)
public class JobRequirementEvidenceService {
    private static final int MINIMUM_ATTEMPTS = 5;

    private final UserProfileRepository profiles;
    private final CompanyRoleRepository roles;
    private final CompanyRepository companies;
    private final RoleSkillMappingRepository roleSkills;
    private final SkillRepository skills;
    private final SkillTopicMappingRepository skillTopics;
    private final TopicRepository topics;
    private final QuestionRepository questions;
    private final ProgressRepository progress;

    public JobRequirementEvidenceService(UserProfileRepository profiles, CompanyRoleRepository roles,
            CompanyRepository companies, RoleSkillMappingRepository roleSkills, SkillRepository skills,
            SkillTopicMappingRepository skillTopics, TopicRepository topics, QuestionRepository questions,
            ProgressRepository progress) {
        this.profiles = profiles;
        this.roles = roles;
        this.companies = companies;
        this.roleSkills = roleSkills;
        this.skills = skills;
        this.skillTopics = skillTopics;
        this.topics = topics;
        this.questions = questions;
        this.progress = progress;
    }

    public EvidenceMap get(UUID userId) {
        UserProfile profile = profiles.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile", userId));
        UUID roleId = profile.getTargetRoleId();
        if (roleId == null) throw new IllegalArgumentException("Choose a target role before building your job-requirement map.");
        if (profile.getJobDescription() == null || profile.getJobDescription().isBlank())
            throw new IllegalArgumentException("Save a job description in your preparation goals before building the evidence map.");

        CompanyRole role = roles.findById(roleId).filter(CompanyRole::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Active role", roleId));
        Company company = companies.findById(role.getCompanyId()).filter(Company::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Active role", roleId));
        Map<UUID, TopicRow> history = new HashMap<>();
        progress.topics(userId).forEach(row -> history.put(row.getTopicId(), row));

        List<SkillMapping> mappings = roleSkills.findAllByRoleIdOrderByImportanceDesc(roleId).stream()
                .map(mapping -> skills.findById(mapping.getSkillId()).filter(Skill::isActive)
                        .map(skill -> new SkillMapping(skill, mapping.getImportance())).orElse(null))
                .filter(java.util.Objects::nonNull).toList();

        List<RequirementEvidence> result = new ArrayList<>();
        int mappedCount = 0;
        for (String requirement : JobRequirementMatcher.extract(profile.getJobDescription())) {
            List<SkillEvidence> matchingSkills = mappings.stream()
                    .filter(mapping -> JobRequirementMatcher.matches(requirement, mapping.skill().getSlug(), mapping.skill().getName()))
                    .sorted(Comparator.comparingInt(SkillMapping::importance).reversed())
                    .map(mapping -> skillEvidence(mapping, history)).toList();
            if (!matchingSkills.isEmpty()) mappedCount++;
            result.add(new RequirementEvidence(requirement, matchingSkills.isEmpty() ? "NOT_MAPPED" : "MAPPED", matchingSkills));
        }

        return new EvidenceMap(role.getId(), role.getName(), company.getName(), result.size(), mappedCount,
                result.size() - mappedCount, List.copyOf(result),
                "Transparent phrase matching against the selected role's mapped skills; no AI or external service is used.",
                "Matches are suggestions, not a complete interpretation of the job description. Practice history shows InterviewForge activity, not proof of professional experience. Review unmapped requirements manually.");
    }

    private SkillEvidence skillEvidence(SkillMapping mapping, Map<UUID, TopicRow> history) {
        List<TopicEvidence> topicEvidence = new ArrayList<>();
        for (SkillTopicMapping topicMapping : skillTopics.findAllBySkillId(mapping.skill().getId())) {
            Topic topic = topics.findById(topicMapping.getTopicId()).filter(Topic::isActive).orElse(null);
            if (topic == null) continue;
            TopicRow row = history.get(topic.getId());
            long answered = row == null || row.getQuestionsAnswered() == null ? 0 : row.getQuestionsAnswered();
            long correct = row == null || row.getCorrectAnswers() == null ? 0 : row.getCorrectAnswers();
            BigDecimal accuracy = answered == 0 ? null : BigDecimal.valueOf(correct * 100L)
                    .divide(BigDecimal.valueOf(answered), 2, RoundingMode.HALF_UP);
            topicEvidence.add(new TopicEvidence(topic.getId(), topic.getName(), topicMapping.getRelevance(),
                    Math.toIntExact(questions.countByTopic_IdAndPublishedTrueAndArchivedFalse(topic.getId())),
                    answered, accuracy, answered >= MINIMUM_ATTEMPTS));
        }
        String practiceStatus = topicEvidence.stream().anyMatch(topic -> topic.questionsAnswered() > 0)
                ? "HAS_PRACTICE_HISTORY" : "NO_PRACTICE_HISTORY";
        return new SkillEvidence(mapping.skill().getId(), mapping.skill().getName(), mapping.importance(), practiceStatus,
                List.copyOf(topicEvidence));
    }

    private record SkillMapping(Skill skill, int importance) { }
}
