import { useSyncExternalStore } from "react";
import { configureApiAuth } from "./api/client";
import type { AuthResponse, User } from "./api/types";

// Session-scoped token storage (sessionStorage). Passwords are never stored.
const KEY = "interviewforge.session";

export interface Session {
  accessToken: string;
  expiresAt: string;
  user: User;
}

let current: Session | null = null;
let loaded = false;
const listeners = new Set<() => void>();

function load() {
  if (loaded || typeof window === "undefined") return;
  loaded = true;
  try {
    const raw = window.sessionStorage.getItem(KEY);
    if (raw) {
      const s = JSON.parse(raw) as Session;
      if (new Date(s.expiresAt).getTime() > Date.now()) current = s;
      else window.sessionStorage.removeItem(KEY);
    }
  } catch {
    current = null;
  }
}

export function getSession(): Session | null {
  load();
  if (current && new Date(current.expiresAt).getTime() <= Date.now()) setSession(null);
  return current;
}

export function setSession(s: Session | null) {
  current = s;
  loaded = true;
  if (typeof window !== "undefined") {
    if (s) window.sessionStorage.setItem(KEY, JSON.stringify(s));
    else window.sessionStorage.removeItem(KEY);
  }
  listeners.forEach((l) => l());
}

export function sessionFromAuth(r: AuthResponse): Session {
  return { accessToken: r.accessToken, expiresAt: r.expiresAt, user: r.user };
}

export function updateSessionUser(user: User) {
  if (current) setSession({ ...current, user });
}

configureApiAuth(() => getSession()?.accessToken ?? null);

export function useSession(): Session | null {
  return useSyncExternalStore(
    (cb) => {
      listeners.add(cb);
      return () => listeners.delete(cb);
    },
    getSession,
    () => null,
  );
}
