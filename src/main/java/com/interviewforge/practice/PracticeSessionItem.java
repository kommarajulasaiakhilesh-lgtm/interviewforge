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
    @Column(name = "selected_option_index") private Integer selectedOptionIndex;
    @Column(name = "submitted_answer_text", columnDefinition = "text") private String submittedAnswerText;
    @Column(name = "is_correct") private Boolean correct;
    @Column(name = "answered_at") private Instant answeredAt;
    protected PracticeSessionItem() { }
    public PracticeSessionItem(UUID sessionId, UUID questionId, UUID topicId, int position, String title, String questionText,
            Difficulty difficulty, QuestionType type, String optionsJson, Integer correctOptionIndex, String answerText, String explanation) {
        id = UUID.randomUUID(); this.sessionId = sessionId; this.questionId = questionId; this.topicId = topicId; this.position = position;
        this.title = title; this.questionText = questionText; this.difficulty = difficulty; this.type = type;
        this.optionsJson = optionsJson; this.correctOptionIndex = correctOptionIndex; this.answerText = answerText; this.explanation = explanation;
    }
    public void submit(Integer optionIndex, String text, Boolean correct) {
        selectedOptionIndex = optionIndex; submittedAnswerText = text; this.correct = correct; answeredAt = Instant.now();
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
    public Integer getSelectedOptionIndex() { return selectedOptionIndex; }
    public String getSubmittedAnswerText() { return submittedAnswerText; }
    public Boolean getCorrect() { return correct; }
    public Instant getAnsweredAt() { return answeredAt; }
}
