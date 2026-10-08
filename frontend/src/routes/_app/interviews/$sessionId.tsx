import { createFileRoute, Link } from "@tanstack/react-router";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState, type FormEvent } from "react";
import { Badge, Button, Card, ErrorBox, Loading, Meter, PageHeader, Textarea, difficultyTone } from "@/components/kit";
import { mockApi } from "@/lib/api/endpoints";
import type { MockAnswerDetail } from "@/lib/api/types";
import { humanize, pct } from "@/lib/format";
import { cn } from "@/lib/utils";

export const Route = createFileRoute("/_app/interviews/$sessionId")({
  head: () => ({ meta: [{ title: "Mock interview — InterviewForge" }, { name: "description", content: "Answer and review a written mock interview." }] }),
  component: InterviewPage,
});

function InterviewPage() {
  const { sessionId } = Route.useParams();
  const q = useQuery({ queryKey: ["mock", sessionId], queryFn: () => mockApi.get(sessionId) });
  if (q.isLoading) return <Loading />;
  if (q.error) return <ErrorBox error={q.error} onRetry={() => q.refetch()} />;
  if (!q.data) return null;
  const { session, questions, skillResults, assessmentMethod } = q.data;
  const done = session.status === "COMPLETED";

  return (
    <>
      <PageHeader
        title={session.roleName}
        description={`Mock interview · ${session.answeredCount} of ${session.questionCount} answered`}
        actions={<Link to="/interviews" className="text-sm font-semibold text-primary">All interviews</Link>}
      />
      <Card className="mb-6">
        <div className="flex items-center justify-between gap-3">
          <Badge tone={done ? "success" : "warning"}>{humanize(session.status)}</Badge>
          <span className="text-sm text-muted-foreground">Answers are not automatically graded — results reflect your own 1–5 self-ratings.</span>
        </div>
        <div className="mt-3"><Meter value={(session.answeredCount / Math.max(1, session.questionCount)) * 100} label="Interview completion" /></div>
      </Card>

      {skillResults.length > 0 && (
        <Card className="mb-6">
          <h2 className="text-lg font-bold">Results by skill</h2>
          <ul className="mt-4 grid gap-4 sm:grid-cols-2">
            {skillResults.map((s) => (
              <li key={s.skillId} className="rounded-lg border border-border p-4">
                <p className="font-semibold">{s.skillName}</p>
                <p className="text-xs text-muted-foreground">{s.questionsAnswered}/{s.questionCount} answered · {pct(s.completionPercent)}</p>
                <p className="mt-2 font-display text-2xl font-bold text-primary">
                  {s.selfRatingAverage !== null ? Number(s.selfRatingAverage).toFixed(1) : "—"}
                  <span className="text-sm font-medium text-muted-foreground"> / 5 self-rating</span>
                </p>
              </li>
            ))}
          </ul>
          {assessmentMethod && <p className="mt-4 text-xs text-muted-foreground">{assessmentMethod}</p>}
        </Card>
      )}

      <ol className="space-y-4">
        {(() => {
          const sorted = [...questions].sort((a, b) => a.position - b.position);
          const current = sorted.findIndex((x) => !x.answeredAt);
          const visible = current === -1 ? sorted : sorted.slice(0, current + 1);
          return visible.map((item) => (
            <li key={item.questionId}>
              {current !== -1 && item === sorted[current] && <p className="mb-2 text-xs font-semibold uppercase tracking-wide text-muted-foreground">Prompt {current + 1} of {sorted.length}</p>}
              <MockQuestion sessionId={sessionId} item={item} />
            </li>
          ));
        })()}
      </ol>
    </>
  );
}

function MockQuestion({ sessionId, item }: { sessionId: string; item: MockAnswerDetail }) {
  const qc = useQueryClient();
  const [text, setText] = useState("");
  const [rating, setRating] = useState<number | null>(null);
  const answered = !!item.answeredAt;
  const submit = useMutation({
    mutationFn: () => mockApi.answer(sessionId, { questionId: item.questionId, answerText: text.trim(), selfRating: rating! }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["mock"] }),
  });
  function onSubmit(e: FormEvent) {
    e.preventDefault();
    submit.mutate();
  }
  const id = `mq-${item.questionId}`;

  return (
    <Card>
      <div className="flex flex-wrap items-center gap-2">
        <span className="font-display text-sm font-bold text-muted-foreground">Q{item.position + 1}</span>
        <Badge tone="primary">{item.skillName}</Badge>
        <Badge tone={difficultyTone(item.difficulty)}>{humanize(item.difficulty)}</Badge>
      </div>
      <h3 className="mt-2 text-lg font-bold">{item.title}</h3>
      <p className="mt-1 whitespace-pre-wrap text-sm">{item.questionText}</p>
      {answered ? (
        <div className="mt-4 rounded-lg bg-muted p-3 text-sm">
          <p className="text-xs font-semibold text-muted-foreground">Your answer · self-rating {item.selfRating}/5</p>
          <p className="mt-1 whitespace-pre-wrap">{item.answerText}</p>
        </div>
      ) : (
        <form onSubmit={onSubmit} className="mt-4 space-y-3">
          <label htmlFor={id} className="text-sm font-semibold">Your answer</label>
          <Textarea id={id} maxLength={10000} value={text} onChange={(e) => setText(e.target.value)} />
          <fieldset>
            <legend className="mb-2 text-sm font-semibold">How well did you answer? (1–5)</legend>
            <div className="flex gap-2">
              {[1, 2, 3, 4, 5].map((n) => (
                <label key={n} className={cn("flex h-10 w-10 cursor-pointer items-center justify-center rounded-lg border font-semibold", rating === n ? "border-primary bg-primary text-primary-foreground" : "border-border hover:bg-muted")}>
                  <input type="radio" name={`${id}-r`} value={n} className="sr-only" checked={rating === n} onChange={() => setRating(n)} />
                  {n}
                </label>
              ))}
            </div>
          </fieldset>
          {submit.error ? <ErrorBox error={submit.error} /> : null}
          <Button type="submit" disabled={submit.isPending || !text.trim() || rating === null}>{submit.isPending ? "Saving…" : "Submit answer"}</Button>
        </form>
      )}
    </Card>
  );
}
