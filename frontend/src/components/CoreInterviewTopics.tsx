import { useQuery } from "@tanstack/react-query";
import { Button, Card, Empty, ErrorBox, Loading } from "@/components/kit";
import { useStartPractice } from "@/components/practice";
import { catalogApi } from "@/lib/api/endpoints";

const INTERVIEW_TOPIC_PRIORITY = [
  /data structure|algorithm|problem.?solv/i,
  /system design|architecture/i,
  /object.oriented|design pattern/i,
  /database|sql|postgres|data modell/i,
  /operating system|concurren|thread/i,
  /network|http|rest api|web service/i,
  /java|programming|language fundamental/i,
  /behavior|communication|leadership|teamwork/i,
];

function topicPriority(name: string) {
  const priority = INTERVIEW_TOPIC_PRIORITY.findIndex((pattern) => pattern.test(name));
  return priority < 0 ? INTERVIEW_TOPIC_PRIORITY.length : priority;
}

export function CoreInterviewTopics() {
  const topics = useQuery({ queryKey: ["topics"], queryFn: catalogApi.topics });
  const start = useStartPractice();
  const ordered = [...(topics.data ?? [])]
    .filter((topic) => topic.active)
    .sort((a, b) => topicPriority(a.name) - topicPriority(b.name) || a.name.localeCompare(b.name));
  const highlighted = ordered.filter(
    (topic) => topicPriority(topic.name) < INTERVIEW_TOPIC_PRIORITY.length,
  );
  const suggestions = (highlighted.length ? highlighted : ordered).slice(0, 8);

  return (
    <Card className="mb-8">
      <h2 className="text-lg font-bold">Core interview topics</h2>
      <p className="mt-1 text-sm text-muted-foreground">
        Choose a concept to practice its three starter MCQs with explanations and theory notes.
      </p>
      {start.error && (
        <div className="mt-4">
          <ErrorBox error={start.error} />
        </div>
      )}
      {topics.isLoading ? (
        <Loading />
      ) : topics.error ? (
        <div className="mt-4">
          <ErrorBox error={topics.error} onRetry={() => topics.refetch()} />
        </div>
      ) : suggestions.length === 0 ? (
        <div className="mt-4">
          <Empty title="No topics published yet">
            Add active topics and MCQs to the question bank to make quick practice sessions
            available.
          </Empty>
        </div>
      ) : (
        <div className="mt-4 grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
          {suggestions.map((topic) => (
            <div key={topic.id} className="rounded-lg border border-border p-3">
              <p className="font-semibold">{topic.name}</p>
              {topic.description && (
                <p className="mt-1 line-clamp-2 text-xs text-muted-foreground">
                  {topic.description}
                </p>
              )}
              <Button
                className="mt-3 w-full"
                size="sm"
                variant="accent"
                disabled={start.isPending}
                onClick={() => start.mutate({ topicId: topic.id, type: "MCQ", questionCount: 3 })}
              >
                {start.isPending ? "Preparing…" : "Practice 3 MCQs"}
              </Button>
            </div>
          ))}
        </div>
      )}
      {highlighted.length === 0 && suggestions.length > 0 && (
        <p className="mt-3 text-xs text-muted-foreground">
          These are the currently available topics, ordered alphabetically. Add topics such as data
          structures, system design, databases, and behavioral interviews to highlight them here.
        </p>
      )}
    </Card>
  );
}
