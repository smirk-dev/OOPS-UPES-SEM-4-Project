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
    <main className="mx-auto mt-10 max-w-md">
      <AppCard>
        <h1 className="text-xl font-semibold">Create Account</h1>
        <p className="mt-1 text-sm text-muted">Student and vendor self-registration are supported. Admin accounts are internal.</p>

        <div className="mt-4 grid grid-cols-2 gap-2">
          {[Role.STUDENT, Role.VENDOR].map((item) => (
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
          <FormField label="Full Name" name="fullName" value={fullName} onChange={(event) => setFullName(event.target.value)} />
          <FormField label="Username" name="username" value={username} onChange={(event) => setUsername(event.target.value)} />
          <FormField label="Email" name="email" type="email" value={email} onChange={(event) => setEmail(event.target.value)} />
          <FormField label="Phone" name="phone" value={phone} onChange={(event) => setPhone(event.target.value)} />
          <FormField label="Password" name="password" type="password" value={password} onChange={(event) => setPassword(event.target.value)} />

          {role === Role.VENDOR ? (
            <>
              <FormField label="Shop Name" name="shopName" value={shopName} onChange={(event) => setShopName(event.target.value)} />
              <label className="block text-sm font-medium">
                Vertical
                <select
                  className="mt-1 w-full rounded-md border border-[var(--card-border)] px-3 py-2"
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
            </>
          ) : null}

          <FormError message={error} />
          <AppButton type="submit" disabled={loading} className="w-full">
            {loading ? "Creating account..." : `Sign up as ${role}`}
          </AppButton>
        </form>

        <p className="mt-4 text-center text-sm text-muted">
          Already have an account? <Link href="/login" className="text-primary underline">Go to login</Link>.
        </p>
      </AppCard>
    </main>
  );
}