package com.interviewforge.preparation;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="skills")
public class Skill {
    @Id private UUID id;
    @Column(nullable=false,unique=true,length=120) private String name;
    @Column(nullable=false,unique=true,length=140) private String slug;
    @Column(length=1000) private String description;
    @Column(nullable=false) private boolean active;
    @Column(name="created_at",nullable=false) private Instant createdAt;
    @Column(name="updated_at",nullable=false) private Instant updatedAt;
    protected Skill() { }
    public Skill(String name,String slug,String description){id=UUID.randomUUID();this.name=name;this.slug=slug;this.description=description;active=true;createdAt=Instant.now();updatedAt=createdAt;}
    public void update(String name,String slug,String description,boolean active){this.name=name;this.slug=slug;this.description=description;this.active=active;updatedAt=Instant.now();}
    public UUID getId(){return id;} public String getName(){return name;} public String getSlug(){return slug;} public String getDescription(){return description;} public boolean isActive(){return active;}
}
