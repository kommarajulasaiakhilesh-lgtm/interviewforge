package com.interviewforge.mockinterview;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "mock_interview_sessions")
public class MockInterviewSession {
    @Id private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(name = "role_id", nullable = false) private UUID roleId;
    @Column(name = "role_name", nullable = false, length = 120) private String roleName;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16) private MockInterviewStatus status;
    @Column(name = "question_count", nullable = false) private int questionCount;
    @Column(name = "answered_count", nullable = false) private int answeredCount;
    @Column(name = "started_at", nullable = false) private Instant startedAt;
    @Column(name = "completed_at") private Instant completedAt;

    protected MockInterviewSession() { }

    public MockInterviewSession(UUID userId, UUID roleId, String roleName, int questionCount) {
        this.id = UUID.randomUUID(); this.userId = userId; this.roleId = roleId; this.roleName = roleName;
        this.questionCount = questionCount; this.status = MockInterviewStatus.IN_PROGRESS; this.startedAt = Instant.now();
    }

    public void recordAnswer() {
        answeredCount++;
        if (answeredCount == questionCount) { status = MockInterviewStatus.COMPLETED; completedAt = Instant.now(); }
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getRoleId() { return roleId; }
    public String getRoleName() { return roleName; }
    public MockInterviewStatus getStatus() { return status; }
    public int getQuestionCount() { return questionCount; }
    public int getAnsweredCount() { return answeredCount; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getCompletedAt() { return completedAt; }
}
