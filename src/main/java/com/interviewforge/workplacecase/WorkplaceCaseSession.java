package com.interviewforge.workplacecase;

import com.interviewforge.questionbank.Difficulty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "workplace_case_sessions")
public class WorkplaceCaseSession {
    @Id private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(name = "case_id", nullable = false) private UUID caseId;
    @Column(name = "case_title", nullable = false, length = 200) private String caseTitle;
    @Column(name = "role_id", nullable = false) private UUID roleId;
    @Column(name = "role_name", nullable = false, length = 120) private String roleName;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 12) private Difficulty difficulty;
    @Column(name = "graph_snapshot", nullable = false, columnDefinition = "jsonb") @JdbcTypeCode(SqlTypes.JSON) private String graphSnapshot;
    @Column(name = "current_node_key", nullable = false, length = 60) private String currentNodeKey;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16) private WorkplaceCaseStatus status;
    @Column(name = "decision_count", nullable = false) private int decisionCount;
    @Column(name = "total_points", nullable = false) private int totalPoints;
    @Column(name = "started_at", nullable = false) private Instant startedAt;
    @Column(name = "completed_at") private Instant completedAt;
    protected WorkplaceCaseSession() { }
    public WorkplaceCaseSession(UUID userId, WorkplaceCase workplaceCase, String roleName, String graphSnapshot, String currentNodeKey) {
        this.id = UUID.randomUUID(); this.userId = userId; this.caseId = workplaceCase.getId(); this.caseTitle = workplaceCase.getTitle();
        this.roleId = workplaceCase.getRoleId(); this.roleName = roleName; this.difficulty = workplaceCase.getDifficulty();
        this.graphSnapshot = graphSnapshot; this.currentNodeKey = currentNodeKey; this.status = WorkplaceCaseStatus.IN_PROGRESS;
        this.startedAt = Instant.now();
    }
    public void recordDecision(String nextNodeKey, int points, boolean completed) {
        this.currentNodeKey = nextNodeKey; this.decisionCount++; this.totalPoints += points;
        if (completed) { this.status = WorkplaceCaseStatus.COMPLETED; this.completedAt = Instant.now(); }
    }
    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getCaseId() { return caseId; }
    public String getCaseTitle() { return caseTitle; }
    public UUID getRoleId() { return roleId; }
    public String getRoleName() { return roleName; }
    public Difficulty getDifficulty() { return difficulty; }
    public String getGraphSnapshot() { return graphSnapshot; }
    public String getCurrentNodeKey() { return currentNodeKey; }
    public WorkplaceCaseStatus getStatus() { return status; }
    public int getDecisionCount() { return decisionCount; }
    public int getTotalPoints() { return totalPoints; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getCompletedAt() { return completedAt; }
}
