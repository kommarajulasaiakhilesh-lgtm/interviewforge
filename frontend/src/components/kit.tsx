import { forwardRef, type ButtonHTMLAttributes, type InputHTMLAttributes, type ReactNode, type SelectHTMLAttributes, type TextareaHTMLAttributes } from "react";
import { cn } from "@/lib/utils";
import { describeError } from "@/lib/api/client";

type BtnVariant = "primary" | "secondary" | "ghost" | "accent" | "danger";
const btnVariants: Record<BtnVariant, string> = {
  primary: "bg-primary text-primary-foreground hover:bg-primary/90",
  secondary: "bg-card text-secondary-foreground hover:bg-secondary",
  ghost: "!border-transparent !shadow-none text-foreground hover:bg-muted",
  accent: "bg-accent text-accent-foreground hover:bg-accent/85",
  danger: "bg-destructive text-destructive-foreground hover:bg-destructive/90",
};

export const Button = forwardRef<HTMLButtonElement, ButtonHTMLAttributes<HTMLButtonElement> & { variant?: BtnVariant; size?: "sm" | "md" }>(
  ({ className, variant = "primary", size = "md", ...p }, ref) => (
    <button
      ref={ref}
      className={cn(
        "inline-flex items-center justify-center gap-2 rounded-lg border-2 border-ink font-semibold shadow-[2px_2px_0_0_var(--ink)] transition-all hover:-translate-y-px hover:shadow-[3px_3px_0_0_var(--ink)] active:translate-y-0 active:shadow-none focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 focus-visible:ring-offset-background disabled:pointer-events-none disabled:opacity-50",
        size === "sm" ? "h-8 px-3 text-sm" : "h-10 px-4 text-sm",
        btnVariants[variant],
        className,
      )}
      {...p}
    />
  ),
);
Button.displayName = "Button";

const fieldCls =
  "w-full rounded-lg border border-input bg-card px-3 text-sm text-foreground placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring";

export const Input = forwardRef<HTMLInputElement, InputHTMLAttributes<HTMLInputElement>>(({ className, ...p }, ref) => (
  <input ref={ref} className={cn(fieldCls, "h-10", className)} {...p} />
));
Input.displayName = "Input";

export const Select = forwardRef<HTMLSelectElement, SelectHTMLAttributes<HTMLSelectElement>>(({ className, ...p }, ref) => (
  <select ref={ref} className={cn(fieldCls, "h-10", className)} {...p} />
));
Select.displayName = "Select";

export const Textarea = forwardRef<HTMLTextAreaElement, TextareaHTMLAttributes<HTMLTextAreaElement>>(({ className, ...p }, ref) => (
  <textarea ref={ref} className={cn(fieldCls, "min-h-28 py-2", className)} {...p} />
));
Textarea.displayName = "Textarea";

export function Field({ label, htmlFor, hint, children }: { label: string; htmlFor: string; hint?: string; children: ReactNode }) {
  return (
    <div className="space-y-1.5">
      <label htmlFor={htmlFor} className="text-sm font-semibold">{label}</label>
      {children}
      {hint && <p className="text-xs text-muted-foreground">{hint}</p>}
    </div>
  );
}

export function Card({ className, children }: { className?: string; children: ReactNode }) {
  return <div className={cn("rounded-xl border-2 border-ink bg-card p-5 text-card-foreground shadow-card transition-[box-shadow,transform] duration-200", className)}>{children}</div>;
}

type BadgeTone = "neutral" | "primary" | "success" | "warning" | "danger" | "accent";
const tones: Record<BadgeTone, string> = {
  neutral: "bg-muted text-muted-foreground",
  primary: "bg-primary/12 text-primary",
  success: "bg-success/15 text-success",
  warning: "bg-warning/25 text-warning-foreground",
  danger: "bg-destructive/12 text-destructive",
  accent: "bg-accent/30 text-accent-foreground",
};
export function Badge({ tone = "neutral", children }: { tone?: BadgeTone; children: ReactNode }) {
  return <span className={cn("inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold", tones[tone])}>{children}</span>;
}

export function difficultyTone(d: string): BadgeTone {
  return d === "EASY" ? "success" : d === "MEDIUM" ? "warning" : "danger";
}
export function levelTone(l: string): BadgeTone {
  return l === "STRONG" ? "success" : l === "DEVELOPING" ? "warning" : l === "NEEDS_WORK" ? "danger" : "neutral";
}

export function PageHeader({ title, description, actions }: { title: string; description?: string | undefined; actions?: ReactNode }) {
  return (
    <div className="mb-6 flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
      <div className="animate-rise">
        <p className="eyebrow text-primary">// InterviewForge</p>
        <h1 className="mt-1 text-3xl font-extrabold sm:text-4xl">{title}<span className="text-accent">.</span></h1>
        {description && <p className="mt-1 text-muted-foreground">{description}</p>}
      </div>
      {actions}
    </div>
  );
}

export function Loading({ label = "Loading…" }: { label?: string }) {
  return (
    <div role="status" className="flex items-center gap-3 py-8 text-muted-foreground">
      <span className="h-4 w-4 animate-spin rounded-full border-2 border-primary border-t-transparent" aria-hidden />
      {label}
    </div>
  );
}

export function Empty({ title, children }: { title: string; children?: ReactNode }) {
  return (
    <div className="rounded-xl border-2 border-dashed border-ink/30 bg-card/60 px-6 py-10 text-center">
      <p className="font-semibold">{title}</p>
      {children && <div className="mt-1 text-sm text-muted-foreground">{children}</div>}
    </div>
  );
}

export function ErrorBox({ error, onRetry }: { error: unknown; onRetry?: () => void }) {
  const { title, message } = describeError(error);
  return (
    <div role="alert" className="rounded-xl border border-destructive/30 bg-destructive/8 p-4">
      <p className="font-semibold text-destructive">{title}</p>
      <p className="mt-1 text-sm text-foreground/80">{message}</p>
      {onRetry && <Button variant="secondary" size="sm" className="mt-3" onClick={onRetry}>Try again</Button>}
    </div>
  );
}

export function Stat({ label, value, sub }: { label: string; value: ReactNode; sub?: string }) {
  return (
    <Card className="p-4">
      <p className="eyebrow text-muted-foreground">{label}</p>
      <p className="mt-2 font-display text-3xl font-extrabold">{value}</p>
      {sub && <p className="text-xs text-muted-foreground">{sub}</p>}
    </Card>
  );
}

export function Meter({ value, label }: { value: number | null | undefined; label: string }) {
  const v = Math.max(0, Math.min(100, Number(value ?? 0)));
  return (
    <div role="meter" aria-label={label} aria-valuenow={v} aria-valuemin={0} aria-valuemax={100} className="h-2.5 w-full overflow-hidden rounded-full border border-ink/20 bg-muted">
      <div className="h-full rounded-full bg-[linear-gradient(90deg,var(--primary),var(--accent))] transition-all duration-700" style={{ width: `${v}%` }} />
    </div>
  );
}

export function Pager({ page, totalPages, onChange }: { page: number; totalPages: number; onChange: (p: number) => void }) {
  if (totalPages <= 1) return null;
  return (
    <nav aria-label="Pagination" className="mt-4 flex items-center justify-between gap-2">
      <Button variant="secondary" size="sm" disabled={page <= 0} onClick={() => onChange(page - 1)}>Previous</Button>
      <span className="text-sm text-muted-foreground">Page {page + 1} of {totalPages}</span>
      <Button variant="secondary" size="sm" disabled={page >= totalPages - 1} onClick={() => onChange(page + 1)}>Next</Button>
    </nav>
  );
}
