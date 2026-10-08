ALTER TABLE questions
    ADD COLUMN evaluation_criteria TEXT,
    ADD COLUMN follow_up_prompt TEXT;

ALTER TABLE mock_interview_items
    ADD COLUMN evaluation_criteria TEXT,
    ADD COLUMN follow_up_prompt TEXT;
