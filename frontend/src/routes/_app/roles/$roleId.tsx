import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import { useMutation, useQuery } from "@tanstack/react-query";
import { useEffect, useState, type FormEvent } from "react";
import { setFocusRole } from "@/lib/prefs";
import { Badge, Button, Card, Empty, ErrorBox, Field, Input, Loading, Meter, PageHeader } from "@/components/kit";
import { useStartPractice } from "@/components/practice";
import { mockApi, prepApi, readinessApi } from "@/lib/api/endpoints";
import { pct } from "@/lib/format";

export const Route = createFileRoute("/_app/roles/$roleId")({
  head: () => ({ meta: [{ title: "Role preparation — InterviewForge" }, { name: "description", content: "Skills, readiness score and study plan for this role." }] }),
  component: RolePage,
});

function RolePage() {
  const { roleId } = Route.useParams();
  const role = useQuery({ queryKey: ["role", roleId], queryFn: () => prepApi.role(roleId) });
  const sets = useQuery({ queryKey: ["sets", roleId], queryFn: () => prepApi.sets(roleId) });
  const readiness = useQuery({ queryKey: ["readiness", roleId], queryFn: () => readinessApi.get(roleId) });
  const plan = useQuery({ queryKey: ["plan", roleId], queryFn: () => readinessApi.plan(roleId, 10) });
  const startPractice = useStartPractice();

  useEffect(() => {
    const rr = role.data?.role;
    if (rr) setFocusRole({ roleId: rr.id, roleName: rr.name, companyName: rr.companyName });
  }, [role.data]);

  if (role.isLoading) return <Loading />;
  if (role.error) return <ErrorBox error={role.error} onRetry={() => role.refetch()} />;
  if (!role.data) return null;
  const r = role.data.role;
  const rd = readiness.data;

  return (
    <>
      <PageHeader
        title={r.name}
        description={r.companyName}
        actions={<Link to="/companies/$companyId" params={{ companyId: r.companyId }} className="text-sm font-semibold text-primary">Back to {r.companyName}</Link>}
      />
      {r.description && <p className="-mt-3 mb-6 max-w-2xl text-muted-foreground">{r.description}</p>}

      <div className="grid gap-6 lg:grid-cols-[1fr_320px]">
        <Card>
          <h2 className="text-lg font-bold">Readiness</h2>
          {readiness.isLoading ? <Loading /> : readiness.error ? <ErrorBox error={readiness.error} /> : rd && (
            <>
              <div className="mt-3 flex flex-wrap items-end gap-6">
                <div>
                  <p className="font-display text-5xl font-extrabold text-primary">{rd.readinessScore === null ? "—" : pct(rd.readinessScore)}</p>
                  <p className="text-sm text-muted-foreground">{rd.readinessScore === null ? "not enough data yet" : "readiness score"}</p>
                </div>
                <div className="min-w-40 flex-1">
                  <p className="text-sm font-semibold">Data coverage {pct(rd.dataCoveragePercent)}</p>
                  <Meter value={rd.dataCoveragePercent} label="Data coverage" />
                  <p className="mt-1 text-xs text-muted-foreground">Topics need {rd.minimumAttemptsForReliableAccuracy}+ answers for a reliable sample.</p>
                </div>
              </div>
              {rd.dataCoveragePercent < 50 && (
                <p role="note" className="mt-4 rounded-lg border border-warning/40 bg-warning/10 p-3 text-sm">
                  Limited data: you've answered too few questions in this role's topics for a dependable score. Practice the study-plan topics to improve accuracy of this view.
                </p>
              )}
              <p className="mt-3 text-xs text-muted-foreground">This score reflects your practice results against the role's topics. It is a study guide, not a hiring prediction or guarantee.</p>
              {rd.skills.length === 0 ? <div className="mt-4"><Empty title="No skills mapped to this role yet" /></div> : (
                <ul className="mt-6 space-y-4">
                  {rd.skills.map((s) => (
                    <li key={s.skillId}>
                      <div className="flex items-center justify-between text-sm">
                        <span className="font-semibold">{s.skillName} <span className="text-xs font-normal text-muted-foreground">importance {s.importance}/5</span></span>
                        <span className="font-semibold">{pct(s.readinessScore)}</span>
                      </div>
                      <Meter value={s.readinessScore} label={`${s.skillName} readiness`} />
                      <div className="mt-2 flex flex-wrap gap-1">
                        {s.topics.map((t) => (
                          <Badge key={t.topicId} tone={t.reliableSample ? "primary" : "neutral"}>{t.topicName} · {pct(t.accuracyPercent)} ({t.questionsAnswered})</Badge>
                        ))}
                      </div>
                    </li>
                  ))}
                </ul>
              )}
              {rd.scoringMethod && <p className="mt-4 text-xs text-muted-foreground">{rd.scoringMethod}</p>}
            </>
          )}
        </Card>
        <MockStarter roleId={roleId} />
      </div>

      <Card className="mt-6">
        <h2 className="text-lg font-bold">Study plan</h2>
        {plan.isLoading ? <Loading /> : plan.error ? <ErrorBox error={plan.error} /> : plan.data?.tasks.length === 0 ? (
          <div className="mt-3"><Empty title="Nothing to study right now">You're at or above {plan.data.targetAccuracyPercent}% on every mapped topic.</Empty></div>
        ) : (
          <ol className="mt-4 space-y-3">
            {plan.data?.tasks.map((t, i) => (
              <li key={t.topicId} className="flex flex-col gap-3 rounded-lg border border-border p-4 sm:flex-row sm:items-center sm:justify-between">
                <div>
                  <p className="font-semibold"><span className="mr-2 font-display text-primary">{i + 1}.</span>{t.topicName} <span className="text-xs font-normal text-muted-foreground">· {t.skillName}</span></p>
                  <p className="text-xs text-muted-foreground">Priority {t.priority} · {t.questionsAnswered} answered · accuracy {pct(t.currentAccuracyPercent)} · ~{t.estimatedMinutes} min</p>
                  <ul className="mt-1 list-disc pl-5 text-sm text-muted-foreground">{t.reasons.map((x) => <li key={x}>{x}</li>)}</ul>
                </div>
                <Button size="sm" variant="accent" disabled={startPractice.isPending} onClick={() => startPractice.mutate({ topicId: t.topicId, type: "MCQ", questionCount: t.recommendedQuestions })}>
                  Practice {t.recommendedQuestions} MCQs
                </Button>
              </li>
            ))}
          </ol>
        )}
        {startPractice.error ? <div className="mt-3"><ErrorBox error={startPractice.error} /></div> : null}
      </Card>

      <div className="mt-6 grid gap-6 lg:grid-cols-2">
        <Card>
          <h2 className="mb-3 text-lg font-bold">Skills & topics</h2>
          {role.data.skills.length === 0 ? <Empty title="No skills listed" /> : (
            <ul className="space-y-3">
              {role.data.skills.map((s) => (
                <li key={s.skillId}>
                  <p className="font-semibold">{s.skillName} <span className="text-xs font-normal text-muted-foreground">importance {s.importance}/5</span></p>
                  <div className="mt-1 flex flex-wrap gap-1">{s.topics.map((t) => <Badge key={t.topicId}>{t.topicName}</Badge>)}</div>
                </li>
              ))}
            </ul>
          )}
        </Card>
        <Card>
          <h2 className="mb-3 text-lg font-bold">Preparation sets</h2>
          {sets.isLoading ? <Loading /> : sets.error ? <ErrorBox error={sets.error} /> : sets.data?.length === 0 ? <Empty title="No sets published yet" /> : (
            <ul className="space-y-2">
              {sets.data?.map((s) => (
                <li key={s.id}>
                  <Link to="/sets/$setId" params={{ setId: s.id }} className="block rounded-lg border border-border px-4 py-3 hover:border-primary/50">
                    <p className="font-semibold">{s.title}</p>
                    <p className="text-xs text-muted-foreground">{s.questionCount} questions</p>
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

function MockStarter({ roleId }: { roleId: string }) {
  const navigate = useNavigate();
  const [count, setCount] = useState(5);
  const start = useMutation({
    mutationFn: () => mockApi.create(roleId, count),
    onSuccess: (r) => navigate({ to: "/interviews/$sessionId", params: { sessionId: r.session.sessionId } }),
  });
  function submit(e: FormEvent) {
    e.preventDefault();
    start.mutate();
  }
  return (
    <Card className="bg-ink text-ink-foreground">
      <h2 className="text-lg font-bold">Mock interview</h2>
      <p className="mt-1 text-sm text-ink-foreground/70">Written questions for this role. Rate yourself 1–5 after each answer.</p>
      <form onSubmit={submit} className="mt-4 space-y-3">
        <Field label="Questions (1–20)" htmlFor="mock-count">
          <Input id="mock-count" type="number" min={1} max={20} value={count} onChange={(e) => setCount(Math.max(1, Math.min(20, Number(e.target.value) || 1)))} />
        </Field>
        {start.error ? <ErrorBox error={start.error} /> : null}
        <Button type="submit" variant="accent" className="w-full" disabled={start.isPending}>{start.isPending ? "Starting…" : "Start interview"}</Button>
      </form>
    </Card>
  );
}
