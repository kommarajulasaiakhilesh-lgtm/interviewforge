package com.interviewforge.mockinterview;

final class MockInterviewPrivacyPolicy {
    static final String NOTICE = "InterviewForge accepts transcript text only and never uploads or stores interview audio. "
            + "For voice sessions, review each transcript and explicitly consent before saving it. "
            + "Speech recognition is performed by the client browser and may be subject to that browser's own processing policies. "
            + "Typed answers are available as a fallback. You can delete the session and its transcripts at any time.";

    private MockInterviewPrivacyPolicy() { }

    static void requireTranscriptConsent(InterviewMode mode, Boolean consent) {
        if (mode == InterviewMode.VOICE && !Boolean.TRUE.equals(consent)) {
            throw new IllegalArgumentException("Explicit consent is required before saving a voice transcript.");
        }
    }
}
