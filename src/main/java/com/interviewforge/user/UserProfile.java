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

    public void updateDisplayName(String displayName) {
        this.displayName = displayName;
        this.updatedAt = Instant.now();
    }
}
