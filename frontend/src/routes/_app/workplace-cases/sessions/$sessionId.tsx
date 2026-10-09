import { createFileRoute, Link } from "@tanstack/react-router";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { Badge, Button, Card, ErrorBox, Loading, PageHeader } from "@/components/kit";
import { workplaceCaseApi } from "@/lib/api/endpoints";
import type { DecisionQuality } from "@/lib/api/types";
import { humanize, pct } from "@/lib/format";
import { cn } from "@/lib/utils";

export const Route = createFileRoute("/_app/workplace-cases/sessions/$sessionId")({
  head: () => ({ meta: [{ title: "Workplace case — InterviewForge" }, { name: "description", content: "Work through a branching workplace case." }] }),
  component: WorkplaceCaseSessionPage,
});

function WorkplaceCaseSessionPage() {
  const { sessionId } = Route.useParams();
  const queryClient = useQueryClient();
  const [selected, setSelected] = useState<string | null>(null);
  const sessionQuery = useQuery({ queryKey: ["workplaceCase", sessionId], queryFn: () => workplaceCaseApi.session(sessionId) });
  const decision = useMutation({
    mutationFn: (choiceKey: string) => workplaceCaseApi.decide(sessionId, sessionQuery.data!.currentNode.nodeKey, choiceKey),
    onSuccess: async () => {
      setSelected(null);
      await queryClient.invalidateQueries({ queryKey: ["workplaceCase", sessionId] });
      await queryClient.invalidateQueries({ queryKey: ["workplaceCases", "history"] });
    },
  });

  if (sessionQuery.isLoading) return <Loading />;
  if (sessionQuery.error) return <ErrorBox error={sessionQuery.error} onRetry={() => sessionQuery.refetch()} />;
  const data = sessionQuery.data;
  if (!data) return null;
  const { session, currentNode, decisions } = data;
  const done = session.status === "COMPLETED";
  const last = decisions.at(-1);

  return <>
    <PageHeader title={session.caseTitle} description={`${session.roleName} · ${humanize(session.difficulty)} · Branching workplace case`} actions={<Link to="/workplace-cases" className="text-sm font-semibold text-primary">All cases</Link>} />
    <Card className="mb-5">
      <div className="flex flex-wrap items-center justify-between gap-3"><Badge tone={done ? "success" : "warning"}>{done ? "Case complete" : "In progress"}</Badge><p className="text-sm font-semibold">Decision score {pct(session.decisionScorePercent, 1)} <span className="font-normal text-muted-foreground">· {session.decisionCount} decisions</span></p></div>
      <p className="mt-4 whitespace-pre-wrap text-sm leading-6">{data.scenarioIntro}</p>
      {data.learningObjective && <p className="mt-3 rounded-lg bg-primary/5 p-3 text-sm"><span className="font-semibold">Learning objective: </span>{data.learningObjective}</p>}
    </Card>

    {currentNode.nodeType === "OUTCOME" ? <>
      {last && <Feedback heading={last.nodeHeading} quality={last.decisionQuality} choiceLabel={last.choiceLabel} choiceText={last.choiceText} explanation={last.explanation} tradeoff={last.tradeoffSummary} misconception={last.misconceptionLabel} feedback={last.choiceFeedback} />}
      <Card className="mt-5">
      <Badge tone="success">Outcome</Badge><h2 className="mt-3 text-xl font-bold">{currentNode.heading}</h2><p className="mt-2 whitespace-pre-wrap text-sm leading-6">{currentNode.situationText}</p>
      {currentNode.lessonText && <div className="mt-5 rounded-xl border border-primary/30 bg-primary/5 p-4"><p className="eyebrow text-primary">What to take away</p><p className="mt-1 whitespace-pre-wrap text-sm leading-6">{currentNode.lessonText}</p></div>}
      <p className="mt-4 text-xs text-muted-foreground">This score reflects the authored case rubric; it does not predict hiring outcomes or overall job readiness.</p>
      <Link to="/workplace-cases" className="mt-5 inline-flex text-sm font-semibold text-primary">Browse more cases →</Link>
    </Card></> : <>
      {last && <Feedback heading={last.nodeHeading} quality={last.decisionQuality} choiceLabel={last.choiceLabel} choiceText={last.choiceText} explanation={last.explanation} tradeoff={last.tradeoffSummary} misconception={last.misconceptionLabel} feedback={last.choiceFeedback} />}
      <Card className="mt-5">
        <p className="eyebrow text-primary">Step {session.decisionCount + 1} · {currentNode.nodeType === "START" ? "Situation" : "New evidence"}</p>
        <h2 className="mt-2 text-xl font-bold">{currentNode.heading}</h2><p className="mt-2 whitespace-pre-wrap text-sm leading-6">{currentNode.situationText}</p>
        <fieldset className="mt-5 space-y-2" disabled={decision.isPending}>
          <legend className="mb-2 text-sm font-bold">What would you do?</legend>
          {currentNode.choices.map((choice) => <label key={choice.choiceKey} className={cn("flex cursor-pointer items-start gap-3 rounded-lg border-2 p-3 text-sm transition-colors", selected === choice.choiceKey ? "border-primary bg-primary/5" : "border-border hover:border-primary/40 hover:bg-muted/60")}>
            <input type="radio" name="case-choice" className="mt-1 accent-[var(--primary)]" checked={selected === choice.choiceKey} onChange={() => setSelected(choice.choiceKey)} />
            <span><span className="font-semibold">{choice.choiceLabel}</span><span className="mt-0.5 block text-muted-foreground">{choice.choiceText}</span></span>
          </label>)}
        </fieldset>
        {decision.error && <div className="mt-4"><ErrorBox error={decision.error} /></div>}
        <Button className="mt-4" disabled={!selected || decision.isPending} onClick={() => selected && decision.mutate(selected)}>{decision.isPending ? "Reviewing your decision…" : "Submit decision"}</Button>
      </Card>
    </>}
  </>;
}

