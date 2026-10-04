CREATE TABLE companies (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    slug VARCHAR(140) NOT NULL UNIQUE,
    description VARCHAR(1000),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE company_roles (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL REFERENCES companies(id),
    name VARCHAR(120) NOT NULL,
    slug VARCHAR(140) NOT NULL,
    description VARCHAR(1000),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    UNIQUE(company_id, slug),
    UNIQUE(company_id, name)
);

CREATE TABLE skills (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    slug VARCHAR(140) NOT NULL UNIQUE,
    description VARCHAR(1000),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE role_skills (
    id UUID PRIMARY KEY,
    role_id UUID NOT NULL REFERENCES company_roles(id),
    skill_id UUID NOT NULL REFERENCES skills(id),
    importance INTEGER NOT NULL CHECK (importance BETWEEN 1 AND 5),
    UNIQUE(role_id, skill_id)
);

CREATE TABLE skill_topics (
    id UUID PRIMARY KEY,
    skill_id UUID NOT NULL REFERENCES skills(id),
    topic_id UUID NOT NULL REFERENCES topics(id),
    relevance INTEGER NOT NULL CHECK (relevance BETWEEN 1 AND 5),
    UNIQUE(skill_id, topic_id)
);

CREATE TABLE preparation_sets (
    id UUID PRIMARY KEY,
    role_id UUID NOT NULL REFERENCES company_roles(id),
    title VARCHAR(160) NOT NULL,
    description VARCHAR(1000),
    published BOOLEAN NOT NULL DEFAULT FALSE,
    archived BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    UNIQUE(role_id, title)
);

CREATE TABLE preparation_set_questions (
    id UUID PRIMARY KEY,
    set_id UUID NOT NULL REFERENCES preparation_sets(id) ON DELETE CASCADE,
    question_id UUID NOT NULL REFERENCES questions(id),
    position INTEGER NOT NULL CHECK (position > 0),
    UNIQUE(set_id, question_id),
    UNIQUE(set_id, position)
);

CREATE INDEX company_roles_company_active_idx ON company_roles(company_id, active);
CREATE INDEX role_skills_skill_id_idx ON role_skills(skill_id);
CREATE INDEX skill_topics_topic_id_idx ON skill_topics(topic_id);
CREATE INDEX preparation_sets_role_published_idx ON preparation_sets(role_id, published, archived);
