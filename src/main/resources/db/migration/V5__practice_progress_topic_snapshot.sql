ALTER TABLE practice_session_items ADD COLUMN topic_id UUID;

UPDATE practice_session_items attempt
SET topic_id = question.topic_id
FROM questions question
WHERE question.id = attempt.question_id;

ALTER TABLE practice_session_items ALTER COLUMN topic_id SET NOT NULL;
ALTER TABLE practice_session_items
    ADD CONSTRAINT practice_item_topic_fk FOREIGN KEY (topic_id) REFERENCES topics(id);
CREATE INDEX practice_session_items_topic_answered_idx ON practice_session_items(topic_id, answered_at);
