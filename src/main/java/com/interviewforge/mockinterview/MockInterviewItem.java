package com.interviewforge.mockinterview;

import com.interviewforge.questionbank.Difficulty;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "mock_interview_items")
public class MockInterviewItem {
    @Id private UUID id;
    @Column(name = "session_id", nullable = false) private UUID sessionId;
    @Column(name = "question_id", nullable = false) private UUID questionId;
    @Column(name = "topic_id", nullable = false) private UUID topicId;
    @Column(name = "skill_id", nullable = false) private UUID skillId;
    @Column(name = "skill_name", nullable = false, length = 120) private String skillName;
    @Column(name = "skill_importance", nullable = false) private int skillImportance;
    @Column(name = "topic_relevance", nullable = false) private int topicRelevance;
    @Column(nullable = false) private int position;
    @Column(nullable = false, length = 200) private String title;
    @Column(name = "question_text", nullable = false, columnDefinition = "text") private String questionText;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 12) private Difficulty difficulty;
    @Column(name = "submitted_answer_text", columnDefinition = "text") private String submittedAnswerText;
    @Column(name = "self_rating") private Integer selfRating;
    @Column(name = "answered_at") private Instant answeredAt;

    protected MockInterviewItem() { }

    public MockInterviewItem(UUID sessionId, UUID questionId, UUID topicId, UUID skillId, String skillName,
            int skillImportance, int topicRelevance, int position, String title, String questionText, Difficulty difficulty) {
        this.id = UUID.randomUUID(); this.sessionId = sessionId; this.questionId = questionId; this.topicId = topicId;
        this.skillId = skillId; this.skillName = skillName; this.skillImportance = skillImportance;
        this.topicRelevance = topicRelevance; this.position = position; this.title = title;
        this.questionText = questionText; this.difficulty = difficulty;
    }

    public void submit(String answer, int rating) { submittedAnswerText = answer; selfRating = rating; answeredAt = Instant.now(); }
    public UUID getQuestionId() { return questionId; }
    public UUID getTopicId() { return topicId; }
    public UUID getSkillId() { return skillId; }
    public String getSkillName() { return skillName; }
    public int getSkillImportance() { return skillImportance; }
    public int getTopicRelevance() { return topicRelevance; }
    public int getPosition() { return position; }
    public String getTitle() { return title; }
    public String getQuestionText() { return questionText; }
    public Difficulty getDifficulty() { return difficulty; }
    public String getSubmittedAnswerText() { return submittedAnswerText; }
    public Integer getSelfRating() { return selfRating; }
    public Instant getAnsweredAt() { return answeredAt; }
}
