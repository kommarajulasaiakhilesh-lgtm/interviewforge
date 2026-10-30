package com.interviewforge.preparation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public final class CompanyGuidanceDtos {
    private CompanyGuidanceDtos() { }

    public record GuidanceInput(@NotNull UUID companyId, UUID roleId,
            @NotBlank @Size(max = 160) String title,
            @NotBlank @Size(max = 1000) String summary,
            @NotNull CompanyGuidanceType resourceType,
            @NotNull GuidanceSourceType sourceType,
            @NotBlank @Size(max = 160) String sourceLabel,
            @NotBlank @Size(max = 1000) @Pattern(regexp = "(?i)^https?://.+", message = "must be an http or https URL") String sourceUrl,
            LocalDate publishedOn, @NotNull LocalDate lastVerifiedOn, LocalDate validUntil,
            Boolean active) { }

    public record GuidanceResponse(UUID id, UUID companyId, String companyName,
            UUID roleId, String roleName, String title, String summary,
            CompanyGuidanceType resourceType, GuidanceSourceType sourceType,
            String sourceLabel, String sourceUrl, LocalDate publishedOn,
            LocalDate lastVerifiedOn, LocalDate validUntil, String freshness,
            boolean active) { }
}
