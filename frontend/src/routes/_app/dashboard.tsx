import { createFileRoute, Link } from "@tanstack/react-router";
import { useQuery } from "@tanstack/react-query";
import { Card, Empty, ErrorBox, Loading, PageHeader, Stat, Badge } from "@/components/kit";
import { PracticeSessionRow } from "@/components/practice";
import { mockApi, progressApi } from "@/lib/api/endpoints";
import { fmtDate, humanize, pct } from "@/lib/format";
import { useSession } from "@/lib/session";
import { useEffect, useState } from "react";
import { getFocusRole, type FocusRole } from "@/lib/prefs";
import { readinessApi } from "@/lib/api/endpoints";

export const Route = createFileRoute("/_app/dashboard")({
  head: () => ({ meta: [{ title: "Dashboard — InterviewForge" }, { name: "description", content: "Your practice and progress at a glance." }] }),
  component: Dashboard,
});

function Dashboard() {
  const session = useSession();
  const overview = useQuery({ queryKey: ["progress", "overview"], queryFn: progressApi.overview });
  const mocks = useQuery({ queryKey: ["mock", "list", 0, 3], queryFn: () => mockApi.list(0, 3) });
  const o = overview.data;

  return (
    <>
      <PageHeader
        title={`Hi, ${session?.user.displayName ?? "there"}`}
        description="Here's where your preparation stands."
        actions={<Link to="/practice" className="inline-flex h-10 items-center rounded-lg bg-primary px-4 text-sm font-semibold text-primary-foreground">Start practice</Link>}
      />
      {overview.isLoading ? <Loading /> : overview.error ? <ErrorBox error={overview.error} onRetry={() => overview.refetch()} /> : o && (
        <div className="grid grid-cols-2 gap-3 lg:grid-cols-4">
          <Stat label="Accuracy" value={pct(o.accuracyPercent)} sub="Multiple choice" />
          <Stat label="Answered" value={o.mcqQuestionsAnswered} sub={`${o.mcqCorrectAnswers} correct`} />
          <Stat label="Completed" value={o.completedSessions} sub="sessions" />
          <Stat label="In progress" value={o.inProgressSessions} sub="sessions" />
        </div>
      )}

      <div className="mt-6 grid gap-6 lg:grid-cols-2">
        <ContinueCard attempts={o?.recentAttempts} loading={overview.isLoading} />
        <FocusRoleCard />
      </div>

      <div className="mt-8 grid gap-6 lg:grid-cols-2">
        <Card>
          <div className="mb-4 flex items-center justify-between">
            <h2 className="text-lg font-bold">Recent practice</h2>
            <Link to="/progress" className="text-sm font-semibold text-primary">All progress</Link>
          </div>
          {overview.isLoading ? <Loading /> : !o || o.recentAttempts.length === 0 ? (
            <Empty title="No practice yet">Start a session to see your results here.</Empty>
          ) : (
            <div className="space-y-2">{o.recentAttempts.map((s) => <PracticeSessionRow key={s.sessionId} s={s} />)}</div>
          )}
        </Card>
        <Card>
          <div className="mb-4 flex items-center justify-between">
            <h2 className="text-lg font-bold">Mock interviews</h2>
            <Link to="/interviews" className="text-sm font-semibold text-primary">All interviews</Link>
          </div>
          {mocks.isLoading ? <Loading /> : mocks.error ? <ErrorBox error={mocks.error} /> : mocks.data?.content.length === 0 ? (
            <Empty title="No interviews yet">Pick a role under <Link to="/companies" className="font-semibold text-primary">Companies</Link> to start one.</Empty>
          ) : (
            <ul className="space-y-2">
              {mocks.data?.content.map((m) => (
                <li key={m.sessionId}>
                  <Link to="/interviews/$sessionId" params={{ sessionId: m.sessionId }} className="flex items-center justify-between rounded-lg border border-border px-4 py-3 hover:border-primary/50">
                    <div>
                      <p className="text-sm font-semibold">{m.roleName}</p>
                      <p className="text-xs text-muted-foreground">{fmtDate(m.startedAt)}</p>
                    </div>
                    <Badge tone={m.status === "COMPLETED" ? "success" : "warning"}>{humanize(m.status)}</Badge>
                  </Link>
                </li>
              ))}
            </ul>
          )}
        </Card>
      </div>
    </>
  );
}

function ContinueCard({ attempts, loading }: { attempts?: { sessionId: string; status: string; answeredCount: number; questionCount: number; type: string }[] | undefined; loading: boolean }) {
  const open = attempts?.find((a) => a.status !== "COMPLETED");
  return (
    <Card>
      <h2 className="text-lg font-bold">Continue practicing</h2>
      {loading ? <Loading /> : open ? (
        <div className="mt-3 space-y-3">
          <p className="text-sm text-muted-foreground">You have an unfinished {open.type === "MCQ" ? "multiple-choice" : "written"} session — {open.answeredCount} of {open.questionCount} answered.</p>
          <Link to="/practice/$sessionId" params={{ sessionId: open.sessionId }} className="inline-flex h-10 items-center rounded-lg bg-primary px-4 text-sm font-semibold text-primary-foreground">Resume session</Link>
        </div>
      ) : (
        <div className="mt-3 space-y-3">
          <p className="text-sm text-muted-foreground">No unfinished sessions. Pick a topic and difficulty to start a fresh one.</p>
          <Link to="/practice" className="inline-flex h-10 items-center rounded-lg bg-primary px-4 text-sm font-semibold text-primary-foreground">New session</Link>
        </div>
      )}
    </Card>
  );
}

function FocusRoleCard() {
  const [role, setRole] = useState<FocusRole | null>(null);
  useEffect(() => setRole(getFocusRole()), []);
  const plan = useQuery({ queryKey: ["plan", role?.roleId, 3], queryFn: () => readinessApi.plan(role!.roleId, 3), enabled: !!role });
  return (
    <Card>
      <h2 className="text-lg font-bold">Next step for your role</h2>
      {!role ? (
        <Empty title="No role selected">Open a role under <Link to="/companies" className="font-semibold text-primary">Companies</Link> and it will show here.</Empty>
      ) : (
        <div className="mt-2">
          <Link to="/roles/$roleId" params={{ roleId: role.roleId }} className="font-semibold text-primary">{role.roleName} · {role.companyName}</Link>
          {plan.isLoading ? <Loading /> : plan.error ? <ErrorBox error={plan.error} onRetry={() => plan.refetch()} /> : plan.data?.tasks.length ? (
            <ol className="mt-3 space-y-2">
              {plan.data.tasks.map((t, i) => (
                <li key={t.topicId} className="flex items-center justify-between rounded-lg border border-border px-3 py-2 text-sm">
                  <span><span className="font-semibold">{i + 1}. {t.topicName}</span> <span className="text-xs text-muted-foreground">{t.skillName}</span></span>
                </li>
              ))}
            </ol>
          ) : <p className="mt-3 text-sm text-muted-foreground">No study tasks right now — try a mock interview for this role.</p>}
        </div>
      )}
    </Card>
  );
}
