import { createFileRoute, Link } from "@tanstack/react-router";
import { useQuery } from "@tanstack/react-query";
import { Badge, Card, Empty, ErrorBox, Loading, PageHeader, difficultyTone } from "@/components/kit";
import { prepApi } from "@/lib/api/endpoints";
import { humanize } from "@/lib/format";

export const Route = createFileRoute("/_app/sets/$setId")({
  head: () => ({ meta: [{ title: "Preparation set — InterviewForge" }, { name: "description", content: "Curated questions for a role." }] }),
  component: SetPage,
});

function SetPage() {
  const { setId } = Route.useParams();
  const q = useQuery({ queryKey: ["set", setId], queryFn: () => prepApi.set(setId) });
  if (q.isLoading) return <Loading />;
  if (q.error) return <ErrorBox error={q.error} onRetry={() => q.refetch()} />;
  if (!q.data) return null;
  const { set, questions } = q.data;
  return (
    <>
      <PageHeader
        title={set.title}
        description={`${set.companyName} · ${set.roleName}`}
        actions={<Link to="/roles/$roleId" params={{ roleId: set.roleId }} className="text-sm font-semibold text-primary">Back to role</Link>}
      />
      {set.description && <p className="-mt-3 mb-6 text-muted-foreground">{set.description}</p>}
      {questions.length === 0 ? <Empty title="No questions in this set" /> : (
        <ol className="space-y-3">
          {questions.map((item, i) => (
            <li key={item.id}>
              <Card className="p-4">
                <div className="flex flex-wrap items-center gap-2">
                  <span className="font-display text-sm font-bold text-muted-foreground">{i + 1}</span>
                  <Badge tone={difficultyTone(item.difficulty)}>{humanize(item.difficulty)}</Badge>
                  <span className="text-xs text-muted-foreground">{item.topic.name}</span>
                </div>
                <p className="mt-2 font-semibold">{item.title}</p>
                <p className="mt-1 whitespace-pre-wrap text-sm text-muted-foreground">{item.questionText}</p>
                {item.options && item.options.length > 0 && (
                  <ol className="mt-2 list-[upper-alpha] space-y-1 pl-5 text-sm text-muted-foreground">{item.options.map((o, j) => <li key={j}>{o}</li>)}</ol>
                )}
              </Card>
            </li>
          ))}
        </ol>
      )}
    </>
  );
}
