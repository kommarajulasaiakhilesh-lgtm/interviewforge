CREATE TABLE experience_stories (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES user_accounts(id) ON DELETE CASCADE,
    title VARCHAR(160) NOT NULL,
    experience_type VARCHAR(20) NOT NULL CHECK (experience_type IN ('WORK', 'PROJECT', 'EDUCATION', 'VOLUNTEER', 'OTHER')),
    experience_context VARCHAR(1000),
    situation_text TEXT NOT NULL,
    task_text TEXT NOT NULL,
    actions_text TEXT NOT NULL,
    result_text TEXT NOT NULL,
    learning_text TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX experience_stories_owner_updated_idx ON experience_stories(user_id, updated_at DESC);

CREATE TABLE experience_story_skills (
    story_id UUID NOT NULL REFERENCES experience_stories(id) ON DELETE CASCADE,
    skill_id UUID NOT NULL REFERENCES skills(id),
    PRIMARY KEY (story_id, skill_id)
);

CREATE INDEX experience_story_skills_skill_idx ON experience_story_skills(skill_id);
