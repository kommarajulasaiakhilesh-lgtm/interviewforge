import { Link, useNavigate } from "@tanstack/react-router";
import { useMutation, useQuery } from "@tanstack/react-query";
import { useState, type FormEvent } from "react";
import { Badge, Button, ErrorBox, Field, Input, Select } from "./kit";
import { catalogApi, practiceApi } from "@/lib/api/endpoints";
import type { CreatePracticeRequest, Difficulty, PracticeSessionSummary, QuestionType } from "@/lib/api/types";
import { fmtDate, humanize, pct } from "@/lib/format";

export function useStartPractice() {
  const navigate = useNavigate();
  return useMutation({
    mutationFn: (b: CreatePracticeRequest) => practiceApi.create(b),
    onSuccess: (r) => navigate({ to: "/practice/$sessionId", params: { sessionId: r.session.sessionId } }),
  });
}

export function PracticeStarter({ initial }: { initial?: Partial<CreatePracticeRequest> }) {
  const topics = useQuery({ queryKey: ["topics"], queryFn: catalogApi.topics });
  const [topicId, setTopicId] = useState(initial?.topicId ?? "");
  const [difficulty, setDifficulty] = useState<string>(initial?.difficulty ?? "");
  const [type, setType] = useState<QuestionType>(initial?.type ?? "MCQ");
  const [count, setCount] = useState(initial?.questionCount ?? 5);
  const start = useStartPractice();

  function submit(e: FormEvent) {
    e.preventDefault();
    start.mutate({
      topicId: topicId || undefined,
      difficulty: (difficulty || undefined) as Difficulty | undefined,
      type,
      questionCount: count,
    });
  }

  return (
    <form onSubmit={submit} className="space-y-4">
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <Field label="Topic" htmlFor="ps-topic">
          <Select id="ps-topic" value={topicId} onChange={(e) => setTopicId(e.target.value)} disabled={topics.isLoading}>
            <option value="">Any topic</option>
            {[...(topics.data ?? [])].sort((a, b) => a.name.localeCompare(b.name)).map((t) => <option key={t.id} value={t.id}>{t.name}</option>)}
          </Select>
        </Field>
        <Field label="Difficulty" htmlFor="ps-diff">
          <Select id="ps-diff" value={difficulty} onChange={(e) => setDifficulty(e.target.value)}>
            <option value="">Any</option>
            <option value="EASY">Easy</option>
            <option value="MEDIUM">Medium</option>
            <option value="HARD">Hard</option>
          </Select>
        </Field>
        <Field label="Type" htmlFor="ps-type">
          <Select id="ps-type" value={type} onChange={(e) => setType(e.target.value as QuestionType)}>
            <option value="MCQ">Multiple choice</option>
            <option value="TEXT">Written</option>
          </Select>
        </Field>
        <Field label="Questions (1–20)" htmlFor="ps-count">
          <Input id="ps-count" type="number" min={1} max={20} required value={count} onChange={(e) => setCount(Math.max(1, Math.min(20, Number(e.target.value) || 1)))} />
        </Field>
      </div>
      {start.error ? <ErrorBox error={start.error} /> : null}
      <Button type="submit" disabled={start.isPending}>{start.isPending ? "Creating session…" : "Start practice"}</Button>
    </form>
  );
}

export function PracticeSessionRow({ s }: { s: PracticeSessionSummary }) {
  const done = s.status === "COMPLETED";
  return (
    <Link
      to="/practice/$sessionId"
      params={{ sessionId: s.sessionId }}
      className="flex flex-wrap items-center justify-between gap-3 rounded-lg border border-border bg-card px-4 py-3 hover:border-primary/50"
    >
      <div>
        <p className="text-sm font-semibold">{s.type === "MCQ" ? "Multiple choice" : "Written"} · {s.questionCount} questions</p>
        <p className="text-xs text-muted-foreground">{fmtDate(s.startedAt)}</p>
      </div>
      <div className="flex items-center gap-2">
        <Badge tone={done ? "success" : "warning"}>{humanize(s.status)}</Badge>
        <span className="font-display text-sm font-bold">
          {done ? (s.scorePercent !== null ? pct(s.scorePercent) : "Review") : `${s.answeredCount}/${s.questionCount}`}
        </span>
      </div>
    </Link>
  );
}
