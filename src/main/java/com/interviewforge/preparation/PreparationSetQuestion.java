package com.interviewforge.preparation;

import jakarta.persistence.*;
import java.util.UUID;

@Entity @Table(name="preparation_set_questions")
public class PreparationSetQuestion {
    @Id private UUID id;
    @Column(name="set_id",nullable=false) private UUID setId;
    @Column(name="question_id",nullable=false) private UUID questionId;
    @Column(nullable=false) private int position;
    protected PreparationSetQuestion() { }
    public PreparationSetQuestion(UUID setId,UUID questionId,int position){id=UUID.randomUUID();this.setId=setId;this.questionId=questionId;this.position=position;}
    public UUID getQuestionId(){return questionId;} public int getPosition(){return position;}
}
