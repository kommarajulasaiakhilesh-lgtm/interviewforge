import { createFileRoute } from "@tanstack/react-router";
import { keepPreviousData, useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { Badge, Card, Empty, ErrorBox, Loading, Meter, PageHeader, Pager, Stat, levelTone } from "@/components/kit";
import { PracticeSessionRow } from "@/components/practice";
import { progressApi } from "@/lib/api/endpoints";
import { fmtDate, humanize, pct } from "@/lib/format";

export const Route = createFileRoute("/_app/progress")({
  head: () => ({ meta: [{ title: "Progress — InterviewForge" }, { name: "description", content: "Overall and per-topic results." }] }),
  component: Progress,
});

function Progress() {
  const [page, setPage] = useState(0);
  const overview = useQuery({ queryKey: ["progress", "overview"], queryFn: progressApi.overview });
  const topics = useQuery({ queryKey: ["progress", "topics"], queryFn: progressApi.topics });
  const attempts = useQuery({ queryKey: ["progress", "attempts", page], queryFn: () => progressApi.attempts(page, 10), placeholderData: keepPreviousData });
  const o = overview.data;

  return (
    <>
      <PageHeader title="Progress" description="Accuracy is based on multiple-choice answers only." />
      {overview.isLoading ? <Loading /> : overview.error ? <ErrorBox error={overview.error} onRetry={() => overview.refetch()} /> : o && (
        <div className="grid grid-cols-2 gap-3 lg:grid-cols-4">
          <Stat label="MCQ accuracy" value={o.accuracyPercent === null ? "No data" : pct(o.accuracyPercent)} sub={o.accuracyPercent === null ? "Answer an MCQ to start" : "Text answers excluded"} />
          <Stat label="MCQs answered" value={o.mcqQuestionsAnswered} sub={`${o.mcqCorrectAnswers} correct`} />
          <Stat label="Completed" value={o.completedSessions} />
          <Stat label="In progress" value={o.inProgressSessions} />
        </div>
      )}

      <Card className="mt-8">
        <h2 className="text-lg font-bold">By topic</h2>
        {topics.data && <p className="mb-4 text-sm text-muted-foreground">Ratings need at least {topics.data.minimumAttemptsForRating} answers. Strong ≥ 80%, Developing 60–79%, Needs work &lt; 60%.</p>}
        {topics.isLoading ? <Loading /> : topics.error ? <ErrorBox error={topics.error} /> : topics.data?.topics.length === 0 ? (
          <Empty title="No graded answers yet">Answer some multiple-choice questions to see topic results.</Empty>
        ) : (
          <ul className="divide-y divide-border">
            {topics.data?.topics.map((t) => (
              <li key={t.topicId} className="grid gap-2 py-3 sm:grid-cols-[1fr_200px_auto] sm:items-center">
                <div>
                  <p className="font-semibold">{t.topicName}</p>
                  <p className="text-xs text-muted-foreground">{t.correctAnswers}/{t.questionsAnswered} correct · last {fmtDate(t.lastAttemptAt)}</p>
                </div>
                <div className="flex items-center gap-2"><Meter value={t.accuracyPercent} label={`${t.topicName} accuracy`} /><span className="w-12 text-right text-sm font-semibold">{pct(t.accuracyPercent)}</span></div>
                <Badge tone={levelTone(t.level)}>{humanize(t.level)}</Badge>
              </li>
            ))}
          </ul>
        )}
      </Card>

      <h2 className="mb-3 mt-8 text-lg font-bold">Attempt history</h2>
      {attempts.isLoading ? <Loading /> : attempts.error ? <ErrorBox error={attempts.error} /> : attempts.data?.content.length === 0 ? (
        <Empty title="No attempts yet" />
      ) : attempts.data && (
        <>
          <div className="space-y-2">{attempts.data.content.map((s) => <PracticeSessionRow key={s.sessionId} s={s} />)}</div>
          <Pager page={attempts.data.page} totalPages={attempts.data.totalPages} onChange={setPage} />
        </>
      )}
    </>
  );
}
