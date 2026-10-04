CREATE TABLE topics (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(120) NOT NULL UNIQUE,
    description VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE question_tags (
    id UUID PRIMARY KEY,
    name VARCHAR(60) NOT NULL UNIQUE,
    slug VARCHAR(80) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE questions (
    id UUID PRIMARY KEY,
    topic_id UUID NOT NULL REFERENCES topics(id),
    title VARCHAR(200) NOT NULL,
    question_text TEXT NOT NULL,
    difficulty VARCHAR(12) NOT NULL CHECK (difficulty IN ('EASY', 'MEDIUM', 'HARD')),
    type VARCHAR(10) NOT NULL CHECK (type IN ('MCQ', 'TEXT')),
    options_json JSONB,
    correct_option_index INTEGER,
    answer_text TEXT,
    explanation TEXT,
    published BOOLEAN NOT NULL DEFAULT FALSE,
    archived BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT questions_mcq_options_check CHECK (
        (type = 'MCQ' AND jsonb_typeof(options_json) = 'array' AND jsonb_array_length(options_json) >= 2
            AND correct_option_index IS NOT NULL AND correct_option_index >= 0
            AND correct_option_index < jsonb_array_length(options_json))
        OR (type = 'TEXT' AND options_json IS NULL AND correct_option_index IS NULL)
    )
);

CREATE TABLE question_tag_assignments (
    question_id UUID NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    tag_id UUID NOT NULL REFERENCES question_tags(id),
    PRIMARY KEY (question_id, tag_id)
);

CREATE INDEX questions_topic_id_idx ON questions(topic_id);
CREATE INDEX questions_visibility_idx ON questions(published, archived, created_at DESC);
CREATE INDEX question_tag_assignments_tag_id_idx ON question_tag_assignments(tag_id);
