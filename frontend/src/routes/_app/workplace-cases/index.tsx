import { createFileRoute, useNavigate } from "@tanstack/react-router";
import { useMutation, useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { Badge, Button, Card, Empty, ErrorBox, Loading, PageHeader, Pager, difficultyTone } from "@/components/kit";
import { workplaceCaseApi } from "@/lib/api/endpoints";
import { fmtDate, humanize } from "@/lib/format";

export const Route = createFileRoute("/_app/workplace-cases/")({
  head: () => ({ meta: [{ title: "Workplace cases — InterviewForge" }, { name: "description", content: "Practice judgment with branching workplace scenarios." }] }),
  component: WorkplaceCasesPage,
});

function WorkplaceCasesPage() {
  const navigate = useNavigate();
  const [page, setPage] = useState(0);
  const cases = useQuery({ queryKey: ["workplaceCases", "browse", page], queryFn: () => workplaceCaseApi.browse({ page, size: 9 }) });
  const history = useQuery({ queryKey: ["workplaceCases", "history"], queryFn: () => workplaceCaseApi.history(0, 5) });
  const start = useMutation({
    mutationFn: workplaceCaseApi.start,
    onSuccess: (result) => navigate({ to: "/workplace-cases/sessions/$sessionId", params: { sessionId: result.session.sessionId } }),
  });

  return <>
    <PageHeader title="Workplace cases" description="Practice high-stakes decisions through scenarios that change with your choices." />
    {start.error && <div className="mb-5"><ErrorBox error={start.error} /></div>}
    <section aria-labelledby="available-cases-heading">
      <h2 id="available-cases-heading" className="mb-3 text-lg font-bold">Explore scenarios</h2>
      {cases.isLoading ? <Loading /> : cases.error ? <ErrorBox error={cases.error} onRetry={() => cases.refetch()} /> : cases.data?.content.length === 0 ? (
        <Empty title="No cases published yet">Published workplace scenarios will appear here.</Empty>
      ) : cases.data && <>
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          {cases.data.content.map((item) => <Card key={item.id} className="flex flex-col">
            <div className="flex flex-wrap items-center gap-2"><Badge tone={difficultyTone(item.difficulty)}>{humanize(item.difficulty)}</Badge><Badge>{item.topicName}</Badge></div>
            <h3 className="mt-3 text-lg font-bold">{item.title}</h3>
            <p className="mt-1 text-sm text-muted-foreground">{item.description || item.learningObjective || "Explore a realistic workplace decision and its consequences."}</p>
            <p className="mt-3 text-xs text-muted-foreground">{item.roleName} · about {item.estimatedMinutes} min</p>
            {item.sourceLabel && <p className="mt-2 text-xs text-muted-foreground">Source: {item.sourceLabel}{item.sourceVerifiedAt ? ` · verified ${fmtDate(item.sourceVerifiedAt)}` : ""}</p>}
            {item.sourceUrl && <a className="mt-1 break-all text-xs font-semibold text-primary underline" href={item.sourceUrl} target="_blank" rel="noreferrer">View source</a>}
            <Button className="mt-5 w-full" onClick={() => start.mutate(item.id)} disabled={start.isPending}>{start.isPending ? "Starting…" : "Start case"}</Button>
          </Card>)}
        </div>
        <Pager page={cases.data.page} totalPages={cases.data.totalPages} onChange={setPage} />
      </>}
    </section>

    <section className="mt-9" aria-labelledby="case-history-heading">
      <h2 id="case-history-heading" className="mb-3 text-lg font-bold">Your recent cases</h2>
      {history.isLoading ? <Loading /> : history.error ? <ErrorBox error={history.error} onRetry={() => history.refetch()} /> : history.data?.content.length === 0 ? (
        <Empty title="No case sessions yet">Start a scenario to build your decision history.</Empty>
      ) : <div className="space-y-2">{history.data?.content.map((session) => <button key={session.sessionId} type="button" onClick={() => navigate({ to: "/workplace-cases/sessions/$sessionId", params: { sessionId: session.sessionId } })} className="flex w-full items-center justify-between gap-4 rounded-xl border-2 border-ink bg-card px-4 py-3 text-left shadow-card hover:border-primary/60">
        <span><span className="block font-semibold">{session.caseTitle}</span><span className="text-xs text-muted-foreground">{session.roleName} · {session.decisionCount} decisions · {fmtDate(session.startedAt)}</span></span>
        <span className="flex shrink-0 items-center gap-2"><Badge tone={session.status === "COMPLETED" ? "success" : "warning"}>{humanize(session.status)}</Badge><span aria-hidden>→</span></span>
      </button>)}</div>}
    </section>
  </>;
}
