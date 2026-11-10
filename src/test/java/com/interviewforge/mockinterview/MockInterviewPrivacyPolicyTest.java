package com.interviewforge.mockinterview;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class MockInterviewPrivacyPolicyTest {
    @Test
    void voiceTranscriptRequiresExplicitConsent() {
        assertThrows(IllegalArgumentException.class,
                () -> MockInterviewPrivacyPolicy.requireTranscriptConsent(InterviewMode.VOICE, null));
        assertThrows(IllegalArgumentException.class,
                () -> MockInterviewPrivacyPolicy.requireTranscriptConsent(InterviewMode.VOICE, false));
        assertDoesNotThrow(() -> MockInterviewPrivacyPolicy.requireTranscriptConsent(InterviewMode.VOICE, true));
    }

    @Test
    void typedInterviewDoesNotRequireVoiceTranscriptConsent() {
        assertDoesNotThrow(() -> MockInterviewPrivacyPolicy.requireTranscriptConsent(InterviewMode.TEXT, null));
    }
}
