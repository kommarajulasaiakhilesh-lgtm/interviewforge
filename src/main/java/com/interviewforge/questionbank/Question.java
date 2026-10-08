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
    @Column(name = "option_explanations_json", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String optionExplanationsJson;
    @Column(name = "theory_notes", columnDefinition = "text") private String theoryNotes;
    @Column(name = "workplace_example", columnDefinition = "text") private String workplaceExample;
    @Column(name = "misconception_label", length = 160) private String misconceptionLabel;
    @Column(name = "scenario_context", columnDefinition = "text") private String scenarioContext;
    @Column(name = "interview_stage", length = 40) private String interviewStage;
    @Column(name = "source_type", length = 30) private String sourceType;
    @Column(name = "source_label", length = 200) private String sourceLabel;
    @Column(name = "source_url", length = 1000) private String sourceUrl;
    @Column(name = "source_verified_at") private Instant sourceVerifiedAt;
    @Column(name = "evaluation_criteria", columnDefinition = "text") private String evaluationCriteria;
    @Column(name = "follow_up_prompt", columnDefinition = "text") private String followUpPrompt;
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
                    String optionExplanationsJson, String theoryNotes, String workplaceExample, String misconceptionLabel,
                    String scenarioContext, String interviewStage, String sourceType, String sourceLabel, String sourceUrl,
                    Instant sourceVerifiedAt, String evaluationCriteria, String followUpPrompt,
                    Set<Tag> tags, boolean published) {
        this.id = UUID.randomUUID(); this.createdAt = Instant.now();
        update(topic, title, questionText, difficulty, type, optionsJson, correctOptionIndex, answerText, explanation,
                optionExplanationsJson, theoryNotes, workplaceExample, misconceptionLabel, scenarioContext,
                interviewStage, sourceType, sourceLabel, sourceUrl, sourceVerifiedAt, evaluationCriteria, followUpPrompt, tags, published);
    }
    public void update(Topic topic, String title, String questionText, Difficulty difficulty, QuestionType type,
                       String optionsJson, Integer correctOptionIndex, String answerText, String explanation,
                       String optionExplanationsJson, String theoryNotes, String workplaceExample, String misconceptionLabel,
                       String scenarioContext, String interviewStage, String sourceType, String sourceLabel, String sourceUrl,
                       Instant sourceVerifiedAt, String evaluationCriteria, String followUpPrompt,
                       Set<Tag> tags, boolean published) {
        this.topic = topic; this.title = title; this.questionText = questionText; this.difficulty = difficulty;
        this.type = type; this.optionsJson = optionsJson; this.correctOptionIndex = correctOptionIndex;
        this.answerText = answerText; this.explanation = explanation; this.tags = new LinkedHashSet<>(tags);
        this.optionExplanationsJson = optionExplanationsJson; this.theoryNotes = theoryNotes;
        this.workplaceExample = workplaceExample; this.misconceptionLabel = misconceptionLabel;
        this.scenarioContext = scenarioContext; this.interviewStage = interviewStage; this.sourceType = sourceType;
        this.sourceLabel = sourceLabel; this.sourceUrl = sourceUrl; this.sourceVerifiedAt = sourceVerifiedAt;
        this.evaluationCriteria = evaluationCriteria; this.followUpPrompt = followUpPrompt;
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
    public String getOptionExplanationsJson() { return optionExplanationsJson; }
    public String getTheoryNotes() { return theoryNotes; }
    public String getWorkplaceExample() { return workplaceExample; }
    public String getMisconceptionLabel() { return misconceptionLabel; }
    public String getScenarioContext() { return scenarioContext; }
    public String getInterviewStage() { return interviewStage; }
    public String getSourceType() { return sourceType; }
    public String getSourceLabel() { return sourceLabel; }
    public String getSourceUrl() { return sourceUrl; }
    public Instant getSourceVerifiedAt() { return sourceVerifiedAt; }
    public String getEvaluationCriteria() { return evaluationCriteria; }
    public String getFollowUpPrompt() { return followUpPrompt; }
    public Set<Tag> getTags() { return tags; }
    public boolean isPublished() { return published; }
    public boolean isArchived() { return archived; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
