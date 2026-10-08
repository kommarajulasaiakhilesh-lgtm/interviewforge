import { Link } from "@tanstack/react-router";
import type { ReactNode } from "react";
import { API_BASE_URL } from "@/lib/api/client";

export function AuthShell({ title, subtitle, children, footer }: { title: string; subtitle: string; children: ReactNode; footer: ReactNode }) {
  return (
    <div className="grid min-h-screen lg:grid-cols-[1.1fr_1fr]">
      <aside className="relative hidden overflow-hidden bg-ink p-12 text-ink-foreground lg:flex lg:flex-col lg:justify-between">
        <Link to="/" className="flex items-center gap-2 font-display text-xl font-extrabold">
          <span className="grid h-9 w-9 place-items-center rounded-md bg-accent font-mono text-sm text-accent-foreground">IF</span>
          InterviewForge
        </Link>
        <div className="animate-rise">
          <p className="eyebrow text-accent">// Practice · Measure · Improve</p>
          <h2 className="mt-4 max-w-md text-5xl font-extrabold leading-[1.05]">
            Every interview is <span className="text-accent">forged</span> before it happens.
          </h2>
          <div className="mt-10 grid max-w-md grid-cols-3 gap-3">
            {["Practice", "Readiness", "Mock"].map((t, i) => (
              <div key={t} className="rounded-lg border border-ink-foreground/15 p-3">
                <p className="font-mono text-xs text-ink-foreground/50">0{i + 1}</p>
                <p className="mt-1 text-sm font-semibold">{t}</p>
              </div>
            ))}
          </div>
        </div>
        <p className="font-mono text-xs text-ink-foreground/50">Server: {API_BASE_URL}</p>
        <div aria-hidden className="pointer-events-none absolute -right-24 -top-24 h-80 w-80 rounded-full border-[40px] border-primary/30" />
      </aside>
      <div className="flex items-center justify-center px-4 py-10">
        <div className="w-full max-w-md animate-rise">
          <Link to="/" className="mb-6 block text-center font-display text-xl font-extrabold lg:hidden">
            Interview<span className="text-primary">Forge</span>
          </Link>
          <div className="rounded-2xl border-2 border-ink bg-card p-6 shadow-card sm:p-8">
            <p className="eyebrow text-primary">// Account</p>
            <h1 className="mt-1 text-3xl font-extrabold">{title}</h1>
            <p className="mt-1 text-sm text-muted-foreground">{subtitle}</p>
            <div className="mt-6">{children}</div>
          </div>
          <div className="mt-5 text-center text-sm text-muted-foreground">{footer}</div>
          <p className="mt-6 text-center font-mono text-xs text-muted-foreground lg:hidden">Server: {API_BASE_URL}</p>
        </div>
      </div>
    </div>
  );
}
