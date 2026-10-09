package com.interviewforge.workplacecase;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "workplace_case_nodes")
public class WorkplaceCaseNode {
    @Id private UUID id;
    @Column(name = "case_id", nullable = false) private UUID caseId;
    @Column(name = "node_key", nullable = false, length = 60) private String nodeKey;
    @Enumerated(EnumType.STRING) @Column(name = "node_type", nullable = false, length = 12) private CaseNodeType nodeType;
    @Column(nullable = false) private int position;
    @Column(nullable = false, length = 200) private String heading;
    @Column(name = "situation_text", nullable = false, columnDefinition = "text") private String situationText;
    @Column(name = "lesson_text", columnDefinition = "text") private String lessonText;
    protected WorkplaceCaseNode() { }
    public WorkplaceCaseNode(UUID caseId, String nodeKey, CaseNodeType nodeType, int position, String heading, String situationText, String lessonText) {
        this.id = UUID.randomUUID(); this.caseId = caseId; this.nodeKey = nodeKey; this.nodeType = nodeType;
        this.position = position; this.heading = heading; this.situationText = situationText; this.lessonText = lessonText;
    }
    public String getNodeKey() { return nodeKey; }
    public CaseNodeType getNodeType() { return nodeType; }
    public int getPosition() { return position; }
    public String getHeading() { return heading; }
    public String getSituationText() { return situationText; }
    public String getLessonText() { return lessonText; }
}