function Feedback({ heading, quality, choiceLabel, choiceText, explanation, tradeoff, misconception, feedback }: {
  heading: string; quality: DecisionQuality; choiceLabel: string; choiceText: string; explanation: string; tradeoff: string | null; misconception: string | null;
  feedback: { choiceKey: string; choiceLabel: string; choiceText: string; decisionQuality: DecisionQuality; explanation: string; whenAppropriate: string | null; tradeoffSummary: string | null; misconceptionLabel: string | null; selected: boolean }[];
}) {
  const tone = quality === "STRONG" ? "success" : quality === "VIABLE" ? "warning" : "danger";
  return <Card className="border-primary/40 bg-primary/[0.025]">
    <p className="eyebrow text-primary">Decision review · {heading}</p>
    <div className="mt-2 flex flex-wrap items-center gap-2"><h2 className="text-lg font-bold">{choiceLabel}</h2><Badge tone={tone}>{humanize(quality)}</Badge></div>
    <p className="mt-2 text-sm text-muted-foreground">{choiceText}</p><p className="mt-3 whitespace-pre-wrap text-sm leading-6">{explanation}</p>
    {tradeoff && <p className="mt-3 rounded-lg bg-muted p-3 text-sm"><span className="font-semibold">Trade-off: </span>{tradeoff}</p>}
    {misconception && <p className="mt-3 rounded-lg bg-warning/15 p-3 text-sm"><span className="font-semibold">Watch for: </span>{misconception}</p>}
    <details className="mt-4"><summary className="cursor-pointer text-sm font-semibold">Compare the other options</summary><ul className="mt-3 space-y-3">{feedback.map((item) => <li key={item.choiceKey} className="rounded-lg border border-border p-3 text-sm"><div className="flex flex-wrap items-center gap-2"><span className="font-semibold">{item.choiceLabel}{item.selected ? " · Your choice" : ""}</span><Badge tone={item.decisionQuality === "STRONG" ? "success" : item.decisionQuality === "VIABLE" ? "warning" : "danger"}>{humanize(item.decisionQuality)}</Badge></div><p className="mt-1 text-muted-foreground">{item.explanation}</p>{item.whenAppropriate && <p className="mt-2 text-xs"><span className="font-semibold">Could fit when: </span>{item.whenAppropriate}</p>}</li>)}</ul></details>
  </Card>;
}
