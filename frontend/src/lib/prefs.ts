// UI preference only (not account data): the role a student is currently preparing for.
const KEY = "interviewforge.focusRole";

export interface FocusRole {
  roleId: string;
  roleName: string;
  companyName: string;
}

export function getFocusRole(): FocusRole | null {
  if (typeof window === "undefined") return null;
  try {
    const raw = window.localStorage.getItem(KEY);
    return raw ? (JSON.parse(raw) as FocusRole) : null;
  } catch {
    return null;
  }
}

export function setFocusRole(r: FocusRole) {
  try {
    window.localStorage.setItem(KEY, JSON.stringify(r));
  } catch {
    /* ignore */
  }
}

export function clearFocusRole() {
  try {
    window.localStorage.removeItem(KEY);
  } catch {
    /* ignore */
  }
}
