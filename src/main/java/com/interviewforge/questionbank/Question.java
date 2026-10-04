package com.interviewforge.questionbank;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "questions")
public class Question {
    @Id
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;
    @Column(nullable = false, length = 200)
    private String title;
    @Column(name = "question_text", nullable = false, columnDefinition = "text")
    private String questionText;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private Difficulty difficulty;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private QuestionType type;
    @Column(name = "options_json", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String optionsJson;
    @Column(name = "correct_option_index")
    private Integer correctOptionIndex;
    @Column(name = "answer_text", columnDefinition = "text")
    private String answerText;
    @Column(columnDefinition = "text")
    private String explanation;
    @ManyToMany
    @JoinTable(name = "question_tag_assignments", joinColumns = @JoinColumn(name = "question_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags = new LinkedHashSet<>();
    @Column(nullable = false)
    private boolean published;
    @Column(nullable = false)
    private boolean archived;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Question() { }
    public Question(Topic topic, String title, String questionText, Difficulty difficulty, QuestionType type,
                    String optionsJson, Integer correctOptionIndex, String answerText, String explanation,
                    Set<Tag> tags, boolean published) {
        this.id = UUID.randomUUID(); this.createdAt = Instant.now();
        update(topic, title, questionText, difficulty, type, optionsJson, correctOptionIndex, answerText, explanation, tags, published);
    }
    public void update(Topic topic, String title, String questionText, Difficulty difficulty, QuestionType type,
                       String optionsJson, Integer correctOptionIndex, String answerText, String explanation,
                       Set<Tag> tags, boolean published) {
        this.topic = topic; this.title = title; this.questionText = questionText; this.difficulty = difficulty;
        this.type = type; this.optionsJson = optionsJson; this.correctOptionIndex = correctOptionIndex;
        this.answerText = answerText; this.explanation = explanation; this.tags = new LinkedHashSet<>(tags);
        this.published = published; this.updatedAt = Instant.now();
    }
    public void archive() { this.archived = true; this.published = false; this.updatedAt = Instant.now(); }
    public UUID getId() { return id; }
    public Topic getTopic() { return topic; }
    public String getTitle() { return title; }
    public String getQuestionText() { return questionText; }
    public Difficulty getDifficulty() { return difficulty; }
    public QuestionType getType() { return type; }
    public String getOptionsJson() { return optionsJson; }
    public Integer getCorrectOptionIndex() { return correctOptionIndex; }
    public String getAnswerText() { return answerText; }
    public String getExplanation() { return explanation; }
    public Set<Tag> getTags() { return tags; }
    public boolean isPublished() { return published; }
    public boolean isArchived() { return archived; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
