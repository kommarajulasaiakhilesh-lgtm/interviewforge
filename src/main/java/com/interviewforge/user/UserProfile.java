package com.interviewforge.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    private UUID id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Column(name = "display_name", nullable = false, length = 80)
    private String displayName;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    @Column(name = "target_role_id") private UUID targetRoleId;
    @Column(name = "interview_date") private LocalDate interviewDate;
    @Column(name = "weekly_study_minutes") private Integer weeklyStudyMinutes;
    @Column(name = "job_description", columnDefinition = "text") private String jobDescription;

    protected UserProfile() {
    }

    public UserProfile(UserAccount user, String displayName) {
        this.user = user;
        this.displayName = displayName;
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
    public UUID getTargetRoleId() { return targetRoleId; }
    public LocalDate getInterviewDate() { return interviewDate; }
    public Integer getWeeklyStudyMinutes() { return weeklyStudyMinutes; }
    public String getJobDescription() { return jobDescription; }

    public void updatePreparation(UUID targetRoleId, LocalDate interviewDate, Integer weeklyStudyMinutes, String jobDescription) {
        this.targetRoleId = targetRoleId;
        this.interviewDate = interviewDate;
        this.weeklyStudyMinutes = weeklyStudyMinutes;
        this.jobDescription = jobDescription == null || jobDescription.isBlank() ? null : jobDescription.trim();
        this.updatedAt = Instant.now();
    }

    public void updateDisplayName(String displayName) {
        this.displayName = displayName;
        this.updatedAt = Instant.now();
    }
}
