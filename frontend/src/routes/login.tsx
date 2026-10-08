import { createFileRoute, Link, useNavigate, useRouter } from "@tanstack/react-router";
import { useState, type FormEvent } from "react";
import { AuthShell } from "@/components/AuthShell";
import { Button, ErrorBox, Field, Input } from "@/components/kit";
import { authApi } from "@/lib/api/endpoints";
import { sessionFromAuth, setSession } from "@/lib/session";

export const Route = createFileRoute("/login")({
  validateSearch: (s: Record<string, unknown>) => ({
    redirect: typeof s["redirect"] === "string" && s["redirect"].startsWith("/") && !s["redirect"].startsWith("//") ? s["redirect"] : undefined,
  }),
  head: () => ({
    meta: [
      { title: "Sign in — InterviewForge" },
      { name: "description", content: "Sign in to continue your interview preparation." },
      { property: "og:title", content: "Sign in — InterviewForge" },
      { property: "og:description", content: "Sign in to continue your interview preparation." },
    ],
  }),
  component: Login,
});

function Login() {
  const { redirect } = Route.useSearch();
  const navigate = useNavigate();
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<unknown>(null);
  const [busy, setBusy] = useState(false);

  async function submit(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      const res = await authApi.login({ email: email.trim(), password });
      setPassword("");
      setSession(sessionFromAuth(res));
      if (redirect) router.history.push(redirect);
      else navigate({ to: "/dashboard" });
    } catch (err) {
      setError(err);
    } finally {
      setBusy(false);
    }
  }

  return (
    <AuthShell
      title="Welcome back"
      subtitle="Sign in to continue practicing."
      footer={<>New here? <Link to="/register" className="font-semibold text-primary">Create an account</Link></>}
    >
      <form onSubmit={submit} className="space-y-4">
        <Field label="Email" htmlFor="email">
          <Input id="email" type="email" autoComplete="email" required value={email} onChange={(e) => setEmail(e.target.value)} />
        </Field>
        <Field label="Password" htmlFor="password">
          <Input id="password" type="password" autoComplete="current-password" required value={password} onChange={(e) => setPassword(e.target.value)} />
        </Field>
        {error ? <ErrorBox error={error} /> : null}
        <Button type="submit" className="w-full" disabled={busy}>{busy ? "Signing in…" : "Sign in"}</Button>
      </form>
    </AuthShell>
  );
}
