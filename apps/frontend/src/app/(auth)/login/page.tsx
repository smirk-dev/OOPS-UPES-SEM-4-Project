"use client";

import { useState, type FormEvent } from "react";
import { useRouter } from "next/navigation";
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
    <main className="mx-auto mt-10 max-w-md">
      <AppCard>
        <h1 className="text-xl font-semibold">Role-Aware Login</h1>
        <p className="mt-1 text-sm text-muted">Choose role first, then authenticate.</p>

        <div className="mt-4 grid grid-cols-3 gap-2">
          {[Role.STUDENT, Role.VENDOR, Role.ADMIN].map((item) => (
            <button
              key={item}
              type="button"
              onClick={() => setRole(item)}
              className={`rounded-md border px-3 py-2 text-sm font-medium transition ${
                role === item
                  ? "border-primary bg-primary text-white"
                  : "border-[var(--card-border)] bg-[var(--card)] text-[var(--text)]"
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
      </AppCard>
    </main>
  );
}
