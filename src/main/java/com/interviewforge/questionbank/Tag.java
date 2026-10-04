package com.interviewforge.questionbank;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "question_tags")
public class Tag {
    @Id
    private UUID id;
    @Column(nullable = false, unique = true, length = 60)
    private String name;
    @Column(nullable = false, unique = true, length = 80)
    private String slug;
    @Column(nullable = false)
    private boolean active;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Tag() { }
    public Tag(String name, String slug) {
        this.id = UUID.randomUUID(); this.name = name; this.slug = slug; this.active = true;
        this.createdAt = Instant.now(); this.updatedAt = this.createdAt;
    }
    public void update(String name, String slug, boolean active) {
        this.name = name; this.slug = slug; this.active = active; this.updatedAt = Instant.now();
    }
    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
