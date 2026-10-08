export function pct(v: number | null | undefined, digits = 0) {
  if (v === null || v === undefined) return "—";
  return `${Number(v).toFixed(digits)}%`;
}
export function fmtDate(v: string | null | undefined) {
  if (!v) return "—";
  return new Date(v).toLocaleString(undefined, { dateStyle: "medium", timeStyle: "short" });
}
export function humanize(s: string) {
  return s.replace(/_/g, " ").toLowerCase().replace(/^\w/, (c) => c.toUpperCase());
}
