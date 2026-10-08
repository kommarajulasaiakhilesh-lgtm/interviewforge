// Typed models mirroring the InterviewForge Spring Boot API (/api/v1).
// Source: README.md, docs/api.md and the backend *Dtos.java records.

export type UUID = string;
export type ISODate = string;
export type UserRole = "STUDENT" | "ADMIN";
export type Difficulty = "EASY" | "MEDIUM" | "HARD";
export type QuestionType = "MCQ" | "TEXT";
export type SessionStatus = "IN_PROGRESS" | "COMPLETED" | string;
export type TopicLevel = "NOT_ENOUGH_DATA" | "NEEDS_WORK" | "DEVELOPING" | "STRONG";

export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

// Accounts
export interface User {
  id: UUID;
  email: string;
  role: UserRole;
  displayName: string;
  createdAt: ISODate;
}
export interface AuthResponse {
  accessToken: string;
  tokenType: "Bearer" | string;
  expiresAt: ISODate;
  user: User;
}
export interface RegisterRequest {
  email: string;
  password: string;
  displayName: string;
}
export interface LoginRequest {
  email: string;
  password: string;
}

// Question bank (student-safe — never contains answer keys)
export interface Topic {
  id: UUID;
  name: string;
  slug: string;
  description: string | null;
  active: boolean;
}
export interface Tag {
  id: UUID;
  name: string;
  slug: string;
  active: boolean;
}
export interface QuestionSummary {
  id: UUID;
  title: string;
  questionText: string;
  difficulty: Difficulty;
  type: QuestionType;
  options: string[] | null;
  topic: Topic;
  tags: Tag[];
  createdAt: ISODate;
}
export interface QuestionFilters {
  topicId?: UUID | undefined;
  tagId?: UUID | undefined;
  difficulty?: Difficulty | undefined;
  search?: string | undefined;
  page?: number;
  size?: number;
}

// Practice
export interface CreatePracticeRequest {
  topicId?: UUID | undefined;
  difficulty?: Difficulty | undefined;
  type?: QuestionType | undefined;
  questionCount: number;
}
export interface PracticePrompt {
  questionId: UUID;
  position: number;
  title: string;
  questionText: string;
  difficulty: Difficulty;
  type: QuestionType;
  options: string[] | null;
}
export interface PracticeSessionSummary {
  sessionId: UUID;
  status: SessionStatus;
  startedAt: ISODate;
  completedAt: ISODate | null;
  type: QuestionType;
  questionCount: number;
  answeredCount: number;
  correctCount: number;
  scorePercent: number | null;
}
export interface CreatePracticeResponse {
  session: PracticeSessionSummary;
  questions: PracticePrompt[];
}
export type SubmitPracticeAnswer =
  | { questionId: UUID; selectedOptionIndex: number }
  | { questionId: UUID; answerText: string };
export interface PracticeAnswerResponse {
  sessionId: UUID;
  questionId: UUID;
  status: SessionStatus;
  correct: boolean | null;
  correctOptionIndex: number | null;
  answerText: string | null;
  explanation: string | null;
  answeredAt: ISODate;
  answeredCount: number;
  questionCount: number;
  scorePercent: number | null;
}
export interface PracticeQuestionReview {
  questionId: UUID;
  position: number;
  title: string;
  questionText: string;
  difficulty: Difficulty;
  type: QuestionType;
  options: string[] | null;
  selectedOptionIndex: number | null;
  submittedAnswerText: string | null;
  correct: boolean | null;
  correctOptionIndex: number | null;
  answerText: string | null;
  explanation: string | null;
  answeredAt: ISODate | null;
}
export interface PracticeSessionDetail {
  session: PracticeSessionSummary;
  questions: PracticeQuestionReview[];
}

