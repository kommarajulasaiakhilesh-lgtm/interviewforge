import { createFileRoute, Link } from "@tanstack/react-router";
import { useQuery } from "@tanstack/react-query";
import { Empty, ErrorBox, Loading, PageHeader } from "@/components/kit";
import { prepApi } from "@/lib/api/endpoints";

export const Route = createFileRoute("/_app/companies/")({
  head: () => ({ meta: [{ title: "Companies — InterviewForge" }, { name: "description", content: "Browse companies and roles." }] }),
  component: Companies,
});

function Companies() {
  const q = useQuery({ queryKey: ["companies"], queryFn: prepApi.companies });
  return (
    <>
      <PageHeader title="Companies" description="Pick a company to see its roles, skills and preparation sets." />
      {q.isLoading ? <Loading /> : q.error ? <ErrorBox error={q.error} onRetry={() => q.refetch()} /> : q.data?.length === 0 ? (
        <Empty title="No companies yet" />
      ) : (
        <ul className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {[...(q.data ?? [])].sort((a, b) => a.name.localeCompare(b.name)).map((c) => (
            <li key={c.id}>
              <Link to="/companies/$companyId" params={{ companyId: c.id }} className="block h-full rounded-xl border border-border bg-card p-5 shadow-card transition hover:-translate-y-0.5 hover:border-primary/50">
                <div className="mb-3 flex h-10 w-10 items-center justify-center rounded-lg bg-ink font-display font-bold text-ink-foreground">{c.name.charAt(0)}</div>
                <h2 className="text-lg font-bold">{c.name}</h2>
                {c.description && <p className="mt-1 line-clamp-3 text-sm text-muted-foreground">{c.description}</p>}
              </Link>
            </li>
          ))}
        </ul>
      )}
    </>
  );
}
