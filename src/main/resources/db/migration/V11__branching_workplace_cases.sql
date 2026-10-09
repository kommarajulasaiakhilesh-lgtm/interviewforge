CREATE TABLE workplace_cases (
    id UUID PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    slug VARCHAR(180) NOT NULL UNIQUE,
    description VARCHAR(1000),
    role_id UUID NOT NULL REFERENCES company_roles(id),
    topic_id UUID NOT NULL REFERENCES topics(id),
    difficulty VARCHAR(12) NOT NULL CHECK (difficulty IN ('EASY', 'MEDIUM', 'HARD')),
    estimated_minutes INTEGER NOT NULL CHECK (estimated_minutes BETWEEN 1 AND 240),
    scenario_intro TEXT NOT NULL,
    learning_objective VARCHAR(1000),
    source_type VARCHAR(30),
    source_label VARCHAR(200),
    source_url VARCHAR(1000),
    source_verified_at TIMESTAMPTZ,
    published BOOLEAN NOT NULL DEFAULT FALSE,
    archived BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE workplace_case_nodes (
    id UUID PRIMARY KEY,
    case_id UUID NOT NULL REFERENCES workplace_cases(id) ON DELETE CASCADE,
    node_key VARCHAR(60) NOT NULL,
    node_type VARCHAR(12) NOT NULL CHECK (node_type IN ('START', 'DECISION', 'OUTCOME')),
    position INTEGER NOT NULL CHECK (position > 0),
    heading VARCHAR(200) NOT NULL,
    situation_text TEXT NOT NULL,
    lesson_text TEXT,
    UNIQUE(case_id, node_key),
    UNIQUE(case_id, position)
);

CREATE TABLE workplace_case_choices (
    id UUID PRIMARY KEY,
    case_id UUID NOT NULL,
    node_key VARCHAR(60) NOT NULL,
    choice_key VARCHAR(30) NOT NULL,
    position INTEGER NOT NULL CHECK (position > 0),
    choice_label VARCHAR(30) NOT NULL,
    choice_text VARCHAR(2000) NOT NULL,
    decision_quality VARCHAR(12) NOT NULL CHECK (decision_quality IN ('STRONG', 'VIABLE', 'RISKY')),
    quality_points INTEGER NOT NULL CHECK (quality_points BETWEEN 0 AND 2),
    explanation TEXT NOT NULL,
    when_appropriate TEXT,
    tradeoff_summary TEXT,
    misconception_label VARCHAR(160),
    next_node_key VARCHAR(60) NOT NULL,
    UNIQUE(case_id, node_key, choice_key),
    UNIQUE(case_id, node_key, position),
    FOREIGN KEY(case_id, node_key) REFERENCES workplace_case_nodes(case_id, node_key) ON DELETE CASCADE,
    FOREIGN KEY(case_id, next_node_key) REFERENCES workplace_case_nodes(case_id, node_key) ON DELETE CASCADE
);
CREATE INDEX workplace_cases_catalog_idx ON workplace_cases(role_id, topic_id, published, archived);
CREATE INDEX workplace_case_choices_node_idx ON workplace_case_choices(case_id, node_key);

CREATE TABLE workplace_case_sessions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES user_accounts(id) ON DELETE CASCADE,
    case_id UUID NOT NULL REFERENCES workplace_cases(id),
    case_title VARCHAR(200) NOT NULL,
    role_id UUID NOT NULL REFERENCES company_roles(id),
    role_name VARCHAR(120) NOT NULL,
    difficulty VARCHAR(12) NOT NULL CHECK (difficulty IN ('EASY', 'MEDIUM', 'HARD')),
    graph_snapshot JSONB NOT NULL,
    current_node_key VARCHAR(60) NOT NULL,
    status VARCHAR(16) NOT NULL CHECK (status IN ('IN_PROGRESS', 'COMPLETED')),
    decision_count INTEGER NOT NULL DEFAULT 0 CHECK (decision_count >= 0),
    total_points INTEGER NOT NULL DEFAULT 0 CHECK (total_points >= 0),
    started_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,
    CHECK ((status = 'IN_PROGRESS' AND completed_at IS NULL)
        OR (status = 'COMPLETED' AND completed_at IS NOT NULL))
);

CREATE TABLE workplace_case_decisions (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL REFERENCES workplace_case_sessions(id) ON DELETE CASCADE,
    position INTEGER NOT NULL CHECK (position > 0),
    node_key VARCHAR(60) NOT NULL,
    choice_key VARCHAR(30) NOT NULL,
    choice_label VARCHAR(30) NOT NULL,
    choice_text VARCHAR(2000) NOT NULL,
    decision_quality VARCHAR(12) NOT NULL CHECK (decision_quality IN ('STRONG', 'VIABLE', 'RISKY')),
    quality_points INTEGER NOT NULL CHECK (quality_points BETWEEN 0 AND 2),
    explanation TEXT NOT NULL,
    when_appropriate TEXT,
    tradeoff_summary TEXT,
    misconception_label VARCHAR(160),
    next_node_key VARCHAR(60) NOT NULL,
    answered_at TIMESTAMPTZ NOT NULL,
    UNIQUE(session_id, position),
    UNIQUE(session_id, node_key)
);
CREATE INDEX workplace_case_sessions_user_started_idx ON workplace_case_sessions(user_id, started_at DESC);
