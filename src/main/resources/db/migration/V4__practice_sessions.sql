CREATE TABLE practice_sessions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES user_accounts(id) ON DELETE CASCADE,
    status VARCHAR(16) NOT NULL CHECK (status IN ('IN_PROGRESS', 'COMPLETED')),
    type VARCHAR(10) NOT NULL CHECK (type IN ('MCQ', 'TEXT')),
    question_count INTEGER NOT NULL CHECK (question_count BETWEEN 1 AND 20),
    answered_count INTEGER NOT NULL DEFAULT 0,
    correct_count INTEGER NOT NULL DEFAULT 0,
    score_percent NUMERIC(5,2),
    started_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,
    CONSTRAINT practice_session_counts_check CHECK (answered_count BETWEEN 0 AND question_count AND correct_count BETWEEN 0 AND answered_count),
    CONSTRAINT practice_session_completion_check CHECK (
        (status = 'IN_PROGRESS' AND completed_at IS NULL AND score_percent IS NULL AND answered_count < question_count)
        OR (status = 'COMPLETED' AND completed_at IS NOT NULL AND answered_count = question_count
            AND ((type = 'MCQ' AND score_percent IS NOT NULL) OR (type = 'TEXT' AND score_percent IS NULL)))),
    CONSTRAINT practice_session_score_check CHECK (score_percent IS NULL OR score_percent BETWEEN 0 AND 100)
);

CREATE TABLE practice_session_items (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL REFERENCES practice_sessions(id) ON DELETE CASCADE,
    question_id UUID NOT NULL REFERENCES questions(id),
    position INTEGER NOT NULL CHECK (position > 0),
    title VARCHAR(200) NOT NULL,
    question_text TEXT NOT NULL,
    difficulty VARCHAR(12) NOT NULL CHECK (difficulty IN ('EASY', 'MEDIUM', 'HARD')),
    type VARCHAR(10) NOT NULL CHECK (type IN ('MCQ', 'TEXT')),
    options_json JSONB,
    correct_option_index INTEGER,
    answer_text TEXT,
    explanation TEXT,
    selected_option_index INTEGER,
    submitted_answer_text TEXT,
    is_correct BOOLEAN,
    answered_at TIMESTAMPTZ,
    CONSTRAINT practice_item_choice_check CHECK (
        (type = 'MCQ' AND options_json IS NOT NULL AND jsonb_typeof(options_json) = 'array'
            AND jsonb_array_length(options_json) >= 2 AND correct_option_index IS NOT NULL
            AND correct_option_index >= 0 AND correct_option_index < jsonb_array_length(options_json))
        OR (type = 'TEXT' AND options_json IS NULL AND correct_option_index IS NULL)),
    CONSTRAINT practice_item_answer_check CHECK (
        (answered_at IS NULL AND selected_option_index IS NULL AND submitted_answer_text IS NULL AND is_correct IS NULL)
        OR (answered_at IS NOT NULL))
);

CREATE UNIQUE INDEX practice_session_question_unique ON practice_session_items(session_id, question_id);
CREATE UNIQUE INDEX practice_session_position_unique ON practice_session_items(session_id, position);
CREATE INDEX practice_sessions_user_started_idx ON practice_sessions(user_id, started_at DESC);
