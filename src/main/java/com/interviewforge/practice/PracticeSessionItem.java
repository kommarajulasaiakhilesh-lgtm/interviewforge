package com.interviewforge.practice;

import com.interviewforge.questionbank.Difficulty;
import com.interviewforge.questionbank.QuestionType;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "practice_session_items")
public class PracticeSessionItem {
    @Id private UUID id;
    @Column(name = "session_id", nullable = false) private UUID sessionId;
    @Column(name = "question_id", nullable = false) private UUID questionId;
    @Column(name = "topic_id", nullable = false) private UUID topicId;
    @Column(nullable = false) private int position;
    @Column(nullable = false, length = 200) private String title;
    @Column(name = "question_text", nullable = false, columnDefinition = "text") private String questionText;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 12) private Difficulty difficulty;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 10) private QuestionType type;
    @Column(name = "options_json", columnDefinition = "jsonb") @JdbcTypeCode(SqlTypes.JSON) private String optionsJson;
    @Column(name = "correct_option_index") private Integer correctOptionIndex;
    @Column(name = "answer_text", columnDefinition = "text") private String answerText;
    @Column(columnDefinition = "text") private String explanation;
    @Column(name = "option_explanations_json", columnDefinition = "jsonb") @JdbcTypeCode(SqlTypes.JSON) private String optionExplanationsJson;
    @Column(name = "theory_notes", columnDefinition = "text") private String theoryNotes;
    @Column(name = "workplace_example", columnDefinition = "text") private String workplaceExample;
    @Column(name = "misconception_label", length = 160) private String misconceptionLabel;
    @Column(name = "scenario_context", columnDefinition = "text") private String scenarioContext;
    @Column(name = "interview_stage", length = 40) private String interviewStage;
    @Column(name = "source_type", length = 30) private String sourceType;
    @Column(name = "source_label", length = 200) private String sourceLabel;
    @Column(name = "source_url", length = 1000) private String sourceUrl;
    @Column(name = "source_verified_at") private Instant sourceVerifiedAt;
    @Column(name = "confidence_rating") private Integer confidenceRating;
    @Column(name = "selected_option_index") private Integer selectedOptionIndex;
    @Column(name = "submitted_answer_text", columnDefinition = "text") private String submittedAnswerText;
    @Column(name = "is_correct") private Boolean correct;
    @Column(name = "answered_at") private Instant answeredAt;
    protected PracticeSessionItem() { }
    public PracticeSessionItem(UUID sessionId, UUID questionId, UUID topicId, int position, String title, String questionText,
            Difficulty difficulty, QuestionType type, String optionsJson, Integer correctOptionIndex, String answerText, String explanation,
            String optionExplanationsJson, String theoryNotes, String workplaceExample, String misconceptionLabel,
            String scenarioContext, String interviewStage, String sourceType, String sourceLabel, String sourceUrl, Instant sourceVerifiedAt) {
        id = UUID.randomUUID(); this.sessionId = sessionId; this.questionId = questionId; this.topicId = topicId; this.position = position;
        this.title = title; this.questionText = questionText; this.difficulty = difficulty; this.type = type;
        this.optionsJson = optionsJson; this.correctOptionIndex = correctOptionIndex; this.answerText = answerText; this.explanation = explanation;
        this.optionExplanationsJson = optionExplanationsJson; this.theoryNotes = theoryNotes; this.workplaceExample = workplaceExample;
        this.misconceptionLabel = misconceptionLabel; this.scenarioContext = scenarioContext; this.interviewStage = interviewStage;
        this.sourceType = sourceType; this.sourceLabel = sourceLabel; this.sourceUrl = sourceUrl; this.sourceVerifiedAt = sourceVerifiedAt;
    }
    public void submit(Integer optionIndex, String text, Boolean correct, Integer confidenceRating) {
        selectedOptionIndex = optionIndex; submittedAnswerText = text; this.correct = correct; this.confidenceRating = confidenceRating; answeredAt = Instant.now();
    }
    public UUID getQuestionId() { return questionId; }
    public UUID getTopicId() { return topicId; }
    public int getPosition() { return position; }
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
    public Integer getConfidenceRating() { return confidenceRating; }
    public Integer getSelectedOptionIndex() { return selectedOptionIndex; }
    public String getSubmittedAnswerText() { return submittedAnswerText; }
    public Boolean getCorrect() { return correct; }
    public Instant getAnsweredAt() { return answeredAt; }
}
