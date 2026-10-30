package com.interviewforge.preparation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class CompanyGuidanceServiceTest {
    private final LocalDate today = LocalDate.of(2026, 10, 30);

    @Test
    void marksGuidanceVerifiedWithinNinetyDaysAsRecent() {
        assertThat(CompanyGuidanceService.freshness(today.minusDays(90), null, today))
                .isEqualTo("RECENTLY_VERIFIED");
    }

    @Test
    void marksOlderGuidanceAsDueForReview() {
        assertThat(CompanyGuidanceService.freshness(today.minusDays(91), null, today))
                .isEqualTo("REVIEW_DUE");
    }

    @Test
    void expiryOverridesRecentVerification() {
        assertThat(CompanyGuidanceService.freshness(today, today.minusDays(1), today))
                .isEqualTo("EXPIRED");
    }
}
