package com.interviewforge.preparation;

import jakarta.persistence.*;
import java.util.UUID;

@Entity @Table(name="role_skills")
public class RoleSkillMapping {
    @Id private UUID id;
    @Column(name="role_id",nullable=false) private UUID roleId;
    @Column(name="skill_id",nullable=false) private UUID skillId;
    @Column(nullable=false) private int importance;
    protected RoleSkillMapping() { }
    public RoleSkillMapping(UUID roleId,UUID skillId,int importance){id=UUID.randomUUID();this.roleId=roleId;this.skillId=skillId;this.importance=importance;}
    public UUID getSkillId(){return skillId;} public int getImportance(){return importance;}
}
