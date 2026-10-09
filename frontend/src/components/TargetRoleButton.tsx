import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Badge, Button, ErrorBox } from "@/components/kit";
import { preparationGoalApi } from "@/lib/api/endpoints";
import type { Role } from "@/lib/api/types";
import { setFocusRole } from "@/lib/prefs";

export function TargetRoleButton({ role }: { role: Role }) {
  const queryClient = useQueryClient();
  const goal = useQuery({ queryKey: ["preparationGoal"], queryFn: preparationGoalApi.get });
  const save = useMutation({
    mutationFn: async () => {
      const current = goal.data ?? (await preparationGoalApi.get());
      return preparationGoalApi.update({ ...current, targetRoleId: role.id });
    },
    onSuccess: async (saved) => {
      setFocusRole({ roleId: role.id, roleName: role.name, companyName: role.companyName });
      queryClient.setQueryData(["preparationGoal"], saved);
      await queryClient.invalidateQueries({ queryKey: ["plan"] });
    },
  });
  const isTarget = goal.data?.targetRoleId === role.id;

  return (
    <div className="flex flex-wrap items-center gap-2">
      {isTarget ? (
        <Badge tone="primary">Target role</Badge>
      ) : (
        <Button
          type="button"
          size="sm"
          variant="secondary"
          disabled={goal.isLoading || save.isPending}
          onClick={() => save.mutate()}
        >
          {save.isPending ? "Saving…" : "Set as target role"}
        </Button>
      )}
      {goal.error && (
        <span className="basis-full">
          <ErrorBox error={goal.error} onRetry={() => goal.refetch()} />
        </span>
      )}
      {save.error && (
        <span className="basis-full">
          <ErrorBox error={save.error} />
        </span>
      )}
    </div>
  );
}
