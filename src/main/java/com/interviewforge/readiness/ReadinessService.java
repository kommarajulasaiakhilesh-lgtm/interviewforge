package com.interviewforge.readiness;

import com.interviewforge.preparation.*;
import com.interviewforge.progress.ProgressRepository;
import com.interviewforge.progress.ProgressProjection.TopicRow;
import com.interviewforge.questionbank.ResourceNotFoundException;
import com.interviewforge.questionbank.Topic;
import com.interviewforge.questionbank.TopicRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.interviewforge.readiness.ReadinessDtos.*;

@Service
@Transactional(readOnly = true)
public class ReadinessService {
    private static final int MINIMUM_ATTEMPTS = 5;
    private static final int TARGET_ACCURACY = 80;
    private final CompanyRoleRepository roles;
    private final CompanyRepository companies;
    private final RoleSkillMappingRepository roleSkills;
    private final SkillTopicMappingRepository skillTopics;
    private final SkillRepository skills;
    private final TopicRepository topics;
    private final ProgressRepository progress;

    public ReadinessService(CompanyRoleRepository roles, CompanyRepository companies,
            RoleSkillMappingRepository roleSkills, SkillTopicMappingRepository skillTopics,
            SkillRepository skills, TopicRepository topics, ProgressRepository progress) {
        this.roles = roles; this.companies = companies; this.roleSkills = roleSkills;
        this.skillTopics = skillTopics; this.skills = skills; this.topics = topics; this.progress = progress;
    }

