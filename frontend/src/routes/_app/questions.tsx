import { createFileRoute } from "@tanstack/react-router";
import { keepPreviousData, useQuery } from "@tanstack/react-query";
import { useState, type FormEvent } from "react";
import { Badge, Button, Card, Empty, ErrorBox, Field, Input, Loading, PageHeader, Pager, Select, difficultyTone } from "@/components/kit";
import { useStartPractice } from "@/components/practice";
import { catalogApi } from "@/lib/api/endpoints";
import type { Difficulty, QuestionFilters } from "@/lib/api/types";
import { humanize } from "@/lib/format";

export const Route = createFileRoute("/_app/questions")({
  head: () => ({ meta: [{ title: "Question bank — InterviewForge" }, { name: "description", content: "Browse published interview questions." }] }),
  component: Questions,
});

function Questions() {
  const topics = useQuery({ queryKey: ["topics"], queryFn: catalogApi.topics });
  const tags = useQuery({ queryKey: ["tags"], queryFn: catalogApi.tags });
  const [filters, setFilters] = useState<QuestionFilters>({ page: 0, size: 20 });
  const [search, setSearch] = useState("");
  const [open, setOpen] = useState<string | null>(null);
  const q = useQuery({ queryKey: ["questions", filters], queryFn: () => catalogApi.questions(filters), placeholderData: keepPreviousData });
  const start = useStartPractice();

  const set = (patch: QuestionFilters) => setFilters((f) => ({ ...f, ...patch, page: 0 }));
  function onSearch(e: FormEvent) {
    e.preventDefault();
    set({ search: search.trim() || undefined });
  }

  return (
    <>
      <PageHeader title="Question bank" description="Published questions. Answers are revealed only after you answer in a practice session." />
      <Card className="mb-6">
        <form onSubmit={onSearch} className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <Field label="Search" htmlFor="q-search">
            <div className="flex gap-2">
              <Input id="q-search" type="search" value={search} onChange={(e) => setSearch(e.target.value)} placeholder="e.g. hash" />
              <Button type="submit" variant="secondary">Go</Button>
            </div>
          </Field>
          <Field label="Topic" htmlFor="q-topic">
            <Select id="q-topic" value={filters.topicId ?? ""} onChange={(e) => set({ topicId: e.target.value || undefined })}>
              <option value="">All topics</option>
              {topics.data?.map((t) => <option key={t.id} value={t.id}>{t.name}</option>)}
            </Select>
          </Field>
          <Field label="Tag" htmlFor="q-tag">
            <Select id="q-tag" value={filters.tagId ?? ""} onChange={(e) => set({ tagId: e.target.value || undefined })}>
              <option value="">All tags</option>
              {tags.data?.map((t) => <option key={t.id} value={t.id}>{t.name}</option>)}
            </Select>
          </Field>
          <Field label="Difficulty" htmlFor="q-diff">
            <Select id="q-diff" value={filters.difficulty ?? ""} onChange={(e) => set({ difficulty: (e.target.value || undefined) as Difficulty | undefined })}>
              <option value="">Any</option>
              <option value="EASY">Easy</option>
              <option value="MEDIUM">Medium</option>
              <option value="HARD">Hard</option>
            </Select>
          </Field>
        </form>
        <div className="mt-4 flex flex-wrap items-center gap-3 border-t border-border pt-4">
          <Button
            variant="accent"
            size="sm"
            disabled={start.isPending}
            onClick={() => start.mutate({ topicId: filters.topicId, difficulty: filters.difficulty, type: "MCQ", questionCount: 5 })}
          >
            {start.isPending ? "Creating…" : "Practice 5 MCQs with these filters"}
          </Button>
          <span className="text-xs text-muted-foreground">Uses topic and difficulty filters.</span>
        </div>
        {start.error ? <div className="mt-3"><ErrorBox error={start.error} /></div> : null}
      </Card>

      {q.isLoading ? <Loading /> : q.error ? <ErrorBox error={q.error} onRetry={() => q.refetch()} /> : q.data && q.data.content.length === 0 ? (
        <Empty title="No questions match">Try removing a filter.</Empty>
      ) : q.data && (
        <>
          <p className="mb-3 text-sm text-muted-foreground">{q.data.totalElements} questions</p>
          <ul className="space-y-3">
            {q.data.content.map((item) => (
              <li key={item.id}>
                <Card className="p-4">
                  <button className="w-full text-left" aria-expanded={open === item.id} onClick={() => setOpen(open === item.id ? null : item.id)}>
                    <div className="flex flex-wrap items-center gap-2">
                      <Badge tone={difficultyTone(item.difficulty)}>{humanize(item.difficulty)}</Badge>
                      <Badge tone="primary">{item.type === "MCQ" ? "Multiple choice" : "Written"}</Badge>
                      <span className="text-xs text-muted-foreground">{item.topic.name}</span>
                    </div>
                    <p className="mt-2 font-semibold">{item.title}</p>
                  </button>
                  {open === item.id && (
                    <div className="mt-3 border-t border-border pt-3 text-sm">
                      <p className="whitespace-pre-wrap">{item.questionText}</p>
                      {item.options && item.options.length > 0 && (
                        <ol className="mt-2 list-[upper-alpha] space-y-1 pl-5 text-muted-foreground">
                          {item.options.map((o, i) => <li key={i}>{o}</li>)}
                        </ol>
                      )}
                      {item.tags.length > 0 && (
                        <div className="mt-3 flex flex-wrap gap-1">{item.tags.map((t) => <Badge key={t.id}>#{t.name}</Badge>)}</div>
                      )}
                    </div>
                  )}
                </Card>
              </li>
            ))}
          </ul>
          <Pager page={q.data.page} totalPages={q.data.totalPages} onChange={(p) => setFilters((f) => ({ ...f, page: p }))} />
        </>
      )}
    </>
  );
}
