import { createFileRoute } from "@tanstack/react-router";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useEffect, useState, type FormEvent } from "react";
import { Badge, Button, Card, Empty, ErrorBox, Field, Input, Loading, PageHeader, Select, Textarea } from "@/components/kit";
import { experienceStoryApi } from "@/lib/api/endpoints";
import type { ExperienceStory, ExperienceStoryInput, ExperienceType } from "@/lib/api/types";
import { fmtDate } from "@/lib/format";

export const Route = createFileRoute("/_app/experiences")({
  head: () => ({ meta: [{ title: "Story bank — InterviewForge" }, { name: "description", content: "Build private interview stories from your work, projects, education, and volunteering." }] }),
  component: ExperiencesPage,
});

const blank: ExperienceStoryInput = {
  title: "",
  experienceType: "PROJECT",
  context: "",
  situation: "",
  task: "",
  actions: "",
  result: "",
  learning: "",
  skillIds: [],
};

const typeLabels: Record<ExperienceType, string> = {
  WORK: "Work",
  PROJECT: "Project",
  EDUCATION: "Education",
  VOLUNTEER: "Volunteer",
  OTHER: "Other",
};

function ExperiencesPage() {
  const queryClient = useQueryClient();
  const stories = useQuery({ queryKey: ["experienceStories"], queryFn: experienceStoryApi.list });
  const competencies = useQuery({ queryKey: ["experienceCompetencies"], queryFn: experienceStoryApi.competencies });
  const [form, setForm] = useState<ExperienceStoryInput>(blank);
  const [editingId, setEditingId] = useState<string | null>(null);

  useEffect(() => {
    if (editingId && stories.data) {
      const story = stories.data.find((item) => item.id === editingId);
      if (story) setForm(toInput(story));
    }
  }, [editingId, stories.data]);

  const save = useMutation({
    mutationFn: () => editingId ? experienceStoryApi.update(editingId, form) : experienceStoryApi.create(form),
    onSuccess: async () => {
      setForm(blank);
      setEditingId(null);
      await queryClient.invalidateQueries({ queryKey: ["experienceStories"] });
    },
  });

  const remove = useMutation({
    mutationFn: experienceStoryApi.delete,
    onSuccess: async () => queryClient.invalidateQueries({ queryKey: ["experienceStories"] }),
  });

  function submit(event: FormEvent) {
    event.preventDefault();
    save.mutate();
  }

  function edit(story: ExperienceStory) {
    setForm(toInput(story));
    setEditingId(story.id);
    document.getElementById("story-title")?.focus();
  }

  function cancelEdit() {
    setEditingId(null);
    setForm(blank);
  }

  function toggleSkill(id: string) {
    setForm((current) => ({
      ...current,
      skillIds: current.skillIds.includes(id)
        ? current.skillIds.filter((skillId) => skillId !== id)
        : current.skillIds.length < 10 ? [...current.skillIds, id] : current.skillIds,
    }));
  }

  return (
    <>
      <PageHeader title="Personal story bank" description="Turn real work, project, school, or volunteer experiences into interview-ready STAR stories." />
      <div className="grid gap-6 xl:grid-cols-[minmax(0,1fr)_minmax(0,1.15fr)]">
        <Card>
          <h2 className="text-lg font-bold">{editingId ? "Edit your story" : "Create a STAR story"}</h2>
          <p className="mt-1 text-sm text-muted-foreground">Only you can access these stories. Start with rough notes; refine them before an interview.</p>
          <form onSubmit={submit} className="mt-4 space-y-4">
            <Field label="Story title" htmlFor="story-title">
              <Input id="story-title" maxLength={160} required value={form.title} onChange={(event) => setForm({ ...form, title: event.target.value })} placeholder="e.g., Reduced API response time" />
            </Field>
            <Field label="Experience source" htmlFor="story-type">
              <Select id="story-type" value={form.experienceType} onChange={(event) => setForm({ ...form, experienceType: event.target.value as ExperienceType })}>
                {Object.entries(typeLabels).map(([value, label]) => <option key={value} value={value}>{label}</option>)}
              </Select>
            </Field>
            <Field label="Context (optional)" htmlFor="story-context" hint="Team, project, course, or setting. Do not include confidential employer or customer information.">
              <Input id="story-context" maxLength={1000} value={form.context ?? ""} onChange={(event) => setForm({ ...form, context: event.target.value })} />
            </Field>
            <Field label="Situation" htmlFor="story-situation" hint="What was happening? What challenge or opportunity did you face?">
              <Textarea id="story-situation" maxLength={6000} required value={form.situation} onChange={(event) => setForm({ ...form, situation: event.target.value })} />
            </Field>
            <Field label="Task" htmlFor="story-task" hint="What were you responsible for achieving?">
              <Textarea id="story-task" maxLength={6000} required value={form.task} onChange={(event) => setForm({ ...form, task: event.target.value })} />
            </Field>
            <Field label="Actions" htmlFor="story-actions" hint="What did you personally do? Include the reasoning behind your choices.">
              <Textarea id="story-actions" maxLength={6000} required value={form.actions} onChange={(event) => setForm({ ...form, actions: event.target.value })} />
            </Field>
            <Field label="Result" htmlFor="story-result" hint="What changed? Use numbers when you can support them.">
              <Textarea id="story-result" maxLength={6000} required value={form.result} onChange={(event) => setForm({ ...form, result: event.target.value })} />
            </Field>
            <Field label="Learning or reflection (optional)" htmlFor="story-learning" hint="What would you repeat or change next time?">
              <Textarea id="story-learning" maxLength={4000} value={form.learning ?? ""} onChange={(event) => setForm({ ...form, learning: event.target.value })} />
            </Field>
            <fieldset className="space-y-2">
              <legend className="text-sm font-semibold">Competencies (optional, up to 10)</legend>
              {competencies.isLoading ? <Loading label="Loading competencies…" /> : competencies.error ? <ErrorBox error={competencies.error} onRetry={() => competencies.refetch()} /> : competencies.data?.length ? (
                <div className="grid gap-2 sm:grid-cols-2">
                  {competencies.data.map((skill) => (
                    <label key={skill.id} className="flex items-start gap-2 rounded-lg border border-border p-2 text-sm">
                      <input type="checkbox" checked={form.skillIds.includes(skill.id)} disabled={!form.skillIds.includes(skill.id) && form.skillIds.length >= 10} onChange={() => toggleSkill(skill.id)} className="mt-1 accent-primary" />
                      <span><span className="font-medium">{skill.name}</span>{skill.description && <span className="block text-xs text-muted-foreground">{skill.description}</span>}</span>
                    </label>
                  ))}
                </div>
              ) : <p className="text-sm text-muted-foreground">No active competencies are available yet.</p>}
            </fieldset>
            {save.error ? <ErrorBox error={save.error} /> : null}
            <div className="flex flex-wrap gap-2">
              <Button type="submit" disabled={save.isPending}>{save.isPending ? "Saving…" : editingId ? "Save story" : "Add story"}</Button>
              {editingId && <Button type="button" variant="secondary" onClick={cancelEdit}>Cancel edit</Button>}
            </div>
          </form>
        </Card>

        <section aria-labelledby="saved-stories-heading">
          <h2 id="saved-stories-heading" className="mb-3 text-lg font-bold">Your saved stories</h2>
          {stories.isLoading ? <Loading /> : stories.error ? <ErrorBox error={stories.error} onRetry={() => stories.refetch()} /> : stories.data?.length ? (
            <ul className="space-y-3">
              {stories.data.map((story) => (
                <li key={story.id}>
                  <StoryCard story={story} editing={editingId === story.id} onEdit={() => edit(story)} onDelete={() => remove.mutate(story.id)} deleting={remove.isPending} />
                </li>
              ))}
            </ul>
          ) : <Card><Empty title="Your story bank is empty">Save your first real example; you can revise it any time.</Empty></Card>}
          {remove.error ? <div className="mt-3"><ErrorBox error={remove.error} /></div> : null}
        </section>
      </div>
    </>
  );
}

