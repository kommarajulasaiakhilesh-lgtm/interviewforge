import { useEffect, useState, type FormEvent } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Badge, Button, Card, ErrorBox, Field, Loading, Textarea } from "@/components/kit";
import { useStartPractice } from "@/components/practice";
import { preparationGoalApi } from "@/lib/api/endpoints";
import type { JobRequirementEvidenceMap } from "@/lib/api/types";
import { pct } from "@/lib/format";

export function JobRequirementEvidenceCard({ roleId }: { roleId: string }) {
  const queryClient = useQueryClient();
  const goal = useQuery({ queryKey: ["preparationGoal"], queryFn: preparationGoalApi.get });
  const evidence = useQuery({
    queryKey: ["jobRequirementEvidence", roleId],
    queryFn: preparationGoalApi.evidenceMap,
    enabled: goal.data?.targetRoleId === roleId && Boolean(goal.data?.jobDescription?.trim()),
  });
  const [jobDescription, setJobDescription] = useState("");
  const startPractice = useStartPractice();

  useEffect(() => setJobDescription(goal.data?.jobDescription ?? ""), [goal.data?.jobDescription]);

  const saveAndAnalyze = useMutation({
    mutationFn: async () => {
      const current = goal.data ?? await preparationGoalApi.get();
      return preparationGoalApi.update({ ...current, targetRoleId: roleId, jobDescription });
    },
    onSuccess: async (saved) => {
      queryClient.setQueryData(["preparationGoal"], saved);
      await queryClient.invalidateQueries({ queryKey: ["jobRequirementEvidence", roleId] });
    },
  });

  function submit(event: FormEvent) {
    event.preventDefault();
    saveAndAnalyze.mutate();
  }

  const savedForRole = goal.data?.targetRoleId === roleId && goal.data.jobDescription === jobDescription;
  const result = evidence.data as JobRequirementEvidenceMap | undefined;

  return (
    <Card className="mt-6">
      <div className="max-w-3xl">
        <h2 className="text-lg font-bold">Job requirement evidence map</h2>
        <p className="mt-1 text-sm text-muted-foreground">
          Paste a job description to connect its requirements to this role’s skills, mapped interview topics, and your practice history. Saving it also sets this role as your target role.
        </p>
        <form onSubmit={submit} className="mt-4 space-y-3">
          <Field label="Job description" htmlFor="job-evidence-description" hint={`${jobDescription.length}/12000 characters. Stored in your account and matched locally by role skill phrases.`}>
            <Textarea id="job-evidence-description" maxLength={12000} value={jobDescription} onChange={(event) => setJobDescription(event.target.value)} placeholder="Paste the responsibilities and qualifications here…" />
          </Field>
          {saveAndAnalyze.error ? <ErrorBox error={saveAndAnalyze.error} /> : null}
          <Button type="submit" disabled={saveAndAnalyze.isPending || jobDescription.trim().length === 0}>
            {saveAndAnalyze.isPending ? "Saving and mapping…" : "Save and build evidence map"}
          </Button>
        </form>
      </div>

      {goal.isLoading ? <Loading /> : goal.error ? <div className="mt-4"><ErrorBox error={goal.error} onRetry={() => goal.refetch()} /></div> : null}
      {savedForRole && evidence.isLoading ? <Loading label="Building your evidence map…" /> : null}
      {savedForRole && evidence.error ? <div className="mt-4"><ErrorBox error={evidence.error} onRetry={() => evidence.refetch()} /></div> : null}

      {savedForRole && result ? (
        <div className="mt-6">
          <div className="flex flex-wrap gap-2" aria-label="Mapping summary">
            <Badge tone="primary">{result.mappedRequirementCount} mapped</Badge>
            <Badge tone={result.unmappedRequirementCount ? "warning" : "success"}>{result.unmappedRequirementCount} not mapped</Badge>
            <Badge>{result.requirementCount} requirements found</Badge>
          </div>
          <p className="mt-3 max-w-4xl text-xs text-muted-foreground">{result.matchingMethod} {result.guidance}</p>
          {result.requirements.length === 0 ? <p className="mt-4 text-sm text-muted-foreground">No distinct requirements were found. Try pasting the job responsibilities and qualifications as separate lines.</p> : (
            <ul className="mt-4 space-y-3">
              {result.requirements.map((requirement, index) => (
                <li key={`${index}-${requirement.requirementText}`} className="rounded-lg border border-border p-4">
                  <div className="flex flex-wrap items-start justify-between gap-2">
                    <p className="max-w-4xl text-sm font-semibold">{requirement.requirementText}</p>
                    <Badge tone={requirement.matchStatus === "MAPPED" ? "success" : "warning"}>{requirement.matchStatus === "MAPPED" ? "Mapped" : "Review manually"}</Badge>
                  </div>
                  {requirement.skills.length === 0 ? <p className="mt-2 text-sm text-muted-foreground">No phrase match in this role’s skill map. Review this requirement manually.</p> : (
                    <ul className="mt-3 space-y-3">
                      {requirement.skills.map((skill) => (
                        <li key={skill.skillId} className="border-l-2 border-primary/40 pl-3">
                          <p className="text-sm font-semibold">{skill.skillName} <span className="text-xs font-normal text-muted-foreground">role importance {skill.roleImportance}/5 · {skill.practiceStatus === "HAS_PRACTICE_HISTORY" ? "practice history exists" : "no practice yet"}</span></p>
                          {skill.topics.length ? <ul className="mt-2 flex flex-wrap gap-2">{skill.topics.map((topic) => (
                            <li key={topic.topicId} className="flex flex-wrap items-center gap-2 rounded-md bg-muted px-2 py-1 text-xs">
                              <span>{topic.topicName} · {topic.questionsAnswered} answered{topic.accuracyPercent === null ? "" : ` · ${pct(topic.accuracyPercent)}`} · {topic.publishedQuestionCount} MCQs available</span>
                              {topic.publishedQuestionCount > 0 ? <Button type="button" size="sm" variant="secondary" disabled={startPractice.isPending} onClick={() => startPractice.mutate({ topicId: topic.topicId, type: "MCQ", questionCount: Math.min(5, topic.publishedQuestionCount) })}>Practice</Button> : null}
                            </li>
                          ))}</ul> : <p className="mt-1 text-xs text-muted-foreground">No active topics are mapped to this skill yet.</p>}
                        </li>
                      ))}
                    </ul>
                  )}
                </li>
              ))}
            </ul>
          )}
          {startPractice.error ? <div className="mt-3"><ErrorBox error={startPractice.error} /></div> : null}
        </div>
      ) : null}
    </Card>
  );
}
