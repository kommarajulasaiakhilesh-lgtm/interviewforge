import { createFileRoute, Link } from "@tanstack/react-router";
import { ArrowRight, BarChart3, CheckCircle2, ListChecks, MessagesSquare, Target } from "lucide-react";
import { useSession } from "@/lib/session";

export const Route = createFileRoute("/")({
  head: () => ({
    meta: [
      { title: "InterviewForge — Practice smarter for your next interview" },
      { name: "description", content: "Practice questions, measure role readiness, follow a study plan, and run written mock interviews." },
      { property: "og:title", content: "InterviewForge — Practice smarter" },
      { property: "og:description", content: "Practice, readiness scores, study plans and mock interviews for real company roles." },
      { property: "og:type", content: "website" },
      { name: "twitter:card", content: "summary_large_image" },
    ],
  }),
  component: Landing,
});

const features = [
  { icon: Target, t: "Question practice", d: "Filter by topic, tag and difficulty. Instant feedback and explanations on multiple choice." },
  { icon: BarChart3, t: "Progress by topic", d: "Accuracy per topic, rated only once you have enough attempts to be meaningful." },
  { icon: ListChecks, t: "Role readiness", d: "A transparent, weighted score for real company roles — plus a prioritized study plan." },
  { icon: MessagesSquare, t: "Mock interviews", d: "Written, role-based interviews with honest self-ratings broken down by skill." },
];

const steps = [
  ["Pick a target role", "Choose a company and the role you're preparing for. We map it to the skills and topics that matter."],
  ["Practice with purpose", "Answer questions by topic. Every answer sharpens your accuracy profile."],
  ["Follow your plan", "Get a ranked list of what to study next, with question counts and time estimates."],
  ["Rehearse the real thing", "Run a mock interview and see which skills still need work."],
];

const skills = [
  ["Data structures", 82],
  ["System design", 58],
  ["Behavioral", 71],
  ["SQL", 36],
] as const;

