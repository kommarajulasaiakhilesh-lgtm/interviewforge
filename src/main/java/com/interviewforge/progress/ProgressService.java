package com.interviewforge.progress;

import com.interviewforge.practice.PracticeDtos.SessionSummary;
import com.interviewforge.practice.PracticeSession;
import com.interviewforge.practice.PracticeSessionRepository;
import com.interviewforge.progress.ProgressDtos.*;
import com.interviewforge.progress.ProgressProjection.OverviewRow;
import com.interviewforge.progress.ProgressProjection.TopicRow;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProgressService {
    private static final int MINIMUM_ATTEMPTS_FOR_RATING = 5;
    private final ProgressRepository progress;
    private final PracticeSessionRepository sessions;
    private final JdbcTemplate jdbc;

    public ProgressService(ProgressRepository progress, PracticeSessionRepository sessions, JdbcTemplate jdbc) {
        this.progress = progress; this.sessions = sessions; this.jdbc = jdbc;
    }

    public Overview overview(UUID userId) {
        OverviewRow row = progress.overview(userId);
        Page<PracticeSession> recent = sessions.findAllByUserIdOrderByStartedAtDesc(userId, PageRequest.of(0, 5));
        long answered = number(row.getMcqQuestionsAnswered());
        long correct = number(row.getMcqCorrectAnswers());
        return new Overview(number(row.getCompletedSessions()), number(row.getInProgressSessions()), answered,
                correct, accuracy(correct, answered), recent.getContent().stream().map(this::summary).toList());
    }

    public TopicProgress topics(UUID userId) {
        List<TopicPerformance> result = progress.topics(userId).stream().map(this::topic).toList();
        return new TopicProgress(MINIMUM_ATTEMPTS_FOR_RATING, result);
    }

    public LearningInsights learningInsights(UUID userId) {
        List<ConfidencePerformance> confidence = jdbc.query("""
                SELECT i.confidence_rating, COUNT(*) AS answered,
                       COUNT(*) FILTER (WHERE i.is_correct) AS correct,
                       ROUND(100.0 * COUNT(*) FILTER (WHERE i.is_correct) / NULLIF(COUNT(*), 0), 2) AS accuracy
                FROM practice_session_items i JOIN practice_sessions s ON s.id = i.session_id
                WHERE s.user_id = ? AND i.type = 'MCQ' AND i.answered_at IS NOT NULL AND i.confidence_rating IS NOT NULL
                GROUP BY i.confidence_rating ORDER BY i.confidence_rating
                """, (rs, rowNum) -> new ConfidencePerformance(rs.getInt("confidence_rating"), rs.getLong("answered"),
                rs.getLong("correct"), rs.getBigDecimal("accuracy")), userId);
        List<MisconceptionPerformance> misconceptions = jdbc.query("""
                SELECT i.misconception_label, COUNT(*) AS incorrect_answers, MAX(i.answered_at) AS most_recent_at
                FROM practice_session_items i JOIN practice_sessions s ON s.id = i.session_id
                WHERE s.user_id = ? AND i.type = 'MCQ' AND i.is_correct = false
                  AND i.misconception_label IS NOT NULL AND i.misconception_label <> ''
                GROUP BY i.misconception_label HAVING COUNT(*) >= 2
                ORDER BY incorrect_answers DESC, most_recent_at DESC LIMIT 10
                """, (rs, rowNum) -> new MisconceptionPerformance(rs.getString("misconception_label"),
                rs.getLong("incorrect_answers"), rs.getTimestamp("most_recent_at").toInstant()), userId);
        return new LearningInsights(confidence, misconceptions, 3);
    }

    public com.interviewforge.questionbank.QuestionBankDtos.PageResponse<SessionSummary> attempts(UUID userId, int page, int size) {
        if (page < 0 || size < 1 || size > 100) throw new IllegalArgumentException("page must be non-negative and size must be between 1 and 100.");
        Page<PracticeSession> result = sessions.findAllByUserIdOrderByStartedAtDesc(userId, PageRequest.of(page, size));
        return new com.interviewforge.questionbank.QuestionBankDtos.PageResponse<>(result.getContent().stream().map(this::summary).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    private TopicPerformance topic(TopicRow row) {
        long answered = number(row.getQuestionsAnswered());
        long correct = number(row.getCorrectAnswers());
        TopicLevel level = answered < MINIMUM_ATTEMPTS_FOR_RATING ? TopicLevel.NOT_ENOUGH_DATA
                : accuracy(correct, answered).compareTo(BigDecimal.valueOf(80)) >= 0 ? TopicLevel.STRONG
                : accuracy(correct, answered).compareTo(BigDecimal.valueOf(60)) < 0 ? TopicLevel.NEEDS_WORK
                : TopicLevel.DEVELOPING;
        Instant lastAttempt = row.getLastAttemptEpoch() == null ? null : Instant.ofEpochMilli(row.getLastAttemptEpoch());
        return new TopicPerformance(row.getTopicId(), row.getTopicName(), answered, correct, accuracy(correct, answered), level, lastAttempt);
    }
    private BigDecimal accuracy(long correct, long total) {
        return total == 0 ? null : BigDecimal.valueOf(correct * 100L).divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }
    private long number(Long value) { return value == null ? 0 : value; }
    private SessionSummary summary(PracticeSession s) {
        return new SessionSummary(s.getId(), s.getStatus(), s.getStartedAt(), s.getCompletedAt(), s.getType(),
                s.getQuestionCount(), s.getAnsweredCount(), s.getCorrectCount(), s.getScorePercent());
    }
}
