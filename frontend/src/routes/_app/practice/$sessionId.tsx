import { createFileRoute, Link } from "@tanstack/react-router";
import { useStartPractice } from "@/components/practice";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState, type FormEvent } from "react";
import { Badge, Button, Card, ErrorBox, Loading, Meter, PageHeader, Textarea, difficultyTone } from "@/components/kit";
import { practiceApi } from "@/lib/api/endpoints";
import type { PracticeQuestionReview } from "@/lib/api/types";
import { humanize, pct } from "@/lib/format";
import { cn } from "@/lib/utils";

export const Route = createFileRoute("/_app/practice/$sessionId")({
  head: () => ({ meta: [{ title: "Practice session — InterviewForge" }, { name: "description", content: "Answer questions and review feedback." }] }),
  component: SessionPage,
});

function SessionPage() {
  const { sessionId } = Route.useParams();
  const q = useQuery({ queryKey: ["practice", sessionId], queryFn: () => practiceApi.get(sessionId) });
  if (q.isLoading) return <Loading />;
  if (q.error) return <ErrorBox error={q.error} onRetry={() => q.refetch()} />;
  if (!q.data) return null;
  const { session, questions } = q.data;
  const done = session.status === "COMPLETED";

  return (
    <>
      <PageHeader
        title={done ? "Session review" : "Practice session"}
        description={`${session.type === "MCQ" ? "Multiple choice" : "Written"} · ${session.answeredCount} of ${session.questionCount} answered`}
        actions={<Link to="/practice" className="text-sm font-semibold text-primary">Back to practice</Link>}
      />
      <Card className="mb-6">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <Badge tone={done ? "success" : "warning"}>{humanize(session.status)}</Badge>
          {done && session.type === "MCQ" && (
            <p className="font-display text-xl font-bold">Score {pct(session.scorePercent)} <span className="text-sm font-medium text-muted-foreground">({session.correctCount}/{session.questionCount})</span></p>
          )}
          {done && session.type === "TEXT" && <p className="text-sm text-muted-foreground">Written answers aren't auto-graded — compare with the reference answers below.</p>}
        </div>
        <div className="mt-3"><Meter value={(session.answeredCount / Math.max(1, session.questionCount)) * 100} label="Session completion" /></div>
        {done && <Finish type={session.type} count={session.questionCount} />}
      </Card>
      <ol className="space-y-4">
        {[...questions].sort((a, b) => a.position - b.position).map((item) => (
          <li key={item.questionId}><QuestionCard sessionId={sessionId} item={item} /></li>
        ))}
      </ol>
    </>
  );
}

function QuestionCard({ sessionId, item }: { sessionId: string; item: PracticeQuestionReview }) {
  const qc = useQueryClient();
  const answered = !!item.answeredAt;
  const [choice, setChoice] = useState<number | null>(null);
  const [text, setText] = useState("");
  const submit = useMutation({
    mutationFn: () =>
      practiceApi.answer(sessionId, item.type === "MCQ" ? { questionId: item.questionId, selectedOptionIndex: choice! } : { questionId: item.questionId, answerText: text.trim() }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["practice"] });
      qc.invalidateQueries({ queryKey: ["progress"] });
    },
  });

  function onSubmit(e: FormEvent) {
    e.preventDefault();
    submit.mutate();
  }
  const name = `q-${item.questionId}`;

  return (
    <Card>
      <div className="flex flex-wrap items-center gap-2">
        <span className="font-display text-sm font-bold text-muted-foreground">Q{item.position}</span>
        <Badge tone={difficultyTone(item.difficulty)}>{humanize(item.difficulty)}</Badge>
        {answered && item.correct !== null && <Badge tone={item.correct ? "success" : "danger"}>{item.correct ? "Correct" : "Incorrect"}</Badge>}
      </div>
      <h3 className="mt-2 text-lg font-bold">{item.title}</h3>
      <p className="mt-1 whitespace-pre-wrap text-sm">{item.questionText}</p>

      {answered ? (
        <div className="mt-4 space-y-3 text-sm">
          {item.type === "MCQ" && item.options ? (
            <ul className="space-y-2">
              {item.options.map((o, i) => (
                <li
                  key={i}
                  className={cn(
                    "rounded-lg border px-3 py-2",
                    i === item.correctOptionIndex ? "border-success bg-success/10" : i === item.selectedOptionIndex ? "border-destructive bg-destructive/8" : "border-border",
                  )}
                >
                  <span className="font-semibold">{String.fromCharCode(65 + i)}.</span> {o}
                  {i === item.selectedOptionIndex && <span className="ml-2 text-xs text-muted-foreground">(your answer)</span>}
                </li>
              ))}
            </ul>
          ) : (
            <div className="rounded-lg bg-muted p-3"><p className="text-xs font-semibold text-muted-foreground">Your answer</p><p className="whitespace-pre-wrap">{item.submittedAnswerText}</p></div>
          )}
          {item.answerText && <div className="rounded-lg border border-primary/30 bg-primary/5 p-3"><p className="text-xs font-semibold text-primary">Reference answer</p><p className="whitespace-pre-wrap">{item.answerText}</p></div>}
          {item.explanation && <div><p className="text-xs font-semibold text-muted-foreground">Explanation</p><p className="whitespace-pre-wrap">{item.explanation}</p></div>}
        </div>
      ) : (
        <form onSubmit={onSubmit} className="mt-4 space-y-3">
          {item.type === "MCQ" && item.options ? (
            <fieldset className="space-y-2">
              <legend className="sr-only">Choose an answer</legend>
              {item.options.map((o, i) => (
                <label key={i} className={cn("flex cursor-pointer items-start gap-3 rounded-lg border px-3 py-2 text-sm", choice === i ? "border-primary bg-primary/5" : "border-border hover:bg-muted")}>
                  <input type="radio" name={name} className="mt-1 accent-[var(--primary)]" checked={choice === i} onChange={() => setChoice(i)} />
                  <span><span className="font-semibold">{String.fromCharCode(65 + i)}.</span> {o}</span>
                </label>
              ))}
            </fieldset>
          ) : (
            <>
              <label htmlFor={name} className="sr-only">Your answer</label>
              <Textarea id={name} value={text} onChange={(e) => setText(e.target.value)} placeholder="Write your answer…" />
            </>
          )}
          {submit.error ? <ErrorBox error={submit.error} /> : null}
          <Button type="submit" disabled={submit.isPending || (item.type === "MCQ" ? choice === null : !text.trim())}>
            {submit.isPending ? "Submitting…" : "Submit answer"}
          </Button>
        </form>
      )}
    </Card>
  );
}

function Finish({ type, count }: { type: "MCQ" | "TEXT"; count: number }) {
  const start = useStartPractice();
  return (
    <div className="mt-4 flex flex-wrap items-center gap-3 border-t border-border pt-4">
      <Button onClick={() => start.mutate({ type, questionCount: count })} disabled={start.isPending}>
        {start.isPending ? "Starting…" : `Practice ${count} more`}
      </Button>
      <Link to="/dashboard" className="text-sm font-semibold text-primary">Back to dashboard</Link>
      <Link to="/progress" className="text-sm font-semibold text-primary">See progress</Link>
      {start.error ? <div className="w-full"><ErrorBox error={start.error} /></div> : null}
    </div>
  );
}
