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

@Entity
@Table(name = "workplace_cases")
public class WorkplaceCase {
    @Id private UUID id;
    @Column(nullable = false, length = 200) private String title;
    @Column(nullable = false, length = 180) private String slug;
    @Column(length = 1000) private String description;
    @Column(name = "role_id", nullable = false) private UUID roleId;
    @Column(name = "topic_id", nullable = false) private UUID topicId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 12) private Difficulty difficulty;
    @Column(name = "estimated_minutes", nullable = false) private int estimatedMinutes;
    @Column(name = "scenario_intro", nullable = false, columnDefinition = "text") private String scenarioIntro;
    @Column(name = "learning_objective", length = 1000) private String learningObjective;
    @Column(name = "source_type", length = 30) private String sourceType;
    @Column(name = "source_label", length = 200) private String sourceLabel;
    @Column(name = "source_url", length = 1000) private String sourceUrl;
    @Column(name = "source_verified_at") private Instant sourceVerifiedAt;
    @Column(nullable = false) private boolean published;
    @Column(nullable = false) private boolean archived;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected WorkplaceCase() { }

    public WorkplaceCase(String title, String slug, String description, UUID roleId, UUID topicId, Difficulty difficulty,
            int estimatedMinutes, String scenarioIntro, String learningObjective, String sourceType, String sourceLabel,
            String sourceUrl, Instant sourceVerifiedAt, boolean published) {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        update(title, slug, description, roleId, topicId, difficulty, estimatedMinutes, scenarioIntro, learningObjective,
                sourceType, sourceLabel, sourceUrl, sourceVerifiedAt, published);
    }

    public void update(String title, String slug, String description, UUID roleId, UUID topicId, Difficulty difficulty,
            int estimatedMinutes, String scenarioIntro, String learningObjective, String sourceType, String sourceLabel,
            String sourceUrl, Instant sourceVerifiedAt, boolean published) {
        this.title = title; this.slug = slug; this.description = description; this.roleId = roleId; this.topicId = topicId;
        this.difficulty = difficulty; this.estimatedMinutes = estimatedMinutes; this.scenarioIntro = scenarioIntro;
        this.learningObjective = learningObjective; this.sourceType = sourceType; this.sourceLabel = sourceLabel;
        this.sourceUrl = sourceUrl; this.sourceVerifiedAt = sourceVerifiedAt; this.published = published;
        this.updatedAt = Instant.now();
    }

    public void archive() { archived = true; published = false; updatedAt = Instant.now(); }
    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getSlug() { return slug; }
    public String getDescription() { return description; }
    public UUID getRoleId() { return roleId; }
    public UUID getTopicId() { return topicId; }
    public Difficulty getDifficulty() { return difficulty; }
    public int getEstimatedMinutes() { return estimatedMinutes; }
    public String getScenarioIntro() { return scenarioIntro; }
    public String getLearningObjective() { return learningObjective; }
    public String getSourceType() { return sourceType; }
    public String getSourceLabel() { return sourceLabel; }
    public String getSourceUrl() { return sourceUrl; }
    public Instant getSourceVerifiedAt() { return sourceVerifiedAt; }
    public boolean isPublished() { return published; }
    public boolean isArchived() { return archived; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
