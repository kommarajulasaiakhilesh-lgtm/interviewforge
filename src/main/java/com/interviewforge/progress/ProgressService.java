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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProgressService {
    private static final int MINIMUM_ATTEMPTS_FOR_RATING = 5;
    private final ProgressRepository progress;
    private final PracticeSessionRepository sessions;

    public ProgressService(ProgressRepository progress, PracticeSessionRepository sessions) {
        this.progress = progress; this.sessions = sessions;
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
