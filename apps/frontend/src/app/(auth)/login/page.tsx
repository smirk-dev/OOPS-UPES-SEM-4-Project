"use client";

import { useState, type FormEvent } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { Role } from "@/lib/enums";
import { login } from "@/lib/api-client";
import { authStorage } from "@/lib/auth-storage";
import { AppButton } from "@/components/ui/AppButton";
import { AppCard } from "@/components/ui/AppCard";
import { FormError } from "@/components/ui/FormError";
import { FormField } from "@/components/ui/FormField";

function landingPath(role: Role) {
  if (role === Role.VENDOR) return "/vendor";
  if (role === Role.ADMIN) return "/admin";
  return "/student";
}

export default function LoginPage() {
  const [role, setRole] = useState<Role>(Role.STUDENT);
  const [username, setUsername] = useState("student1");
  const [password, setPassword] = useState("Student@123");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const router = useRouter();

  const onSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError("");

    if (!username.trim() || !password.trim()) {
      setError("Username and password are required.");
      return;
    }

    setLoading(true);
    try {
      const response = await login({ username, password, role });
      authStorage.set(response.token, role);
      router.push(landingPath(role));
      router.refresh();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Login failed. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="mx-auto mt-8 max-w-4xl">
      <AppCard className="overflow-hidden bg-[var(--card)]">
        <div className="grid gap-6 lg:grid-cols-[1.05fr_0.95fr] lg:items-center">
          <div className="space-y-4">
            <span className="inline-flex rounded-full border-[3px] border-[var(--card-border)] bg-[var(--surface-alt)] px-3 py-1 text-[11px] font-extrabold uppercase tracking-[0.16em] shadow-[4px_4px_0_var(--card-border)]">
              Sign in
            </span>
            <h1 className="max-w-md text-4xl font-black tracking-tight">Pick your role and jump straight in.</h1>
            <p className="max-w-lg text-sm font-medium text-muted">
              Login stays compact, but the visual treatment is intentionally bold so the auth flow feels like part of the product.
            </p>
          </div>

          <div className="rounded-[1.25rem] border-[3px] border-[var(--card-border)] bg-[var(--card)] p-5 shadow-[6px_6px_0_var(--card-border)]">
            <div className="grid grid-cols-3 gap-2">
              {[Role.STUDENT, Role.VENDOR, Role.ADMIN].map((item) => (
                <button
                  key={item}
                  type="button"
                  onClick={() => setRole(item)}
                  className={`rounded-full border-[3px] px-3 py-2 text-xs font-extrabold uppercase tracking-[0.1em] transition ${
                    role === item
                      ? "border-[var(--card-border)] bg-primary text-white"
                      : "border-[var(--card-border)] bg-[var(--surface-alt)] text-[var(--text)]"
                  }`}
                >
                  {item}
                </button>
              ))}
            </div>

            <form className="mt-5 space-y-3" onSubmit={onSubmit}>
              <FormField
                label="Username"
                name="username"
                value={username}
                onChange={(event) => setUsername(event.target.value)}
              />
              <FormField
                label="Password"
                name="password"
                type="password"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
              />
              <FormError message={error} />
              <AppButton type="submit" disabled={loading} className="w-full">
                {loading ? "Signing in..." : `Login as ${role}`}
              </AppButton>
            </form>

            <p className="mt-4 text-center text-sm font-medium text-muted">
              Need an account? <Link href="/signup" className="font-extrabold text-primary underline decoration-2 underline-offset-4">Create one here</Link>.
            </p>
          </div>
        </div>
      </AppCard>
    </main>
  );
}
