package com.interviewforge.practice;

import com.interviewforge.practice.PracticeDtos.*;
import com.interviewforge.questionbank.*;
import com.interviewforge.questionbank.QuestionBankDtos.PageResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Service
@Transactional
public class PracticeService {
    private final QuestionRepository questions;
    private final PracticeSessionRepository sessions;
    private final PracticeSessionItemRepository items;
    private final ObjectMapper mapper;
    private final JdbcTemplate jdbc;

    public PracticeService(QuestionRepository questions, PracticeSessionRepository sessions,
            PracticeSessionItemRepository items, ObjectMapper mapper, JdbcTemplate jdbc) {
        this.questions = questions; this.sessions = sessions; this.items = items; this.mapper = mapper; this.jdbc = jdbc;
    }

    public CreateSessionResponse create(UUID userId, CreateSessionRequest request) {
        QuestionType type = request.type() == null ? QuestionType.MCQ : request.type();
        List<UUID> ids = questions.findRandomPublishedQuestionIds(request.topicId(),
                request.difficulty() == null ? null : request.difficulty().name(), type.name(), request.questionCount());
        if (ids.size() < request.questionCount()) throw new InsufficientQuestionsException(request.questionCount(), ids.size());
        return createForQuestionIds(userId, type, ids.subList(0, request.questionCount()));
    }

    public List<ReviewQueueItem> reviewQueue(UUID userId, int limit) {
        if (limit < 1 || limit > 50) throw new IllegalArgumentException("limit must be between 1 and 50.");
        return jdbc.query("""
                SELECT r.question_id, q.title, q.question_text, q.difficulty, q.options_json::text AS options_json,
                       r.due_at, r.interval_days, r.last_confidence, q.misconception_label,
                       q.source_type, q.source_label, q.source_url
                FROM user_question_reviews r JOIN questions q ON q.id = r.question_id
                JOIN topics t ON t.id = q.topic_id
                WHERE r.user_id = ? AND r.due_at <= now() AND q.published AND NOT q.archived AND t.active AND q.type = 'MCQ'
                ORDER BY r.due_at ASC LIMIT ?
                """, (rs, rowNum) -> new ReviewQueueItem(rs.getObject("question_id", UUID.class), rs.getString("title"),
                rs.getString("question_text"), com.interviewforge.questionbank.Difficulty.valueOf(rs.getString("difficulty")),
                options(rs.getString("options_json")), rs.getTimestamp("due_at").toInstant(), rs.getInt("interval_days"),
                (Integer) rs.getObject("last_confidence"), rs.getString("misconception_label"), rs.getString("source_type"),
                rs.getString("source_label"), rs.getString("source_url")), userId, limit);
    }

    public CreateSessionResponse createReviewSession(UUID userId, int questionCount) {
        if (questionCount < 1 || questionCount > 20) throw new IllegalArgumentException("questionCount must be between 1 and 20.");
        List<ReviewQueueItem> due = reviewQueue(userId, Math.min(questionCount, 50));
        if (due.size() < questionCount) throw new InsufficientQuestionsException(questionCount, due.size());
        return createForQuestionIds(userId, QuestionType.MCQ, due.stream().limit(questionCount).map(ReviewQueueItem::questionId).toList());
    }

    private CreateSessionResponse createForQuestionIds(UUID userId, QuestionType type, List<UUID> ids) {
        Map<UUID, Question> byId = questions.findAllById(ids).stream().collect(Collectors.toMap(Question::getId, Function.identity()));
        PracticeSession session = sessions.save(new PracticeSession(userId, type, ids.size()));
        int position = 1;
        for (UUID id : ids) {
            Question q = byId.get(id);
            items.save(new PracticeSessionItem(session.getId(), id, q.getTopic().getId(), position++, q.getTitle(), q.getQuestionText(),
                    q.getDifficulty(), q.getType(), q.getOptionsJson(), q.getCorrectOptionIndex(), q.getAnswerText(), q.getExplanation(),
                    q.getOptionExplanationsJson(), q.getTheoryNotes(), q.getWorkplaceExample(), q.getMisconceptionLabel(),
                    q.getScenarioContext(), q.getInterviewStage(), q.getSourceType(), q.getSourceLabel(), q.getSourceUrl(), q.getSourceVerifiedAt()));
        }
        return new CreateSessionResponse(summary(session), items.findAllBySessionIdOrderByPosition(session.getId()).stream().map(this::prompt).toList());
    }

