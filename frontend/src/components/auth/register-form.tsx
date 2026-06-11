"use client";

import { useState } from "react";
import { useForm } from "react-hook-form";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { Button, Input } from "@/components/ui";
import { useAuthStore } from "@/stores/auth.store";
import * as authApi from "@/lib/api/auth";
import type { RegisterRequest } from "@/lib/types";
import { validatePassword } from "@/lib/validation/password";

function getPasswordStrength(password: string): { label: string; color: string; width: string } {
  if (!password) return { label: "", color: "", width: "0%" };
  if (password.length < 6) return { label: "Weak", color: "bg-red-500", width: "25%" };
  if (password.length < 10) return { label: "Fair", color: "bg-yellow-500", width: "50%" };
  if (/[A-Z]/.test(password) && /[0-9]/.test(password) && /[^A-Za-z0-9]/.test(password))
    return { label: "Strong", color: "bg-green-500", width: "100%" };
  return { label: "Good", color: "bg-accent", width: "75%" };
}

export function RegisterForm() {
  const router = useRouter();
  const setAuth = useAuthStore((s) => s.setAuth);
  const [error, setError] = useState("");

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<RegisterRequest>({ mode: "onChange" });

  const password = watch("password", "");
  const strength = getPasswordStrength(password);

  const onSubmit = async (data: RegisterRequest) => {
    try {
      setError("");
      const res = await authApi.register({
        ...data,
        password: data.password.trim(),
      });
      setAuth(res.accessToken, {
        username: res.user.username,
        role: res.user.role,
      });
      router.push("/movies");
    } catch (err: any) {
      const payload = err.response?.data;
      if (payload?.message === "Validation failed" && payload?.data) {
        setError(Object.values(payload.data).join(". "));
      } else {
        setError(payload?.message || "Registration failed");
      }
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
      <Input
        label="Email"
        type="email"
        placeholder="user@example.com"
        error={errors.email?.message}
        {...register("email", { required: "Email is required" })}
      />

      <Input
        label="Username"
        placeholder="your_username"
        error={errors.username?.message}
        {...register("username", {
          required: "Username is required",
          minLength: { value: 3, message: "At least 3 characters" },
        })}
      />

      <div>
        <Input
          label="Password"
          type="password"
          autoComplete="new-password"
          placeholder="••••••••"
          error={errors.password?.message}
          {...register("password", { validate: validatePassword })}
        />
        <p className="text-[10px] text-text-dim mt-1.5">
          Example: <span className="text-text-muted">Password123@</span> — special chars allowed: @ $ ! % * ? &
        </p>
        {password && (
          <div className="mt-2">
            <div className="h-1 bg-bg-elevated rounded-full overflow-hidden">
              <div
                className={`h-full ${strength.color} transition-all duration-300`}
                style={{ width: strength.width }}
              />
            </div>
            <p className="text-[10px] text-text-dim mt-1">{strength.label}</p>
          </div>
        )}
      </div>

      {error && (
        <p className="text-xs text-red-400 text-center">{error}</p>
      )}

      <Button type="submit" size="lg" className="w-full" disabled={isSubmitting}>
        {isSubmitting ? "Creating account..." : "Create Account"}
      </Button>

      <p className="text-center text-sm text-text-muted">
        Already have an account?{" "}
        <Link href="/login" className="text-accent hover:text-accent-bright transition-colors">
          Sign in
        </Link>
      </p>
    </form>
  );
}
