# Database strategy

PostgreSQL is the primary database. Flyway SQL migrations under `src/main/resources/db/migration` are the source of truth for schema changes. Hibernate uses `ddl-auto: validate` and must not create or update tables automatically. Store timestamps in UTC. Add tables only as their owning product phase is implemented.

Phase 1 added no domain tables. Phase 2 adds `user_accounts`, `user_profiles`, and `auth_sessions`. Session tokens are stored as SHA-256 hashes. Accounts and their profiles/sessions use UUID keys and cascading foreign keys for account-owned data.

Phase 3 adds `topics`, `question_tags`, `questions`, and `question_tag_assignments`. Catalog entries are deactivated instead of deleted so existing question references remain valid. Question deletion is archival. MCQ choices are JSONB and the correct option is an index constrained to the stored choices; student responses omit the answer key and explanation.

Phase 4 adds `practice_sessions` and `practice_session_items`. Session items snapshot the prompt, options, and key at start so later question edits do not alter reviews. Items store submitted answers and grading timestamps. MCQ sessions store a percentage score; TEXT sessions remain unscored.

Phase 5 adds a `topic_id` snapshot to practice items. The migration backfills existing attempt items from their linked question, then requires the topic reference. New sessions persist the question's topic at the time of session creation so subsequent question retagging does not move historical accuracy between topics.

Phase 6 adds `companies`, `company_roles`, `skills`, `role_skills`, `skill_topics`, `preparation_sets`, and `preparation_set_questions`. Link weights are integers from 1 to 5. Catalogs and sets are deactivated/archived rather than hard-deleted; curated set questions reference the existing question bank and are exposed only while published.

Phase 8 adds `mock_interview_sessions` and `mock_interview_items`. Each session snapshots the selected role name; each item snapshots its prompt, topic/skill attribution, and mapping weights. Answer text and required 1–5 self-rating are saved once per question. Session ownership cascades on account deletion; referenced role, question, topic, and skill records are retained by foreign keys.

Phase 10 expands question authoring with per-choice explanations, theory notes, workplace examples, misconception labels, real-world scenario context, interview stage, source provenance, verification timestamp, and written-interview evaluation criteria/follow-up prompts. Practice and mock interview attempts snapshot the teaching and rubric content so edits do not rewrite previous learning sessions. Practice items also store optional confidence ratings.

Phase 10 adds `user_question_reviews`, keyed by user and question, to schedule spaced MCQ retries from the learner's correctness and confidence history. Entries cascade with account/question deletion. `user_profiles` gains an optional target role, interview date, weekly study minutes, and private job description. These preferences are owner-only; the job description is not sent to an external provider.

Phase 11 adds `workplace_cases`, ordered `workplace_case_nodes`, and `workplace_case_choices` to author a validated acyclic decision graph. Each edge has curated decision-quality, explanation, trade-off, and misconception metadata. `workplace_case_sessions` stores an immutable JSONB snapshot of the graph and the current step; `workplace_case_decisions` stores the selected choice and the feedback snapshot for owner-only history. Case authoring replaces a graph transactionally, and archive is soft-delete so existing sessions remain reviewable.
