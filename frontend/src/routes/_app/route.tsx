import { createFileRoute, Link, Outlet, redirect, useNavigate, useRouter } from "@tanstack/react-router";
import { useQueryClient } from "@tanstack/react-query";
import { useEffect } from "react";
import { BarChart3, Briefcase, BookOpen, LayoutDashboard, LogOut, MessagesSquare, Target, User } from "lucide-react";
import { getSession, setSession, useSession } from "@/lib/session";
import { setUnauthorizedHandler } from "@/lib/api/client";
import { authApi } from "@/lib/api/endpoints";

export const Route = createFileRoute("/_app")({
  ssr: false,
  beforeLoad: ({ location }) => {
    if (!getSession()) throw redirect({ to: "/login", search: { redirect: location.href } });
  },
  component: AppLayout,
});

const nav = [
  { to: "/dashboard", label: "Dashboard", icon: LayoutDashboard },
  { to: "/questions", label: "Questions", icon: BookOpen },
  { to: "/practice", label: "Practice", icon: Target },
  { to: "/progress", label: "Progress", icon: BarChart3 },
  { to: "/companies", label: "Companies", icon: Briefcase },
  { to: "/interviews", label: "Interviews", icon: MessagesSquare },
  { to: "/profile", label: "Profile", icon: User },
] as const;

function AppLayout() {
  const session = useSession();
  const navigate = useNavigate();
  const router = useRouter();
  const qc = useQueryClient();

  useEffect(() => {
    setUnauthorizedHandler(() => {
      qc.clear();
      setSession(null);
      navigate({ to: "/login", search: { redirect: router.state.location.href }, replace: true });
    });
    return () => setUnauthorizedHandler(null);
  }, [qc, navigate, router]);

  async function logout() {
    try {
      await authApi.logout();
    } catch {
      /* token may already be invalid */
    }
    await qc.cancelQueries();
    qc.clear();
    setSession(null);
    navigate({ to: "/login", search: { redirect: undefined }, replace: true });
  }

  return (
    <div className="min-h-screen lg:flex">
      <a href="#main" className="sr-only focus:not-sr-only focus:absolute focus:left-2 focus:top-2 focus:z-50 focus:rounded focus:bg-card focus:p-2">Skip to content</a>
      <aside className="border-b border-border bg-ink text-ink-foreground lg:sticky lg:top-0 lg:flex lg:h-screen lg:w-64 lg:flex-col lg:border-b-0">
        <div className="flex items-center justify-between px-5 py-4">
          <Link to="/dashboard" className="flex items-center gap-2 font-display text-lg font-extrabold"><span className="grid h-8 w-8 place-items-center rounded-md bg-accent font-mono text-sm text-accent-foreground">IF</span>Interview<span className="-ml-2 text-accent">Forge</span></Link>
          <button onClick={logout} className="rounded-md p-2 hover:bg-ink-foreground/10 lg:hidden" aria-label="Sign out"><LogOut className="h-4 w-4" /></button>
        </div>
        <nav aria-label="Main" className="flex gap-1 overflow-x-auto px-3 pb-3 lg:flex-1 lg:flex-col lg:overflow-visible">
          {nav.map(({ to, label, icon: Icon }) => (
            <Link
              key={to}
              to={to}
              className="group flex shrink-0 items-center gap-3 rounded-lg border-l-2 border-transparent px-3 py-2.5 text-sm font-medium text-ink-foreground/70 transition-colors hover:bg-ink-foreground/8 hover:text-ink-foreground"
              activeProps={{ className: "!border-accent bg-ink-foreground/10 !text-ink-foreground" }}
            >
              <Icon className="h-4 w-4" aria-hidden />
              {label}
            </Link>
          ))}
        </nav>
        <div className="hidden border-t border-ink-foreground/10 p-4 lg:block">
          <p className="eyebrow mb-2 text-accent">Signed in</p>
          <p className="truncate text-sm font-semibold">{session?.user.displayName}</p>
          <p className="truncate text-xs text-ink-foreground/60">{session?.user.email}</p>
          <button onClick={logout} className="mt-3 flex items-center gap-2 text-sm text-ink-foreground/75 hover:text-ink-foreground">
            <LogOut className="h-4 w-4" aria-hidden /> Sign out
          </button>
        </div>
      </aside>
      <main id="main" className="mx-auto w-full max-w-6xl flex-1 px-4 py-6 sm:px-10 sm:py-12">
        <Outlet />
      </main>
    </div>
  );
}
