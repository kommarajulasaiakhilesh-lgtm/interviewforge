import { api } from "./client";
import type * as T from "./types";

export const authApi = {
  register: (b: T.RegisterRequest) => api<T.AuthResponse>("/auth/register", { method: "POST", body: b, auth: false }),
  login: (b: T.LoginRequest) => api<T.AuthResponse>("/auth/login", { method: "POST", body: b, auth: false }),
  logout: () => api<void>("/auth/logout", { method: "POST" }),
  me: () => api<T.User>("/users/me"),
  updateMe: (displayName: string) => api<T.User>("/users/me", { method: "PATCH", body: { displayName } }),
};

export const catalogApi = {
  topics: () => api<T.Topic[]>("/topics"),
  tags: () => api<T.Tag[]>("/tags"),
  questions: (f: T.QuestionFilters) => api<T.Page<T.QuestionSummary>>("/questions", { query: { ...f } }),
  question: (id: string) => api<T.QuestionSummary>(`/questions/${id}`),
};

export const practiceApi = {
  create: (b: T.CreatePracticeRequest) => api<T.CreatePracticeResponse>("/practice/sessions", { method: "POST", body: b }),
  answer: (sessionId: string, b: T.SubmitPracticeAnswer) =>
    api<T.PracticeAnswerResponse>(`/practice/sessions/${sessionId}/answers`, { method: "POST", body: b }),
  get: (sessionId: string) => api<T.PracticeSessionDetail>(`/practice/sessions/${sessionId}`),
  list: (page = 0, size = 20) => api<T.Page<T.PracticeSessionSummary>>("/practice/sessions", { query: { page, size } }),
};

export const progressApi = {
  overview: () => api<T.ProgressOverview>("/progress/overview"),
  topics: () => api<T.TopicProgress>("/progress/topics"),
  attempts: (page = 0, size = 20) => api<T.Page<T.PracticeSessionSummary>>("/progress/attempts", { query: { page, size } }),
};

export const prepApi = {
  companies: () => api<T.Company[]>("/companies"),
  roles: (companyId: string) => api<T.Role[]>(`/companies/${companyId}/roles`),
  role: (roleId: string) => api<T.RoleDetail>(`/roles/${roleId}`),
  sets: (roleId: string) => api<T.PreparationSetSummary[]>("/preparation-sets", { query: { roleId } }),
  set: (setId: string) => api<T.PreparationSetDetail>(`/preparation-sets/${setId}`),
};

export const readinessApi = {
  get: (roleId: string) => api<T.ReadinessAssessment>(`/readiness/roles/${roleId}`),
  plan: (roleId: string, maxTasks = 10) =>
    api<T.StudyPlan>(`/readiness/roles/${roleId}/study-plan`, { query: { maxTasks } }),
};

export const mockApi = {
  create: (roleId: string, questionCount: number) =>
    api<T.CreateMockResponse>("/mock-interviews", { method: "POST", body: { roleId, questionCount } }),
  answer: (sessionId: string, b: T.SubmitMockAnswer) =>
    api<T.MockAnswerResponse>(`/mock-interviews/${sessionId}/answers`, { method: "POST", body: b }),
  get: (sessionId: string) => api<T.MockSessionDetail>(`/mock-interviews/${sessionId}`),
  list: (page = 0, size = 20) => api<T.Page<T.MockSessionSummary>>("/mock-interviews", { query: { page, size } }),
};
