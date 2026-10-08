ALTER TABLE user_profiles
    ADD COLUMN target_role_id UUID REFERENCES company_roles(id),
    ADD COLUMN interview_date DATE,
    ADD COLUMN weekly_study_minutes INTEGER CHECK (weekly_study_minutes BETWEEN 15 AND 1200),
    ADD COLUMN job_description TEXT;
