CREATE TABLE mock_interview_sessions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES user_accounts(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES company_roles(id),
    role_name VARCHAR(120) NOT NULL,
    status VARCHAR(16) NOT NULL CHECK (status IN ('IN_PROGRESS', 'COMPLETED')),
    question_count INTEGER NOT NULL CHECK (question_count BETWEEN 1 AND 20),
    answered_count INTEGER NOT NULL DEFAULT 0 CHECK (answered_count BETWEEN 0 AND question_count),
    started_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,
    CHECK ((status = 'IN_PROGRESS' AND completed_at IS NULL AND answered_count < question_count)
        OR (status = 'COMPLETED' AND completed_at IS NOT NULL AND answered_count = question_count))
);

CREATE TABLE mock_interview_items (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL REFERENCES mock_interview_sessions(id) ON DELETE CASCADE,
    question_id UUID NOT NULL REFERENCES questions(id),
    topic_id UUID NOT NULL REFERENCES topics(id),
    skill_id UUID NOT NULL REFERENCES skills(id),
    skill_name VARCHAR(120) NOT NULL,
    skill_importance INTEGER NOT NULL CHECK (skill_importance BETWEEN 1 AND 5),
    topic_relevance INTEGER NOT NULL CHECK (topic_relevance BETWEEN 1 AND 5),
    position INTEGER NOT NULL CHECK (position > 0),
    title VARCHAR(200) NOT NULL,
    question_text TEXT NOT NULL,
    difficulty VARCHAR(12) NOT NULL CHECK (difficulty IN ('EASY', 'MEDIUM', 'HARD')),
    submitted_answer_text TEXT,
    self_rating INTEGER CHECK (self_rating BETWEEN 1 AND 5),
    answered_at TIMESTAMPTZ,
    CHECK ((submitted_answer_text IS NULL AND self_rating IS NULL AND answered_at IS NULL)
        OR (submitted_answer_text IS NOT NULL AND self_rating IS NOT NULL AND answered_at IS NOT NULL)),
    UNIQUE(session_id, question_id),
    UNIQUE(session_id, position)
);

CREATE INDEX mock_interview_sessions_user_started_idx ON mock_interview_sessions(user_id, started_at DESC);
CREATE INDEX mock_interview_items_session_skill_idx ON mock_interview_items(session_id, skill_id);
