import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import { useState, type FormEvent } from "react";
import { AuthShell } from "@/components/AuthShell";
import { Button, ErrorBox, Field, Input } from "@/components/kit";
import { authApi } from "@/lib/api/endpoints";
import { sessionFromAuth, setSession } from "@/lib/session";

export const Route = createFileRoute("/register")({
  head: () => ({
    meta: [
      { title: "Create account — InterviewForge" },
      { name: "description", content: "Create a free student account and start preparing for interviews." },
      { property: "og:title", content: "Create account — InterviewForge" },
      { property: "og:description", content: "Create a student account and start preparing." },
    ],
  }),
  component: Register,
});

function Register() {
  const navigate = useNavigate();
  const [displayName, setDisplayName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [localError, setLocalError] = useState<string | null>(null);
  const [error, setError] = useState<unknown>(null);
  const [busy, setBusy] = useState(false);

  async function submit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    const bytes = new TextEncoder().encode(password).length;
    if (password.length < 12) return setLocalError("Password must be at least 12 characters.");
    if (password.length > 72 || bytes > 72) return setLocalError("Password is too long (max 72 characters / bytes).");
    setLocalError(null);
    setBusy(true);
    try {
      const res = await authApi.register({ email: email.trim(), password, displayName: displayName.trim() });
      setPassword("");
      setSession(sessionFromAuth(res));
      navigate({ to: "/dashboard" });
    } catch (err) {
      setError(err);
    } finally {
      setBusy(false);
    }
  }

  return (
    <AuthShell
      title="Create your account"
      subtitle="Start practicing in under a minute."
      footer={<>Already registered? <Link to="/login" search={{ redirect: undefined }} className="font-semibold text-primary">Sign in</Link></>}
    >
      <form onSubmit={submit} className="space-y-4" noValidate>
        <Field label="Display name" htmlFor="name">
          <Input id="name" autoComplete="name" required value={displayName} onChange={(e) => setDisplayName(e.target.value)} />
        </Field>
        <Field label="Email" htmlFor="email">
          <Input id="email" type="email" autoComplete="email" required value={email} onChange={(e) => setEmail(e.target.value)} />
        </Field>
        <Field label="Password" htmlFor="password" hint="12–72 characters.">
          <Input id="password" type="password" autoComplete="new-password" required value={password} onChange={(e) => setPassword(e.target.value)} aria-invalid={!!localError} />
        </Field>
        {localError && <p role="alert" className="text-sm font-medium text-destructive">{localError}</p>}
        {error ? <ErrorBox error={error} /> : null}
        <Button type="submit" className="w-full" disabled={busy}>{busy ? "Creating account…" : "Create account"}</Button>
      </form>
    </AuthShell>
  );
}