    public AnswerResponse submit(UUID userId, UUID sessionId, SubmitAnswerRequest request) {
        PracticeSession session = sessions.findOwnedForUpdate(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Practice session", sessionId));
        if (session.getStatus() != PracticeSessionStatus.IN_PROGRESS) throw new IllegalArgumentException("This practice session is already complete.");
        PracticeSessionItem item = items.findBySessionIdAndQuestionId(sessionId, request.questionId())
                .orElseThrow(() -> new ResourceNotFoundException("Question in practice session", request.questionId()));
        if (item.getAnsweredAt() != null) throw new IllegalArgumentException("This question has already been answered.");
        Integer choice = request.selectedOptionIndex();
        String text = null;
        Boolean correct;
        if (item.getType() == QuestionType.MCQ) {
            List<String> options = options(item.getOptionsJson());
            if (choice == null || request.answerText() != null) throw new IllegalArgumentException("MCQ answers require selectedOptionIndex only.");
            if (choice < 0 || choice >= options.size()) throw new IllegalArgumentException("selectedOptionIndex is outside the option list.");
            correct = choice.equals(item.getCorrectOptionIndex());
        } else {
            if (choice != null || !StringUtils.hasText(request.answerText())) throw new IllegalArgumentException("TEXT answers require answerText only.");
            correct = null; text = request.answerText().trim();
        }
        item.submit(choice, text, correct, request.confidenceRating());
        if (item.getType() == QuestionType.MCQ) recordReview(userId, item.getQuestionId(), Boolean.TRUE.equals(correct), request.confidenceRating());
        session.recordAnswer(Boolean.TRUE.equals(correct));
        return new AnswerResponse(sessionId, item.getQuestionId(), session.getStatus(), correct,
                item.getCorrectOptionIndex(), item.getAnswerText(), item.getExplanation(), item.getAnsweredAt(),
                session.getAnsweredCount(), session.getQuestionCount(), session.getScorePercent(),
                explanationList(item.getOptionExplanationsJson()), item.getTheoryNotes(), item.getWorkplaceExample(),
                item.getMisconceptionLabel(), item.getConfidenceRating());
    }

    @Transactional(readOnly = true)
    public SessionDetail detail(UUID userId, UUID sessionId) {
        PracticeSession session = owned(userId, sessionId);
        return new SessionDetail(summary(session), items.findAllBySessionIdOrderByPosition(sessionId).stream().map(this::review).toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<SessionSummary> history(UUID userId, int page, int size) {
        if (page < 0 || size < 1 || size > 100) throw new IllegalArgumentException("page must be non-negative and size must be between 1 and 100.");
        Page<PracticeSession> result = sessions.findAllByUserIdOrderByStartedAtDesc(userId, PageRequest.of(page, size));
        return new PageResponse<>(result.getContent().stream().map(this::summary).toList(), result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    private PracticeSession owned(UUID userId, UUID id) {
        return sessions.findByIdAndUserId(id, userId).orElseThrow(() -> new ResourceNotFoundException("Practice session", id));
    }
    private SessionSummary summary(PracticeSession s) {
        return new SessionSummary(s.getId(), s.getStatus(), s.getStartedAt(), s.getCompletedAt(), s.getType(),
                s.getQuestionCount(), s.getAnsweredCount(), s.getCorrectCount(), s.getScorePercent());
    }
    private QuestionPrompt prompt(PracticeSessionItem i) {
        return new QuestionPrompt(i.getQuestionId(), i.getPosition(), i.getTitle(), i.getQuestionText(), i.getDifficulty(), i.getType(),
                options(i.getOptionsJson()), i.getScenarioContext(), i.getInterviewStage(), i.getSourceType(), i.getSourceLabel(),
                i.getSourceUrl(), i.getSourceVerifiedAt());
    }
    private QuestionReview review(PracticeSessionItem i) {
        boolean answered = i.getAnsweredAt() != null;
        return new QuestionReview(i.getQuestionId(), i.getPosition(), i.getTitle(), i.getQuestionText(), i.getDifficulty(), i.getType(),
                options(i.getOptionsJson()), i.getSelectedOptionIndex(), i.getSubmittedAnswerText(), i.getCorrect(),
                answered ? i.getCorrectOptionIndex() : null, answered ? i.getAnswerText() : null,
                answered ? i.getExplanation() : null, i.getAnsweredAt(), answered ? explanationList(i.getOptionExplanationsJson()) : List.of(),
                answered ? i.getTheoryNotes() : null, answered ? i.getWorkplaceExample() : null,
                answered ? i.getMisconceptionLabel() : null, i.getScenarioContext(), i.getInterviewStage(), i.getSourceType(),
                i.getSourceLabel(), i.getSourceUrl(), i.getSourceVerifiedAt(), i.getConfidenceRating());
    }
    private void recordReview(UUID userId, UUID questionId, boolean correct, Integer confidence) {
        jdbc.update("""
                INSERT INTO user_question_reviews(user_id, question_id, due_at, interval_days, consecutive_correct, last_confidence, last_attempt_at)
                VALUES (?, ?, now() + interval '1 day', 1,
                        CASE WHEN ? AND COALESCE(?, 0) >= 3 THEN 1 ELSE 0 END, ?, now())
                ON CONFLICT (user_id, question_id) DO UPDATE SET
                    interval_days = CASE WHEN NOT ? OR COALESCE(?, 0) <= 2 THEN 1
                                         ELSE LEAST(user_question_reviews.interval_days * 2, 365) END,
                    consecutive_correct = CASE WHEN NOT ? OR COALESCE(?, 0) <= 2 THEN 0
                                               ELSE user_question_reviews.consecutive_correct + 1 END,
                    due_at = now() + (CASE WHEN NOT ? OR COALESCE(?, 0) <= 2 THEN 1
                                           ELSE LEAST(user_question_reviews.interval_days * 2, 365) END) * interval '1 day',
                    last_confidence = ?, last_attempt_at = now()
                """, userId, questionId, correct, confidence, confidence,
                correct, confidence, correct, confidence, correct, confidence, confidence);
    }
    private List<String> explanationList(String json) { return options(json); }
    private List<String> options(String value) {
        if (value == null) return List.of();
        try { return mapper.readValue(value, new TypeReference<>() { }); }
        catch (JacksonException e) { throw new IllegalStateException("Unable to read saved question choices.", e); }
    }
}
