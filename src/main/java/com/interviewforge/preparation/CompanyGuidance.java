package com.interviewforge.preparation;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "company_guidance")
public class CompanyGuidance {
    @Id private UUID id;
    @Column(name = "company_id", nullable = false) private UUID companyId;
    @Column(name = "role_id") private UUID roleId;
    @Column(nullable = false, length = 160) private String title;
    @Column(nullable = false, length = 1000) private String summary;
    @Column(name = "resource_type", nullable = false, length = 40) @Enumerated(EnumType.STRING)
    private CompanyGuidanceType resourceType;
    @Column(name = "source_type", nullable = false, length = 40) @Enumerated(EnumType.STRING)
    private GuidanceSourceType sourceType;
    @Column(name = "source_label", nullable = false, length = 160) private String sourceLabel;
    @Column(name = "source_url", nullable = false, length = 1000) private String sourceUrl;
    @Column(name = "published_on") private LocalDate publishedOn;
    @Column(name = "last_verified_on", nullable = false) private LocalDate lastVerifiedOn;
    @Column(name = "valid_until") private LocalDate validUntil;
    @Column(nullable = false) private boolean active;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected CompanyGuidance() { }

    public CompanyGuidance(UUID companyId, UUID roleId, String title, String summary,
            CompanyGuidanceType resourceType, GuidanceSourceType sourceType, String sourceLabel,
            String sourceUrl, LocalDate publishedOn, LocalDate lastVerifiedOn, LocalDate validUntil) {
        this.id = UUID.randomUUID();
        this.update(companyId, roleId, title, summary, resourceType, sourceType, sourceLabel,
                sourceUrl, publishedOn, lastVerifiedOn, validUntil, true);
        this.createdAt = Instant.now();
    }

    public void update(UUID companyId, UUID roleId, String title, String summary,
            CompanyGuidanceType resourceType, GuidanceSourceType sourceType, String sourceLabel,
            String sourceUrl, LocalDate publishedOn, LocalDate lastVerifiedOn, LocalDate validUntil,
            boolean active) {
        this.companyId = companyId; this.roleId = roleId; this.title = title; this.summary = summary;
        this.resourceType = resourceType; this.sourceType = sourceType; this.sourceLabel = sourceLabel;
        this.sourceUrl = sourceUrl; this.publishedOn = publishedOn; this.lastVerifiedOn = lastVerifiedOn;
        this.validUntil = validUntil; this.active = active; this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getCompanyId() { return companyId; }
    public UUID getRoleId() { return roleId; }
    public String getTitle() { return title; }
    public String getSummary() { return summary; }
    public CompanyGuidanceType getResourceType() { return resourceType; }
    public GuidanceSourceType getSourceType() { return sourceType; }
    public String getSourceLabel() { return sourceLabel; }
    public String getSourceUrl() { return sourceUrl; }
    public LocalDate getPublishedOn() { return publishedOn; }
    public LocalDate getLastVerifiedOn() { return lastVerifiedOn; }
    public LocalDate getValidUntil() { return validUntil; }
    public boolean isActive() { return active; }
}
