import { createFileRoute, Link } from "@tanstack/react-router";
import { useQuery } from "@tanstack/react-query";
import { Card, Empty, ErrorBox, Loading, PageHeader } from "@/components/kit";
import { TargetRoleButton } from "@/components/TargetRoleButton";
import { prepApi } from "@/lib/api/endpoints";

export const Route = createFileRoute("/_app/companies/$companyId")({
  head: () => ({ meta: [{ title: "Company roles — InterviewForge" }, { name: "description", content: "Roles at this company." }] }),
  component: CompanyRoles,
});

function CompanyRoles() {
  const { companyId } = Route.useParams();
  const companies = useQuery({ queryKey: ["companies"], queryFn: prepApi.companies });
  const roles = useQuery({ queryKey: ["roles", companyId], queryFn: () => prepApi.roles(companyId) });
  const company = companies.data?.find((c) => c.id === companyId);

  return (
    <>
      <PageHeader
        title={company?.name ?? "Roles"}
        description={company?.description ?? undefined}
        actions={<Link to="/companies" className="text-sm font-semibold text-primary">All companies</Link>}
      />
      {roles.isLoading ? <Loading /> : roles.error ? <ErrorBox error={roles.error} onRetry={() => roles.refetch()} /> : roles.data?.length === 0 ? (
        <Empty title="No open roles" />
      ) : (
        <ul className="space-y-3">
          {[...(roles.data ?? [])].sort((a, b) => a.name.localeCompare(b.name)).map((r) => (
            <li key={r.id}>
              <Card className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                <div>
                  <Link to="/roles/$roleId" params={{ roleId: r.id }} className="text-lg font-bold hover:text-primary">{r.name}</Link>
                  {r.description && <p className="mt-1 text-sm text-muted-foreground">{r.description}</p>}
                  <Link to="/roles/$roleId" params={{ roleId: r.id }} className="mt-2 inline-flex text-sm font-semibold text-primary">View skills, readiness & study plan →</Link>
                </div>
                <TargetRoleButton role={r} />
              </Card>
            </li>
          ))}
        </ul>
      )}
    </>
  );
}
