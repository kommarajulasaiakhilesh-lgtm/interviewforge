import { createFileRoute, Link } from "@tanstack/react-router";
import { keepPreviousData, useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { Badge, Empty, ErrorBox, Loading, PageHeader, Pager } from "@/components/kit";
import { mockApi } from "@/lib/api/endpoints";
import { fmtDate, humanize } from "@/lib/format";

export const Route = createFileRoute("/_app/interviews/")({
  head: () => ({ meta: [{ title: "Mock interviews — InterviewForge" }, { name: "description", content: "Your written mock interviews." }] }),
  component: Interviews,
});

function Interviews() {
  const [page, setPage] = useState(0);
  const q = useQuery({ queryKey: ["mock", "list", page, 10], queryFn: () => mockApi.list(page, 10), placeholderData: keepPreviousData });
  return (
    <>
      <PageHeader
        title="Mock interviews"
        description="Start a new interview from any role page."
        actions={<Link to="/companies" className="inline-flex h-10 items-center rounded-lg bg-primary px-4 text-sm font-semibold text-primary-foreground">Choose a role</Link>}
      />
      {q.isLoading ? <Loading /> : q.error ? <ErrorBox error={q.error} onRetry={() => q.refetch()} /> : q.data?.content.length === 0 ? (
        <Empty title="No interviews yet">Browse companies and pick a role to begin.</Empty>
      ) : q.data && (
        <>
          <ul className="space-y-2">
            {q.data.content.map((m) => (
              <li key={m.sessionId}>
                <Link to="/interviews/$sessionId" params={{ sessionId: m.sessionId }} className="flex flex-wrap items-center justify-between gap-3 rounded-lg border border-border bg-card px-4 py-3 hover:border-primary/50">
                  <div>
                    <p className="font-semibold">{m.roleName}</p>
                    <p className="text-xs text-muted-foreground">{fmtDate(m.startedAt)} · {m.answeredCount}/{m.questionCount} answered</p>
                  </div>
                  <Badge tone={m.status === "COMPLETED" ? "success" : "warning"}>{humanize(m.status)}</Badge>
                </Link>
              </li>
            ))}
          </ul>
          <Pager page={q.data.page} totalPages={q.data.totalPages} onChange={setPage} />
        </>
      )}
    </>
  );
}
