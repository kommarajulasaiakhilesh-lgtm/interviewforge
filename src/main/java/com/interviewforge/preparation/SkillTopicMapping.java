package com.interviewforge.preparation;

import jakarta.persistence.*;
import java.util.UUID;

@Entity @Table(name="skill_topics")
public class SkillTopicMapping {
    @Id private UUID id;
    @Column(name="skill_id",nullable=false) private UUID skillId;
    @Column(name="topic_id",nullable=false) private UUID topicId;
    @Column(nullable=false) private int relevance;
    protected SkillTopicMapping() { }
    public SkillTopicMapping(UUID skillId,UUID topicId,int relevance){id=UUID.randomUUID();this.skillId=skillId;this.topicId=topicId;this.relevance=relevance;}
    public UUID getSkillId(){return skillId;} public UUID getTopicId(){return topicId;} public int getRelevance(){return relevance;}
}
