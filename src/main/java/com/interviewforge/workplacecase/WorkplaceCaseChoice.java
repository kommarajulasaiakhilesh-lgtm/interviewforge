package com.interviewforge.workplacecase;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "workplace_case_choices")
public class WorkplaceCaseChoice {
    @Id private UUID id;
    @Column(name = "case_id", nullable = false) private UUID caseId;
    @Column(name = "node_key", nullable = false, length = 60) private String nodeKey;
    @Column(name = "choice_key", nullable = false, length = 30) private String choiceKey;
    @Column(nullable = false) private int position;
    @Column(name = "choice_label", nullable = false, length = 30) private String choiceLabel;
    @Column(name = "choice_text", nullable = false, length = 2000) private String choiceText;
    @Enumerated(EnumType.STRING) @Column(name = "decision_quality", nullable = false, length = 12) private DecisionQuality decisionQuality;
    @Column(name = "quality_points", nullable = false) private int qualityPoints;
    @Column(nullable = false, columnDefinition = "text") private String explanation;
    @Column(name = "when_appropriate", columnDefinition = "text") private String whenAppropriate;
    @Column(name = "tradeoff_summary", columnDefinition = "text") private String tradeoffSummary;
    @Column(name = "misconception_label", length = 160) private String misconceptionLabel;
    @Column(name = "next_node_key", nullable = false, length = 60) private String nextNodeKey;
    protected WorkplaceCaseChoice() { }
    public WorkplaceCaseChoice(UUID caseId, String nodeKey, int position, WorkplaceCaseDtos.ChoiceInput choice, int qualityPoints) {
        this.id = UUID.randomUUID(); this.caseId = caseId; this.nodeKey = nodeKey; this.choiceKey = choice.choiceKey();
        this.position = position; this.choiceLabel = choice.choiceLabel(); this.choiceText = choice.choiceText(); this.decisionQuality = choice.decisionQuality();
        this.qualityPoints = qualityPoints; this.explanation = choice.explanation(); this.whenAppropriate = choice.whenAppropriate();
        this.tradeoffSummary = choice.tradeoffSummary(); this.misconceptionLabel = choice.misconceptionLabel();
        this.nextNodeKey = choice.nextNodeKey();
    }
    public String getNodeKey() { return nodeKey; }
    public String getChoiceKey() { return choiceKey; }
    public int getPosition() { return position; }
    public String getChoiceLabel() { return choiceLabel; }
    public String getChoiceText() { return choiceText; }
    public DecisionQuality getDecisionQuality() { return decisionQuality; }
    public int getQualityPoints() { return qualityPoints; }
    public String getExplanation() { return explanation; }
    public String getWhenAppropriate() { return whenAppropriate; }
    public String getTradeoffSummary() { return tradeoffSummary; }
    public String getMisconceptionLabel() { return misconceptionLabel; }
    public String getNextNodeKey() { return nextNodeKey; }
}
