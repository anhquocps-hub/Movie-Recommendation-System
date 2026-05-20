"use client";

import { useState } from "react";
import { useForm } from "react-hook-form";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { Button, Input } from "@/components/ui";
import { useAuthStore } from "@/stores/auth.store";
import * as authApi from "@/lib/api/auth";
import type { LoginRequest } from "@/lib/types";

export function LoginForm() {
  const router = useRouter();
  const setAuth = useAuthStore((s) => s.setAuth);
  const [error, setError] = useState("");

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginRequest>();

  const onSubmit = async (data: LoginRequest) => {
    try {
      setError("");
      const res = await authApi.login(data);
      setAuth(res.accessToken, { username: res.username, role: res.role });
      router.push("/movies");
    } catch (err: any) {
      setError(err.response?.data?.message || "Invalid credentials");
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

      <div>
        <div className="flex justify-between items-center mb-1.5">
          <label className="block text-[11px] uppercase tracking-wider text-text-muted">
            Password
          </label>
          <Link
            href="/forgot-password"
            className="text-[11px] text-accent hover:text-accent-bright transition-colors"
          >
            Forgot?
          </Link>
        </div>
        <Input
          type="password"
          placeholder="••••••••"
          error={errors.password?.message}
          {...register("password", { required: "Password is required" })}
        />
      </div>

      {error && (
        <p className="text-xs text-red-400 text-center">{error}</p>
      )}

      <Button type="submit" size="lg" className="w-full" disabled={isSubmitting}>
        {isSubmitting ? "Signing in..." : "Sign In"}
      </Button>

      <p className="text-center text-sm text-text-muted">
        Don&apos;t have an account?{" "}
        <Link href="/register" className="text-accent hover:text-accent-bright transition-colors">
          Create one
        </Link>
      </p>
    </form>
  );
}
