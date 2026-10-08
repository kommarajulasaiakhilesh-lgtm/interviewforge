ALTER TABLE questions
    ADD COLUMN option_explanations_json JSONB,
    ADD COLUMN theory_notes TEXT,
    ADD COLUMN workplace_example TEXT,
    ADD COLUMN misconception_label VARCHAR(160),
    ADD COLUMN scenario_context TEXT,
    ADD COLUMN interview_stage VARCHAR(40),
    ADD COLUMN source_type VARCHAR(30),
    ADD COLUMN source_label VARCHAR(200),
    ADD COLUMN source_url VARCHAR(1000),
    ADD COLUMN source_verified_at TIMESTAMPTZ;

ALTER TABLE practice_session_items
    ADD COLUMN option_explanations_json JSONB,
    ADD COLUMN theory_notes TEXT,
    ADD COLUMN workplace_example TEXT,
    ADD COLUMN misconception_label VARCHAR(160),
    ADD COLUMN scenario_context TEXT,
    ADD COLUMN interview_stage VARCHAR(40),
    ADD COLUMN source_type VARCHAR(30),
    ADD COLUMN source_label VARCHAR(200),
    ADD COLUMN source_url VARCHAR(1000),
    ADD COLUMN source_verified_at TIMESTAMPTZ,
    ADD COLUMN confidence_rating INTEGER CHECK (confidence_rating BETWEEN 1 AND 5);

CREATE TABLE user_question_reviews (
    user_id UUID NOT NULL REFERENCES user_accounts(id) ON DELETE CASCADE,
    question_id UUID NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    due_at TIMESTAMPTZ NOT NULL,
    interval_days INTEGER NOT NULL DEFAULT 1 CHECK (interval_days BETWEEN 1 AND 365),
    consecutive_correct INTEGER NOT NULL DEFAULT 0 CHECK (consecutive_correct >= 0),
    last_confidence INTEGER CHECK (last_confidence BETWEEN 1 AND 5),
    last_attempt_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (user_id, question_id)
);
CREATE INDEX user_question_reviews_due_idx ON user_question_reviews(user_id, due_at);
