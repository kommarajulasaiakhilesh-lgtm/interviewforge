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
  reviewQueue: (limit = 20) => api<T.ReviewQueueItem[]>("/practice/review-queue", { query: { limit } }),
  createReviewSession: (questionCount = 10) => api<T.CreatePracticeResponse>("/practice/review-queue/sessions", { method: "POST", query: { questionCount } }),
};

export const preparationGoalApi = {
  get: () => api<T.PreparationGoal>("/users/me/preparation"),
  update: (goal: T.PreparationGoal) => api<T.PreparationGoal>("/users/me/preparation", { method: "PATCH", body: goal }),
};

export const workplaceCaseApi = {
  browse: (filters: { roleId?: string; topicId?: string; difficulty?: T.Difficulty; page?: number; size?: number } = {}) =>
    api<T.Page<T.WorkplaceCaseSummary>>("/workplace-cases", { query: { ...filters } }),
  get: (caseId: string) => api<T.WorkplaceCaseSummary>(`/workplace-cases/${caseId}`),
  start: (caseId: string) => api<T.StartWorkplaceCaseResponse>(`/workplace-cases/${caseId}/sessions`, { method: "POST" }),
  decide: (sessionId: string, nodeKey: string, choiceKey: string) =>
    api<T.WorkplaceCaseDecisionResponse>(`/workplace-cases/sessions/${sessionId}/decisions`, {
      method: "POST",
      body: { nodeKey, choiceKey },
    }),
  session: (sessionId: string) => api<T.WorkplaceCaseSessionDetail>(`/workplace-cases/sessions/${sessionId}`),
  history: (page = 0, size = 20) =>
    api<T.Page<T.WorkplaceCaseSessionSummary>>("/workplace-cases/sessions", { query: { page, size } }),
};

export const adminWorkplaceCaseApi = {
  browse: (page = 0, size = 20) => api<T.Page<T.AdminWorkplaceCaseDetail>>("/admin/workplace-cases", { query: { page, size } }),
  get: (caseId: string) => api<T.AdminWorkplaceCaseDetail>(`/admin/workplace-cases/${caseId}`),
  create: (body: T.AdminWorkplaceCaseInput) => api<T.AdminWorkplaceCaseDetail>("/admin/workplace-cases", { method: "POST", body }),
  update: (caseId: string, body: T.AdminWorkplaceCaseInput) =>
    api<T.AdminWorkplaceCaseDetail>(`/admin/workplace-cases/${caseId}`, { method: "PUT", body }),
  archive: (caseId: string) => api<void>(`/admin/workplace-cases/${caseId}`, { method: "DELETE" }),
};

export const progressApi = {
  overview: () => api<T.ProgressOverview>("/progress/overview"),
  topics: () => api<T.TopicProgress>("/progress/topics"),
  learningInsights: () => api<T.LearningInsights>("/progress/learning-insights"),
  attempts: (page = 0, size = 20) => api<T.Page<T.PracticeSessionSummary>>("/progress/attempts", { query: { page, size } }),
};

export const prepApi = {
  companies: () => api<T.Company[]>("/companies"),
  roles: (companyId: string) => api<T.Role[]>(`/companies/${companyId}/roles`),
  allRoles: async () => {
    const companies = await prepApi.companies();
    const roles = (await Promise.all(companies.map((company) => prepApi.roles(company.id))))
      .flat()
      .filter((role) => role.active)
      .sort((a, b) => a.name.localeCompare(b.name) || a.companyName.localeCompare(b.companyName));
    return roles;
  },
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