function StoryCard({ story, editing, onEdit, onDelete, deleting }: { story: ExperienceStory; editing: boolean; onEdit: () => void; onDelete: () => void; deleting: boolean }) {
  return (
    <Card>
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <div className="flex flex-wrap items-center gap-2"><h3 className="font-bold">{story.title}</h3><Badge tone="primary">{typeLabels[story.experienceType]}</Badge></div>
          {story.context && <p className="mt-1 text-xs text-muted-foreground">{story.context}</p>}
          <p className="mt-1 text-xs text-muted-foreground">Updated {fmtDate(story.updatedAt)}</p>
        </div>
        <div className="flex gap-2">
          <Button size="sm" variant="secondary" disabled={editing} onClick={onEdit}>Edit</Button>
          <Button size="sm" variant="danger" disabled={deleting} onClick={onDelete}>Delete</Button>
        </div>
      </div>
      {story.competencies.length > 0 && <div className="mt-3 flex flex-wrap gap-1">{story.competencies.map((skill) => <Badge key={skill.id}>{skill.name}</Badge>)}</div>}
      <dl className="mt-4 grid gap-3 text-sm">
        <StoryPart title="Situation" text={story.situation} />
        <StoryPart title="Task" text={story.task} />
        <StoryPart title="Actions" text={story.actions} />
        <StoryPart title="Result" text={story.result} />
        {story.learning && <StoryPart title="Learning" text={story.learning} />}
      </dl>
    </Card>
  );
}

function StoryPart({ title, text }: { title: string; text: string }) {
  return <div><dt className="font-semibold">{title}</dt><dd className="whitespace-pre-wrap text-muted-foreground">{text}</dd></div>;
}

function toInput(story: ExperienceStory): ExperienceStoryInput {
  return {
    title: story.title,
    experienceType: story.experienceType,
    context: story.context,
    situation: story.situation,
    task: story.task,
    actions: story.actions,
    result: story.result,
    learning: story.learning,
    skillIds: story.competencies.map((skill) => skill.id),
  };
}
