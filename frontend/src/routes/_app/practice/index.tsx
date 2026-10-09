import { createFileRoute } from "@tanstack/react-router";
import { keepPreviousData, useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { Card, Empty, ErrorBox, Loading, PageHeader, Pager } from "@/components/kit";
import { CoreInterviewTopics } from "@/components/CoreInterviewTopics";
import { PracticeSessionRow, PracticeStarter } from "@/components/practice";
import { practiceApi } from "@/lib/api/endpoints";

export const Route = createFileRoute("/_app/practice/")({
  head: () => ({ meta: [{ title: "Practice — InterviewForge" }, { name: "description", content: "Create a practice session." }] }),
  component: PracticePage,
});

function PracticePage() {
  const [page, setPage] = useState(0);
  const list = useQuery({ queryKey: ["practice", "list", page], queryFn: () => practiceApi.list(page, 10), placeholderData: keepPreviousData });
  return (
    <>
      <PageHeader title="Practice" description="Multiple choice is scored instantly. Written answers are saved for your own review." />
      <CoreInterviewTopics />
      <Card className="mb-8">
        <h2 className="mb-4 text-lg font-bold">New session</h2>
        <PracticeStarter />
      </Card>
      <h2 className="mb-3 text-lg font-bold">History</h2>
      {list.isLoading ? <Loading /> : list.error ? <ErrorBox error={list.error} onRetry={() => list.refetch()} /> : list.data?.content.length === 0 ? (
        <Empty title="No sessions yet">Your practice history will appear here.</Empty>
      ) : list.data && (
        <>
          <div className="space-y-2">{list.data.content.map((s) => <PracticeSessionRow key={s.sessionId} s={s} />)}</div>
          <Pager page={list.data.page} totalPages={list.data.totalPages} onChange={setPage} />
        </>
      )}
    </>
  );
}
