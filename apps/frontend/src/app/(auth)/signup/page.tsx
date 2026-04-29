"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState, type FormEvent } from "react";
import { AppButton } from "@/components/ui/AppButton";
import { AppCard } from "@/components/ui/AppCard";
import { FormError } from "@/components/ui/FormError";
import { FormField } from "@/components/ui/FormField";
import { authStorage } from "@/lib/auth-storage";
import { signup } from "@/lib/api-client";
import { Role, Vertical } from "@/lib/enums";

function landingPath(role: Role) {
  if (role === Role.VENDOR) return "/vendor";
  if (role === Role.ADMIN) return "/admin";
  return "/student";
}

export default function SignupPage() {
  const [role, setRole] = useState<Role>(Role.STUDENT);
  const [fullName, setFullName] = useState("");
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [phone, setPhone] = useState("");
  const [password, setPassword] = useState("");
  const [shopName, setShopName] = useState("");
  const [vertical, setVertical] = useState<Vertical>(Vertical.GROCERY);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const router = useRouter();

  const onSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError("");

    if (!fullName.trim() || !username.trim() || !password.trim()) {
      setError("Full name, username, and password are required.");
      return;
    }

    if (password.length < 8) {
      setError("Password must be at least 8 characters.");
      return;
    }

    if (email.trim() && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())) {
      setError("Please enter a valid email address.");
      return;
    }

    if (role === Role.VENDOR && (!shopName.trim() || !vertical)) {
      setError("Vendor signup requires a shop name and vertical.");
      return;
    }

    setLoading(true);
    try {
      const response = await signup({
        fullName,
        username,
        email: email.trim() || undefined,
        phone: phone.trim() || undefined,
        password,
        role,
        shopName: role === Role.VENDOR ? shopName : undefined,
        vertical: role === Role.VENDOR ? vertical : undefined,
      });

      authStorage.set(response.token, response.role);
      router.push(landingPath(response.role));
      router.refresh();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Signup failed. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="mx-auto mt-8 max-w-4xl">
      <AppCard className="overflow-hidden bg-[var(--card)]">
        <div className="grid gap-6 lg:grid-cols-[0.95fr_1.05fr] lg:items-start">
          <div className="space-y-4">
            <span className="inline-flex rounded-full border-[3px] border-[var(--card-border)] bg-[var(--surface-alt-2)] px-3 py-1 text-[11px] font-extrabold uppercase tracking-[0.16em] shadow-[4px_4px_0_var(--card-border)]">
              Create account
            </span>
            <h1 className="max-w-md text-4xl font-black tracking-tight">Build your campus identity in one punchy screen.</h1>
            <p className="max-w-lg text-sm font-medium text-muted">
              Students and vendors can self-register. Admin accounts stay internal, keeping the onboarding path clean.
            </p>
          </div>

          <div className="rounded-[1.25rem] border-[3px] border-[var(--card-border)] bg-[var(--card)] p-5 shadow-[6px_6px_0_var(--card-border)]">
            <div className="grid grid-cols-2 gap-2">
              {[Role.STUDENT, Role.VENDOR].map((item) => (
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
              <FormField label="Full Name" name="fullName" value={fullName} onChange={(event) => setFullName(event.target.value)} />
              <FormField label="Username" name="username" value={username} onChange={(event) => setUsername(event.target.value)} />
              <FormField label="Email" name="email" type="email" value={email} onChange={(event) => setEmail(event.target.value)} />
              <FormField label="Phone" name="phone" value={phone} onChange={(event) => setPhone(event.target.value)} />
              <FormField label="Password" name="password" type="password" value={password} onChange={(event) => setPassword(event.target.value)} />

              {role === Role.VENDOR ? (
                <div className="grid gap-3 rounded-[1rem] border-[3px] border-[var(--card-border)] bg-[var(--surface-alt)] p-4 shadow-[4px_4px_0_var(--card-border)]">
                  <FormField label="Shop Name" name="shopName" value={shopName} onChange={(event) => setShopName(event.target.value)} />
                  <label className="block text-sm font-semibold text-text">
                    <span className="mb-1 inline-flex rounded-full border-[3px] border-[var(--card-border)] bg-[var(--surface-alt-2)] px-3 py-1 text-[11px] font-extrabold uppercase tracking-[0.14em] shadow-[4px_4px_0_var(--card-border)]">
                      Vertical
                    </span>
                    <select
                      className="mt-2 w-full px-4 py-3"
                      value={vertical}
                      onChange={(event) => setVertical(event.target.value as Vertical)}
                    >
                      {Object.values(Vertical).map((item) => (
                        <option key={item} value={item}>
                          {item}
                        </option>
                      ))}
                    </select>
                  </label>
                </div>
              ) : null}

              <FormError message={error} />
              <AppButton type="submit" disabled={loading} className="w-full">
                {loading ? "Creating account..." : `Sign up as ${role}`}
              </AppButton>
            </form>

            <p className="mt-4 text-center text-sm font-medium text-muted">
              Already have an account? <Link href="/login" className="font-extrabold text-primary underline decoration-2 underline-offset-4">Go to login</Link>.
            </p>
          </div>
        </div>
      </AppCard>
    </main>
  );
}