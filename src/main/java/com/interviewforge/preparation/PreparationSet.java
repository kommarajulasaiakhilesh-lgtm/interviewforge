package com.interviewforge.preparation;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="preparation_sets")
public class PreparationSet {
    @Id private UUID id;
    @Column(name="role_id",nullable=false) private UUID roleId;
    @Column(nullable=false,length=160) private String title;
    @Column(length=1000) private String description;
    @Column(nullable=false) private boolean published;
    @Column(nullable=false) private boolean archived;
    @Column(name="created_at",nullable=false) private Instant createdAt;
    @Column(name="updated_at",nullable=false) private Instant updatedAt;
    protected PreparationSet() { }
    public PreparationSet(UUID roleId,String title,String description,boolean published){id=UUID.randomUUID();this.roleId=roleId;this.title=title;this.description=description;this.published=published;createdAt=Instant.now();updatedAt=createdAt;}
    public void update(UUID roleId,String title,String description,boolean published){this.roleId=roleId;this.title=title;this.description=description;this.published=published;updatedAt=Instant.now();}
    public void archive(){archived=true;published=false;updatedAt=Instant.now();}
    public UUID getId(){return id;} public UUID getRoleId(){return roleId;} public String getTitle(){return title;} public String getDescription(){return description;} public boolean isPublished(){return published;} public boolean isArchived(){return archived;} public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;}
}
