package com.interviewforge.mockinterview;

import com.interviewforge.mockinterview.MockInterviewDtos.*;
import com.interviewforge.preparation.*;
import com.interviewforge.practice.InsufficientQuestionsException;
import com.interviewforge.questionbank.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class MockInterviewService {
    private final MockInterviewSessionRepository sessions;
    private final MockInterviewItemRepository items;
    private final CompanyRoleRepository roles;
    private final CompanyRepository companies;
    private final RoleSkillMappingRepository roleSkills;
    private final SkillTopicMappingRepository skillTopics;
    private final SkillRepository skills;
    private final TopicRepository topics;
    private final QuestionRepository questions;

    public MockInterviewService(MockInterviewSessionRepository sessions, MockInterviewItemRepository items,
            CompanyRoleRepository roles, CompanyRepository companies, RoleSkillMappingRepository roleSkills,
            SkillTopicMappingRepository skillTopics, SkillRepository skills, TopicRepository topics,
            QuestionRepository questions) {
        this.sessions=sessions; this.items=items; this.roles=roles; this.companies=companies;
        this.roleSkills=roleSkills; this.skillTopics=skillTopics; this.skills=skills; this.topics=topics; this.questions=questions;
    }

    public CreateResponse create(UUID userId, CreateRequest request) {
        CompanyRole role = activeRole(request.roleId());
        List<SkillTopic> mappings = activeMappings(role.getId());
        if (mappings.isEmpty()) throw new InsufficientQuestionsException(request.questionCount(), 0);

        Map<UUID, SkillTopic> bestMappingByTopic = mappings.stream().collect(Collectors.toMap(
                SkillTopic::topicId, m -> m, (a, b) -> a.weight() >= b.weight() ? a : b));
        List<QuestionChoice> candidates = new ArrayList<>();
        for (SkillTopic mapping : bestMappingByTopic.values()) {
            for (Question question : questions.findRandomPublishedTextByTopic(mapping.topicId(), request.questionCount())) {
                candidates.add(new QuestionChoice(question, mapping));
            }
        }
        Collections.shuffle(candidates);
        List<QuestionChoice> selected = candidates.stream().limit(request.questionCount()).toList();
        if (selected.size() < request.questionCount()) throw new InsufficientQuestionsException(request.questionCount(), selected.size());

        MockInterviewSession session = sessions.save(new MockInterviewSession(userId, role.getId(), role.getName(), request.questionCount()));
        int position = 1;
        for (QuestionChoice choice : selected) {
            Question q = choice.question(); SkillTopic m = choice.mapping();
            items.save(new MockInterviewItem(session.getId(), q.getId(), m.topicId(), m.skillId(), m.skillName(),
                    m.importance(), m.relevance(), position++, q.getTitle(), q.getQuestionText(), q.getDifficulty()));
        }
        return new CreateResponse(summary(session), items.findAllBySessionIdOrderByPosition(session.getId()).stream().map(this::prompt).toList());
    }

    public AnswerResponse submit(UUID userId, UUID sessionId, SubmitAnswerRequest request) {
        MockInterviewSession session = sessions.findOwnedForUpdate(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Mock interview", sessionId));
        if (session.getStatus() != MockInterviewStatus.IN_PROGRESS) throw new IllegalArgumentException("This mock interview is already complete.");
        MockInterviewItem item = items.findBySessionIdAndQuestionId(sessionId, request.questionId())
                .orElseThrow(() -> new ResourceNotFoundException("Question in mock interview", request.questionId()));
        if (item.getAnsweredAt() != null) throw new IllegalArgumentException("This interview question has already been answered.");
        if (!StringUtils.hasText(request.answerText())) throw new IllegalArgumentException("answerText must not be blank.");
        item.submit(request.answerText().trim(), request.selfRating());
        session.recordAnswer();
        return new AnswerResponse(summary(session), item.getQuestionId(), item.getSelfRating());
    }

    @Transactional(readOnly = true)
    public SessionDetail detail(UUID userId, UUID sessionId) {
        MockInterviewSession session = owned(userId, sessionId);
        List<MockInterviewItem> sessionItems = items.findAllBySessionIdOrderByPosition(sessionId);
        Map<UUID, List<MockInterviewItem>> bySkill = sessionItems.stream().collect(Collectors.groupingBy(MockInterviewItem::getSkillId));
        List<SkillResult> results = bySkill.values().stream().map(this::skillResult)
                .sorted(Comparator.comparingInt(SkillResult::importance).reversed().thenComparing(SkillResult::skillName, String.CASE_INSENSITIVE_ORDER)).toList();
        return new SessionDetail(summary(session), sessionItems.stream().map(this::answerDetail).toList(), results,
                "Skill results report completion and the weighted average of your 1–5 self-ratings (skill importance × topic relevance). Answers are captured for review and are not automatically graded.");
    }

    @Transactional(readOnly = true)
    public com.interviewforge.questionbank.QuestionBankDtos.PageResponse<SessionSummary> history(UUID userId, int page, int size) {
        if (page < 0 || size < 1 || size > 100) throw new IllegalArgumentException("page must be non-negative and size must be between 1 and 100.");
        Page<MockInterviewSession> result = sessions.findAllByUserIdOrderByStartedAtDesc(userId, PageRequest.of(page, size));
        return new com.interviewforge.questionbank.QuestionBankDtos.PageResponse<>(result.getContent().stream().map(this::summary).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    private List<SkillTopic> activeMappings(UUID roleId) {
        List<SkillTopic> result = new ArrayList<>();
        for (RoleSkillMapping roleSkill : roleSkills.findAllByRoleIdOrderByImportanceDesc(roleId)) {
            Skill skill = skills.findById(roleSkill.getSkillId()).filter(Skill::isActive).orElse(null);
            if (skill == null) continue;
            for (SkillTopicMapping mapping : skillTopics.findAllBySkillId(skill.getId())) {
                Topic topic = topics.findById(mapping.getTopicId()).filter(Topic::isActive).orElse(null);
                if (topic != null) result.add(new SkillTopic(skill.getId(), skill.getName(), roleSkill.getImportance(),
                        topic.getId(), mapping.getRelevance(), roleSkill.getImportance() * mapping.getRelevance()));
            }
        }
        return result;
    }

    private CompanyRole activeRole(UUID roleId) {
        CompanyRole role = roles.findById(roleId).filter(CompanyRole::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Role", roleId));
        companies.findById(role.getCompanyId()).filter(Company::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Role", roleId));
        return role;
    }

    private MockInterviewSession owned(UUID userId, UUID sessionId) {
        return sessions.findByIdAndUserId(sessionId, userId).orElseThrow(() -> new ResourceNotFoundException("Mock interview", sessionId));
    }

    private SkillResult skillResult(List<MockInterviewItem> skillItems) {
        MockInterviewItem first = skillItems.getFirst();
        List<MockInterviewItem> rated = skillItems.stream().filter(i -> i.getSelfRating() != null).toList();
        BigDecimal rating = rated.isEmpty() ? null : rated.stream().map(i -> BigDecimal.valueOf(i.getSelfRating()).multiply(BigDecimal.valueOf(i.getSkillImportance() * i.getTopicRelevance())))
                .reduce(BigDecimal.ZERO, BigDecimal::add).divide(BigDecimal.valueOf(rated.stream()
                        .mapToInt(i -> i.getSkillImportance() * i.getTopicRelevance()).sum()), 2, RoundingMode.HALF_UP);
        long answered = skillItems.stream().filter(i -> i.getAnsweredAt() != null).count();
        BigDecimal completion = BigDecimal.valueOf(answered * 100L).divide(BigDecimal.valueOf(skillItems.size()), 2, RoundingMode.HALF_UP);
        return new SkillResult(first.getSkillId(), first.getSkillName(), first.getSkillImportance(), (int) answered, skillItems.size(), completion, rating);
    }

    private SessionSummary summary(MockInterviewSession s) {
        return new SessionSummary(s.getId(), s.getRoleId(), s.getRoleName(), s.getStatus(), s.getQuestionCount(), s.getAnsweredCount(), s.getStartedAt(), s.getCompletedAt());
    }
    private QuestionPrompt prompt(MockInterviewItem i) { return new QuestionPrompt(i.getQuestionId(), i.getPosition(), i.getSkillName(), i.getTitle(), i.getQuestionText(), i.getDifficulty()); }
    private AnswerDetail answerDetail(MockInterviewItem i) {
        return new AnswerDetail(i.getQuestionId(), i.getPosition(), i.getSkillName(), i.getTitle(), i.getQuestionText(), i.getDifficulty(),
                i.getSubmittedAnswerText(), i.getSelfRating(), i.getAnsweredAt());
    }
    private record SkillTopic(UUID skillId, String skillName, int importance, UUID topicId, int relevance, int weight) { }
    private record QuestionChoice(Question question, SkillTopic mapping) { }
}
