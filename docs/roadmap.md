# Roadmap

1. Foundation and local development setup
2. Accounts, roles, and profiles
3. Topic and question bank
4. Practice sessions and scoring
5. Learning progress and analytics
6. Companies, roles, and preparation sets
7. Readiness scoring and study plans
8. Mock interviews
9. Production polish, security review, deployment, and monitoring (provider-neutral JAR deployment guidance)
10. Explainable learning loop: source provenance, option-level teaching, confidence calibration, spaced review, learner goals, and structured mock-interview rubrics
11. Branching workplace cases: authored decision paths, evolving constraints, trade-off feedback, misconception explanations, and transfer practice
12. Learner role selection and core interview-topic MCQs: alphabetized target-role choices, role-specific dashboard guidance, core concept practice, and starter question content
13. Job-requirement evidence map: connect saved job-description statements to role skills, practice topics, and learner-owned MCQ history
14. Personal experience bank: save private STAR stories from work, projects, education, or volunteering and map them to competencies
15. Market-aligned practice catalog: AI and machine learning, generative AI, data engineering, cloud platform engineering, cybersecurity, quality engineering, additional original MCQs, and company-associated role-preparation tracks

Phases 1–18 have implementation commits or code in the working phase. Phase 15's V14 migration still needs application to the local PostgreSQL database. Phase 16 adds adaptive study-plan difficulty and session sizing. Phase 17 adds deterministic written-answer coaching without external AI calls. Phase 18 adds sourced company/job guidance using official outbound links, source/freshness metadata, and soft-deactivated admin records; migration V15 must be applied at the next backend startup. Remaining add-on phases:

18. Sourced job and company guidance: implemented; local migration/runtime verification remains.
19. Voice mock interviews: spoken questions and follow-ups, transcript review, text fallback, and explicit audio privacy controls.
20. Isolated coding practice: timed exercises and test results using a restricted execution service outside the main API.
21. Goals and reminders: weekly goals, owner-scoped milestones, and opt-in in-app reminders.

These are separate phases so provider costs, privacy, and sandbox requirements can be evaluated before those integrations are enabled.