function Landing() {
  const session = useSession();
  const primaryTo = session ? "/dashboard" : "/register";
  return (
    <div className="min-h-screen">
      <div className="bg-hero">
        <header className="mx-auto flex max-w-6xl items-center justify-between px-5 py-5">
          <span className="font-display text-xl font-extrabold">Interview<span className="text-primary">Forge</span></span>
          <nav className="flex gap-2">
            {session ? (
              <Link to="/dashboard" className="rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-primary-foreground">Open dashboard</Link>
            ) : (
              <>
                <Link to="/login" search={{ redirect: undefined }} className="rounded-lg px-4 py-2 text-sm font-semibold hover:bg-muted">Sign in</Link>
                <Link to="/register" className="rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-primary-foreground">Create account</Link>
              </>
            )}
          </nav>
        </header>

        <section className="mx-auto grid max-w-6xl items-center gap-12 px-5 pb-20 pt-10 sm:pt-16 lg:grid-cols-[1.1fr_0.9fr]">
          <div>
            <p className="mb-4 inline-block rounded-full bg-accent/40 px-3 py-1 text-xs font-bold uppercase tracking-widest text-accent-foreground">Interview preparation</p>
            <h1 className="max-w-3xl text-4xl font-extrabold leading-[1.05] sm:text-6xl">Forge your answers before the room gets quiet.</h1>
            <p className="mt-5 max-w-xl text-lg text-muted-foreground">Practice by topic, see exactly how ready you are for the role you want, and rehearse with written mock interviews.</p>
            <div className="mt-8 flex flex-wrap items-center gap-3">
              <Link to={primaryTo} className="inline-flex items-center gap-2 rounded-lg bg-ink px-5 py-3 font-semibold text-ink-foreground hover:opacity-90">
                {session ? "Continue practicing" : "Start preparing free"} <ArrowRight className="h-4 w-4" aria-hidden />
              </Link>
              {!session && (
                <Link to="/login" search={{ redirect: undefined }} className="rounded-lg px-5 py-3 font-semibold hover:bg-muted">I have an account</Link>
              )}
            </div>
            <ul className="mt-8 flex flex-wrap gap-x-6 gap-y-2 text-sm text-muted-foreground">
              {["No answer keys spoiled", "Scores you can trust", "Built for real roles"].map((x) => (
                <li key={x} className="flex items-center gap-2"><CheckCircle2 className="h-4 w-4 text-primary" aria-hidden />{x}</li>
              ))}
            </ul>
          </div>

          <div aria-hidden className="relative">
            <div className="absolute -inset-4 -z-10 rotate-2 rounded-3xl bg-accent/25" />
            <div className="rounded-2xl border border-border bg-card p-6 shadow-card">
              <div className="flex items-start justify-between">
                <div>
                  <p className="text-xs font-bold uppercase tracking-widest text-muted-foreground">Role readiness</p>
                  <p className="mt-1 font-display text-lg font-bold">Backend Engineer · Acme</p>
                </div>
                <div className="text-right">
                  <p className="font-display text-4xl font-extrabold text-primary">64</p>
                  <p className="text-xs text-muted-foreground">of 100</p>
                </div>
              </div>
              <div className="mt-6 space-y-4">
                {skills.map(([name, v]) => (
                  <div key={name}>
                    <div className="flex justify-between text-sm"><span className="font-medium">{name}</span><span className="text-muted-foreground">{v}%</span></div>
                    <div className="mt-1.5 h-2 rounded-full bg-muted">
                      <div className={`h-2 rounded-full ${v < 50 ? "bg-warning" : "bg-primary"}`} style={{ width: `${v}%` }} />
                    </div>
                  </div>
                ))}
              </div>
              <div className="mt-6 rounded-xl bg-ink p-4 text-ink-foreground">
                <p className="text-xs font-bold uppercase tracking-widest text-accent">Next up</p>
                <p className="mt-1 font-semibold">SQL joins & indexing</p>
                <p className="text-sm text-ink-foreground/70">8 questions · about 20 min</p>
              </div>
            </div>
          </div>
        </section>
      </div>

      <main>
        <section className="mx-auto max-w-6xl px-5 py-20" aria-labelledby="features-title">
          <h2 id="features-title" className="max-w-2xl text-3xl font-extrabold sm:text-4xl">Everything you need between "applied" and "hired".</h2>
          <div className="mt-10 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
            {features.map(({ icon: Icon, t, d }) => (
              <div key={t} className="rounded-xl border border-border bg-card p-6 shadow-card">
                <span className="inline-flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10 text-primary"><Icon className="h-5 w-5" aria-hidden /></span>
                <h3 className="mt-4 text-lg font-bold">{t}</h3>
                <p className="mt-1 text-sm text-muted-foreground">{d}</p>
              </div>
            ))}
          </div>
        </section>

        <section className="bg-ink text-ink-foreground" aria-labelledby="how-title">
          <div className="mx-auto max-w-6xl px-5 py-20">
            <p className="text-xs font-bold uppercase tracking-widest text-accent">How it works</p>
            <h2 id="how-title" className="mt-2 max-w-2xl text-3xl font-extrabold sm:text-4xl">From guessing to knowing, in four steps.</h2>
            <ol className="mt-12 grid gap-8 sm:grid-cols-2 lg:grid-cols-4">
              {steps.map(([t, d], i) => (
                <li key={t} className="border-t border-ink-foreground/15 pt-5">
                  <span className="font-display text-3xl font-extrabold text-accent">0{i + 1}</span>
                  <h3 className="mt-3 text-lg font-bold">{t}</h3>
                  <p className="mt-1 text-sm text-ink-foreground/70">{d}</p>
                </li>
              ))}
            </ol>
          </div>
        </section>

        <section className="mx-auto grid max-w-6xl gap-10 px-5 py-20 lg:grid-cols-2 lg:items-center" aria-labelledby="trust-title">
          <div>
            <h2 id="trust-title" className="text-3xl font-extrabold sm:text-4xl">A score that shows its work.</h2>
            <p className="mt-4 text-lg text-muted-foreground">Readiness weighs each skill by how important it is for the role and each topic by how relevant it is to that skill. Topics without enough attempts are flagged — never silently guessed.</p>
          </div>
          <dl className="grid grid-cols-3 gap-4">
            {[["Weighted", "by skill importance"], ["Honest", "about thin data"], ["Actionable", "ranked study tasks"]].map(([k, v]) => (
              <div key={k} className="rounded-xl border border-border bg-card p-5 text-center shadow-card">
                <dt className="font-display text-lg font-extrabold text-primary">{k}</dt>
                <dd className="mt-1 text-xs text-muted-foreground">{v}</dd>
              </div>
            ))}
          </dl>
        </section>

        <section className="mx-auto max-w-6xl px-5 pb-20">
          <div className="bg-hero rounded-2xl border border-border bg-card px-6 py-14 text-center shadow-card">
            <h2 className="text-3xl font-extrabold sm:text-4xl">Your next interview is closer than it looks.</h2>
            <p className="mx-auto mt-3 max-w-lg text-muted-foreground">Create a free account and run your first practice session in under a minute.</p>
            <Link to={primaryTo} className="mt-8 inline-flex items-center gap-2 rounded-lg bg-primary px-6 py-3 font-semibold text-primary-foreground hover:opacity-90">
              {session ? "Go to dashboard" : "Create free account"} <ArrowRight className="h-4 w-4" aria-hidden />
            </Link>
          </div>
        </section>
      </main>

      <footer className="border-t border-border">
        <div className="mx-auto flex max-w-6xl flex-wrap items-center justify-between gap-3 px-5 py-6 text-sm text-muted-foreground">
          <span className="font-display font-bold text-foreground">Interview<span className="text-primary">Forge</span></span>
          <span>Practice. Measure. Rehearse.</span>
        </div>
      </footer>
    </div>
  );
}