    public ReadinessAssessment assessment(UUID userId, UUID roleId) {
        List<TopicRow> history = progress.topics(userId);
        Map<UUID, TopicRow> historyByTopic = new HashMap<>();
        history.forEach(row -> historyByTopic.put(row.getTopicId(), row));
        RoleData role = roleData(roleId, historyByTopic);
        BigDecimal totalWeight = role.topics().stream().map(TopicData::weight).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal scoredWeight = role.topics().stream().filter(t -> t.answered() >= MINIMUM_ATTEMPTS)
                .map(TopicData::weight).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal weightedScore = role.topics().stream().map(t -> t.weight().multiply(t.accuracy() == null ? BigDecimal.ZERO : t.accuracy()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal score = totalWeight.signum() == 0 ? null : weightedScore.divide(totalWeight, 2, RoundingMode.HALF_UP);
        BigDecimal coverage = totalWeight.signum() == 0 ? BigDecimal.ZERO
                : scoredWeight.multiply(BigDecimal.valueOf(100)).divide(totalWeight, 2, RoundingMode.HALF_UP);
        List<SkillReadiness> skillResults = role.skills().stream().map(skill -> {
            BigDecimal skillWeight = skill.topics().stream().map(TopicData::relevanceWeight).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal skillScore = skillWeight.signum() == 0 ? null : skill.topics().stream()
                    .map(t -> t.relevanceWeight().multiply(t.accuracy() == null ? BigDecimal.ZERO : t.accuracy()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add).divide(skillWeight, 2, RoundingMode.HALF_UP);
            return new SkillReadiness(skill.id(), skill.name(), skill.importance(), skillScore,
                    skill.topics().stream().map(this::topicDto).toList());
        }).toList();
        return new ReadinessAssessment(role.id(), role.name(), score, coverage, MINIMUM_ATTEMPTS, skillResults,
                "Topic accuracy is correct MCQ answers / answered MCQs. Overall readiness is the weighted average using importance × relevance; unattempted topics count as 0. Data coverage is the weight share with at least 5 answers.");
    }

    public StudyPlan studyPlan(UUID userId, UUID roleId, int maxTasks) {
        if (maxTasks < 1 || maxTasks > 20) throw new IllegalArgumentException("maxTasks must be between 1 and 20.");
        List<TopicRow> history = progress.topics(userId);
        Map<UUID, TopicRow> historyByTopic = new HashMap<>();
        history.forEach(row -> historyByTopic.put(row.getTopicId(), row));
        RoleData role = roleData(roleId, historyByTopic);
        List<StudyTask> tasks = role.topics().stream().filter(t -> t.answered() < MINIMUM_ATTEMPTS || t.accuracy().compareTo(BigDecimal.valueOf(TARGET_ACCURACY)) < 0)
                .map(this::task).sorted(Comparator.comparingInt(StudyTask::priority).reversed()
                        .thenComparing(StudyTask::topicName, String.CASE_INSENSITIVE_ORDER)).limit(maxTasks).toList();
        return new StudyPlan(role.id(), role.name(), TARGET_ACCURACY, MINIMUM_ATTEMPTS, tasks);
    }

    private RoleData roleData(UUID roleId, Map<UUID, TopicRow> history) {
        CompanyRole role = roles.findById(roleId).filter(CompanyRole::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Role", roleId));
        companies.findById(role.getCompanyId()).filter(Company::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Role", roleId));
        List<SkillData> skillData = new ArrayList<>();
        for (RoleSkillMapping mapping : roleSkills.findAllByRoleIdOrderByImportanceDesc(roleId)) {
            Skill skill = skills.findById(mapping.getSkillId()).filter(Skill::isActive).orElse(null);
            if (skill == null) continue;
            List<TopicData> topicData = new ArrayList<>();
            for (SkillTopicMapping link : skillTopics.findAllBySkillId(skill.getId())) {
                Topic topic = topics.findById(link.getTopicId()).filter(Topic::isActive).orElse(null);
                if (topic == null) continue;
                TopicRow row = history.get(topic.getId());
                long answered = row == null || row.getQuestionsAnswered() == null ? 0 : row.getQuestionsAnswered();
                long correct = row == null || row.getCorrectAnswers() == null ? 0 : row.getCorrectAnswers();
                BigDecimal accuracy = answered == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(correct * 100L)
                        .divide(BigDecimal.valueOf(answered), 2, RoundingMode.HALF_UP);
                BigDecimal relevanceWeight = BigDecimal.valueOf(link.getRelevance());
                BigDecimal weight = relevanceWeight.multiply(BigDecimal.valueOf(mapping.getImportance()));
                topicData.add(new TopicData(topic.getId(), topic.getName(), skill.getName(), mapping.getImportance(),
                        link.getRelevance(), relevanceWeight, weight, answered, correct, accuracy));
            }
            skillData.add(new SkillData(skill.getId(), skill.getName(), mapping.getImportance(), topicData));
        }
        List<TopicData> flattened = skillData.stream().flatMap(s -> s.topics().stream()).toList();
        return new RoleData(role.getId(), role.getName(), skillData, flattened);
    }

    private TopicReadiness topicDto(TopicData t) {
        return new TopicReadiness(t.id(), t.name(), t.relevance(), t.weight(), t.answered(), t.correct(),
                t.answered() == 0 ? null : t.accuracy(), t.answered() >= MINIMUM_ATTEMPTS);
    }

    private StudyTask task(TopicData t) {
        List<String> reasons = new ArrayList<>();
        if (t.answered() == 0) reasons.add("No MCQ attempts yet.");
        else if (t.answered() < MINIMUM_ATTEMPTS) reasons.add("Fewer than 5 MCQ attempts; accuracy is an early estimate.");
        if (t.answered() >= MINIMUM_ATTEMPTS && t.accuracy().compareTo(BigDecimal.valueOf(TARGET_ACCURACY)) < 0)
            reasons.add("Accuracy is below the 80% target.");
        BigDecimal normalizedGap = BigDecimal.valueOf(100).subtract(t.accuracy()).max(BigDecimal.ZERO);
        int priority = t.weight().multiply(normalizedGap).setScale(0, RoundingMode.HALF_UP).intValue();
        return new StudyTask(t.id(), t.name(), t.skillName(), t.importance(), t.relevance(), t.weight(),
                t.answered(), t.answered() == 0 ? null : t.accuracy(), 5, 30, priority, reasons);
    }

    private record RoleData(UUID id, String name, List<SkillData> skills, List<TopicData> topics) { }
    private record SkillData(UUID id, String name, int importance, List<TopicData> topics) { }
    private record TopicData(UUID id, String name, String skillName, int importance, int relevance,
            BigDecimal relevanceWeight, BigDecimal weight, long answered, long correct, BigDecimal accuracy) { }
}
