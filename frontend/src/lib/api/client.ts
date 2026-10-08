import type { ProblemDetail } from "./types";

/** Configure with VITE_API_BASE_URL (e.g. https://staging.example.com). No trailing slash needed. */
export const API_BASE_URL = String(
  import.meta.env["VITE_API_BASE_URL"] ?? "http://localhost:8080",
).replace(/\/+$/, "");

export class ApiError extends Error {
  status: number;
  problem?: ProblemDetail | undefined;
  constructor(status: number, message: string, problem?: ProblemDetail) {
    super(message);
    this.status = status;
    this.problem = problem;
  }
}

type TokenGetter = () => string | null;
let getToken: TokenGetter = () => null;
let onUnauthorized: (() => void) | null = null;

export function configureApiAuth(getter: TokenGetter) {
  getToken = getter;
}
export function setUnauthorizedHandler(fn: (() => void) | null) {
  onUnauthorized = fn;
}

type Query = Record<string, string | number | undefined | null>;

export async function api<T>(
  path: string,
  opts: { method?: string; body?: unknown; query?: Query; auth?: boolean } = {},
): Promise<T> {
  const { method = "GET", body, query, auth = true } = opts;
  const url = new URL(`${API_BASE_URL}/api/v1${path}`);
  if (query) {
    for (const [k, v] of Object.entries(query)) {
      if (v !== undefined && v !== null && v !== "") url.searchParams.set(k, String(v));
    }
  }
  const headers: Record<string, string> = { Accept: "application/json, application/problem+json" };
  if (body !== undefined) headers["Content-Type"] = "application/json";
  const token = auth ? getToken() : null;
  if (token) headers["Authorization"] = `Bearer ${token}`;

  let res: Response;
  try {
    res = await fetch(url, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : null,
    });
  } catch {
    throw new ApiError(0, `Can't reach the InterviewForge server at ${API_BASE_URL}.`);
  }

  if (res.status === 204) return undefined as T;
  const text = await res.text();
  let data: unknown = undefined;
  if (text) {
    try {
      data = JSON.parse(text);
    } catch {
      data = undefined;
    }
  }
  if (!res.ok) {
    const problem = (data ?? {}) as ProblemDetail;
    if (res.status === 401 && token && onUnauthorized) onUnauthorized();
    throw new ApiError(res.status, problem.detail || problem.title || res.statusText, problem);
  }
  return data as T;
}

/** Human-friendly explanation for any thrown error. */
export function describeError(err: unknown): { title: string; message: string } {
  if (err instanceof ApiError) {
    const detail = err.problem?.detail || err.message;
    switch (err.status) {
      case 0:
        return { title: "Server unreachable", message: `${err.message} Check that the backend is running and VITE_API_BASE_URL is correct.` };
      case 400:
        return { title: "Please check your input", message: detail || "Some fields are invalid." };
      case 401:
        return { title: "Not signed in", message: detail || "Your session has expired. Please sign in again." };
      case 403:
        return { title: "Access denied", message: "You don't have permission to do that." };
      case 404:
        return { title: "Not found", message: "This item doesn't exist or isn't available to you." };
      case 409:
        return { title: "Already exists", message: detail || "That value is already in use." };
      case 422:
        return { title: "Not enough questions", message: detail || "There aren't enough published questions for these settings. Try fewer questions or broader filters." };
      default:
        return { title: "Something went wrong", message: detail || `Request failed (${err.status}).` };
    }
  }
  return { title: "Something went wrong", message: err instanceof Error ? err.message : "Unknown error" };
}
