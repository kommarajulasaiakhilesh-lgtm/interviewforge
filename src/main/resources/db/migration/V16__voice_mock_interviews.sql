ALTER TABLE mock_interview_sessions
    ADD COLUMN interview_mode VARCHAR(12) NOT NULL DEFAULT 'TEXT'
        CHECK (interview_mode IN ('TEXT', 'VOICE')),
    ADD COLUMN audio_stored BOOLEAN NOT NULL DEFAULT FALSE
        CHECK (audio_stored = FALSE);

ALTER TABLE mock_interview_items
    ADD COLUMN transcript_consent_at TIMESTAMPTZ,
    ADD COLUMN follow_up_answer_text TEXT,
    ADD COLUMN follow_up_answered_at TIMESTAMPTZ,
    ADD COLUMN follow_up_transcript_consent_at TIMESTAMPTZ,
    ADD CONSTRAINT mock_interview_follow_up_answer_check CHECK (
        (follow_up_answer_text IS NULL AND follow_up_answered_at IS NULL)
        OR (follow_up_answer_text IS NOT NULL AND follow_up_answered_at IS NOT NULL)
    ),
    ADD CONSTRAINT mock_interview_follow_up_consent_check CHECK (
        follow_up_transcript_consent_at IS NULL OR follow_up_answered_at IS NOT NULL
    );
