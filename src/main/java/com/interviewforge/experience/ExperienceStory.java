package com.interviewforge.experience;

import com.interviewforge.preparation.Skill;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "experience_stories")
public class ExperienceStory {
    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 160)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "experience_type", nullable = false, length = 20)
    private ExperienceType experienceType;

    @Column(name = "experience_context", length = 1000)
    private String context;

    @Column(name = "situation_text", nullable = false, columnDefinition = "text")
    private String situation;

    @Column(name = "task_text", nullable = false, columnDefinition = "text")
    private String task;

    @Column(name = "actions_text", nullable = false, columnDefinition = "text")
    private String actions;

    @Column(name = "result_text", nullable = false, columnDefinition = "text")
    private String result;

    @Column(name = "learning_text", columnDefinition = "text")
    private String learning;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "experience_story_skills", joinColumns = @JoinColumn(name = "story_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id"))
    @OrderBy("name ASC")
    private Set<Skill> skills = new LinkedHashSet<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ExperienceStory() { }

    public ExperienceStory(UUID userId, String title, ExperienceType experienceType, String context,
            String situation, String task, String actions, String result, String learning, Set<Skill> skills) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.createdAt = Instant.now();
        update(title, experienceType, context, situation, task, actions, result, learning, skills);
    }

    public void update(String title, ExperienceType experienceType, String context,
            String situation, String task, String actions, String result, String learning, Set<Skill> skills) {
        this.title = title;
        this.experienceType = experienceType;
        this.context = blankToNull(context);
        this.situation = situation;
        this.task = task;
        this.actions = actions;
        this.result = result;
        this.learning = blankToNull(learning);
        this.skills = new LinkedHashSet<>(skills);
        this.updatedAt = Instant.now();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getTitle() { return title; }
    public ExperienceType getExperienceType() { return experienceType; }
    public String getContext() { return context; }
    public String getSituation() { return situation; }
    public String getTask() { return task; }
    public String getActions() { return actions; }
    public String getResult() { return result; }
    public String getLearning() { return learning; }
    public Set<Skill> getSkills() { return skills; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
