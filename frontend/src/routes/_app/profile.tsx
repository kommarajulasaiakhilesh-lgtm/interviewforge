import { createFileRoute } from "@tanstack/react-router";
import { useMutation, useQuery } from "@tanstack/react-query";
import { useEffect, useState, type FormEvent } from "react";
import { Badge, Button, Card, ErrorBox, Field, Input, Loading, PageHeader } from "@/components/kit";
import { authApi } from "@/lib/api/endpoints";
import { fmtDate } from "@/lib/format";
import { updateSessionUser } from "@/lib/session";

export const Route = createFileRoute("/_app/profile")({
  head: () => ({ meta: [{ title: "Profile — InterviewForge" }, { name: "description", content: "Your account details." }] }),
  component: Profile,
});

function Profile() {
  const me = useQuery({ queryKey: ["me"], queryFn: authApi.me });
  const [name, setName] = useState("");
  useEffect(() => {
    if (me.data) {
      setName(me.data.displayName);
      updateSessionUser(me.data);
    }
  }, [me.data]);
  const save = useMutation({
    mutationFn: () => authApi.updateMe(name.trim()),
    onSuccess: (u) => {
      updateSessionUser(u);
      me.refetch();
    },
  });

  function submit(e: FormEvent) {
    e.preventDefault();
    save.mutate();
  }

  return (
    <>
      <PageHeader title="Profile" />
      {me.isLoading ? <Loading /> : me.error ? <ErrorBox error={me.error} onRetry={() => me.refetch()} /> : me.data && (
        <div className="grid gap-6 lg:grid-cols-2">
          <Card>
            <dl className="space-y-3 text-sm">
              <div><dt className="text-muted-foreground">Email</dt><dd className="font-semibold">{me.data.email}</dd></div>
              <div><dt className="text-muted-foreground">Role</dt><dd><Badge tone={me.data.role === "ADMIN" ? "accent" : "primary"}>{me.data.role}</Badge></dd></div>
              <div><dt className="text-muted-foreground">Member since</dt><dd className="font-semibold">{fmtDate(me.data.createdAt)}</dd></div>
            </dl>
          </Card>
          <Card>
            <form onSubmit={submit} className="space-y-4">
              <Field label="Display name" htmlFor="dn">
                <Input id="dn" required value={name} onChange={(e) => setName(e.target.value)} />
              </Field>
              {save.error ? <ErrorBox error={save.error} /> : null}
              {save.isSuccess && <p role="status" className="text-sm font-medium text-success">Saved.</p>}
              <Button type="submit" disabled={save.isPending || !name.trim() || name.trim() === me.data.displayName}>
                {save.isPending ? "Saving…" : "Save changes"}
              </Button>
            </form>
          </Card>
        </div>
      )}
    </>
  );
}
