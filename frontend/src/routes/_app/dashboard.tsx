import { createFileRoute, Link } from "@tanstack/react-router";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Card, Empty, ErrorBox, Field, Loading, PageHeader, Select, Stat, Badge } from "@/components/kit";
import { PracticeSessionRow } from "@/components/practice";
import { mockApi, preparationGoalApi, prepApi, progressApi, readinessApi } from "@/lib/api/endpoints";
import { fmtDate, humanize, pct } from "@/lib/format";
import { useSession } from "@/lib/session";
import { useEffect } from "react";
import { clearFocusRole, setFocusRole } from "@/lib/prefs";

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
        <TargetRoleCard />
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

function TargetRoleCard() {
  const queryClient = useQueryClient();
  const roles = useQuery({ queryKey: ["roles", "all"], queryFn: prepApi.allRoles });
  const goal = useQuery({ queryKey: ["preparationGoal"], queryFn: preparationGoalApi.get });
  const role = roles.data?.find((candidate) => candidate.id === goal.data?.targetRoleId);
  const plan = useQuery({ queryKey: ["plan", role?.id, 3], queryFn: () => readinessApi.plan(role!.id, 3), enabled: !!role });
  const saveRole = useMutation({
    mutationFn: async (targetRoleId: string | null) => {
      const current = goal.data ?? await preparationGoalApi.get();
      return preparationGoalApi.update({ ...current, targetRoleId });
    },
    onSuccess: async (saved) => {
      queryClient.setQueryData(["preparationGoal"], saved);
      const selectedRole = roles.data?.find((candidate) => candidate.id === saved.targetRoleId);
      if (selectedRole) setFocusRole({ roleId: selectedRole.id, roleName: selectedRole.name, companyName: selectedRole.companyName });
      else clearFocusRole();
      await queryClient.invalidateQueries({ queryKey: ["plan"] });
    },
  });
  useEffect(() => {
    if (role) setFocusRole({ roleId: role.id, roleName: role.name, companyName: role.companyName });
    else if (goal.data?.targetRoleId === null) clearFocusRole();
  }, [role, goal.data?.targetRoleId]);

  return (
    <Card>
      <h2 className="text-lg font-bold">Choose your target role</h2>
      <p className="mt-1 text-sm text-muted-foreground">Role options are sorted alphabetically and saved to your account.</p>
      {roles.error ? <div className="mt-3"><ErrorBox error={roles.error} onRetry={() => roles.refetch()} /></div> : roles.isLoading || goal.isLoading ? <Loading /> : roles.data?.length ? <>
        <Field label="Target role" htmlFor="dashboard-target-role">
          <Select id="dashboard-target-role" value={goal.data?.targetRoleId ?? ""} disabled={saveRole.isPending} onChange={(event) => saveRole.mutate(event.target.value || null)}>
            <option value="">Choose a role</option>
            {roles.data.map((item) => <option key={item.id} value={item.id}>{item.name} · {item.companyName}</option>)}
          </Select>
        </Field>
        {goal.error && <div className="mt-3"><ErrorBox error={goal.error} onRetry={() => goal.refetch()} /></div>}
        {saveRole.error && <div className="mt-3"><ErrorBox error={saveRole.error} /></div>}
        {role ? <div className="mt-4">
          <Link to="/roles/$roleId" params={{ roleId: role.id }} className="font-semibold text-primary">{role.name} · {role.companyName}</Link>
          {plan.isLoading ? <Loading /> : plan.error ? <ErrorBox error={plan.error} onRetry={() => plan.refetch()} /> : plan.data?.tasks.length ? (
            <ol className="mt-3 space-y-2">{plan.data.tasks.map((task, index) => <li key={task.topicId} className="flex items-center justify-between rounded-lg border border-border px-3 py-2 text-sm"><span><span className="font-semibold">{index + 1}. {task.topicName}</span> <span className="text-xs text-muted-foreground">{task.skillName}</span></span></li>)}</ol>
          ) : <p className="mt-3 text-sm text-muted-foreground">No study tasks right now — try a mock interview for this role.</p>}
        </div> : <p className="mt-3 text-sm text-muted-foreground">Choose a role to see its readiness and personalized next steps.</p>}
      </> : <Empty title="No active roles available">Browse the <Link to="/companies" className="font-semibold text-primary">Companies</Link> page to find available roles.</Empty>}
    </Card>
  );
}
