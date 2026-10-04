package com.interviewforge.practice;

import com.interviewforge.questionbank.QuestionType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "practice_sessions")
public class PracticeSession {
    @Id private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16) private PracticeSessionStatus status;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 10) private QuestionType type;
    @Column(name = "question_count", nullable = false) private int questionCount;
    @Column(name = "answered_count", nullable = false) private int answeredCount;
    @Column(name = "correct_count", nullable = false) private int correctCount;
    @Column(name = "score_percent", precision = 5, scale = 2) private BigDecimal scorePercent;
    @Column(name = "started_at", nullable = false) private Instant startedAt;
    @Column(name = "completed_at") private Instant completedAt;
    protected PracticeSession() { }
    public PracticeSession(UUID userId, QuestionType type, int count) {
        id = UUID.randomUUID(); this.userId = userId; this.type = type; questionCount = count;
        status = PracticeSessionStatus.IN_PROGRESS; startedAt = Instant.now();
    }
    public void recordAnswer(boolean correct) {
        answeredCount++;
        if (type == QuestionType.MCQ && correct) correctCount++;
        if (answeredCount == questionCount) {
            status = PracticeSessionStatus.COMPLETED; completedAt = Instant.now();
            if (type == QuestionType.MCQ) scorePercent = BigDecimal.valueOf(correctCount * 100L)
                    .divide(BigDecimal.valueOf(questionCount), 2, RoundingMode.HALF_UP);
        }
    }
    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public PracticeSessionStatus getStatus() { return status; }
    public QuestionType getType() { return type; }
    public int getQuestionCount() { return questionCount; }
    public int getAnsweredCount() { return answeredCount; }
    public int getCorrectCount() { return correctCount; }
    public BigDecimal getScorePercent() { return scorePercent; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getCompletedAt() { return completedAt; }
}