// Progress
export interface ProgressOverview {
  completedSessions: number;
  inProgressSessions: number;
  mcqQuestionsAnswered: number;
  mcqCorrectAnswers: number;
  accuracyPercent: number | null;
  recentAttempts: PracticeSessionSummary[];
}
export interface TopicPerformance {
  topicId: UUID;
  topicName: string;
  questionsAnswered: number;
  correctAnswers: number;
  accuracyPercent: number | null;
  level: TopicLevel;
  lastAttemptAt: ISODate | null;
}
export interface TopicProgress {
  minimumAttemptsForRating: number;
  topics: TopicPerformance[];
}

// Company preparation
export interface Company {
  id: UUID;
  name: string;
  slug: string;
  description: string | null;
  active: boolean;
}
export interface Role {
  id: UUID;
  companyId: UUID;
  companyName: string;
  name: string;
  slug: string;
  description: string | null;
  active: boolean;
}
export interface TopicLink {
  topicId: UUID;
  topicName: string;
  relevance: number;
}
export interface SkillLink {
  skillId: UUID;
  skillName: string;
  importance: number;
  topics: TopicLink[];
}
export interface RoleDetail {
  role: Role;
  skills: SkillLink[];
}
export interface PreparationSetSummary {
  id: UUID;
  roleId: UUID;
  roleName: string;
  companyName: string;
  title: string;
  description: string | null;
  published: boolean;
  questionCount: number;
}
export interface PreparationSetDetail {
  set: PreparationSetSummary;
  questions: QuestionSummary[];
}

// Readiness
export interface TopicReadiness {
  topicId: UUID;
  topicName: string;
  relevance: number;
  combinedWeight: number;
  questionsAnswered: number;
  correctAnswers: number;
  accuracyPercent: number | null;
  reliableSample: boolean;
}
export interface SkillReadiness {
  skillId: UUID;
  skillName: string;
  importance: number;
  readinessScore: number | null;
  topics: TopicReadiness[];
}
export interface ReadinessAssessment {
  roleId: UUID;
  roleName: string;
  readinessScore: number | null;
  dataCoveragePercent: number;
  minimumAttemptsForReliableAccuracy: number;
  skills: SkillReadiness[];
  scoringMethod: string;
}
export interface StudyTask {
  topicId: UUID;
  topicName: string;
  skillName: string;
  importance: number;
  relevance: number;
  combinedWeight: number;
  questionsAnswered: number;
  currentAccuracyPercent: number | null;
  recommendedQuestions: number;
  estimatedMinutes: number;
  priority: number;
  reasons: string[];
}
export interface StudyPlan {
  roleId: UUID;
  roleName: string;
  targetAccuracyPercent: number;
  minimumAttemptsForReliableAccuracy: number;
  tasks: StudyTask[];
}

// Mock interviews
export interface MockSessionSummary {
  sessionId: UUID;
  roleId: UUID;
  roleName: string;
  status: SessionStatus;
  questionCount: number;
  answeredCount: number;
  startedAt: ISODate;
  completedAt: ISODate | null;
}
export interface MockPrompt {
  questionId: UUID;
  position: number;
  skillName: string;
  title: string;
  questionText: string;
  difficulty: Difficulty;
}
export interface CreateMockResponse {
  session: MockSessionSummary;
  questions: MockPrompt[];
}
export interface SubmitMockAnswer {
  questionId: UUID;
  answerText: string;
  selfRating: number;
}
export interface MockAnswerResponse {
  session: MockSessionSummary;
  questionId: UUID;
  selfRating: number;
}
export interface MockAnswerDetail extends MockPrompt {
  answerText: string | null;
  selfRating: number | null;
  answeredAt: ISODate | null;
}
export interface MockSkillResult {
  skillId: UUID;
  skillName: string;
  importance: number;
  questionsAnswered: number;
  questionCount: number;
  completionPercent: number;
  selfRatingAverage: number | null;
}
export interface MockSessionDetail {
  session: MockSessionSummary;
  questions: MockAnswerDetail[];
  skillResults: MockSkillResult[];
  assessmentMethod: string;
}

// RFC 7807 problem+json error body
export interface ProblemDetail {
  type?: string;
  title?: string;
  status?: number;
  detail?: string;
  instance?: string;
}
