"use client";

import { useState } from "react";
import { useForm } from "react-hook-form";
import Link from "next/link";
import { Button, Input } from "@/components/ui";
import * as authApi from "@/lib/api/auth";
import type { ForgotPasswordRequest } from "@/lib/types";

export default function ForgotPasswordPage() {
  const [sent, setSent] = useState(false);
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<ForgotPasswordRequest>();

  const onSubmit = async (data: ForgotPasswordRequest) => {
    await authApi.forgotPassword(data);
    setSent(true);
  };

  return (
    <div className="min-h-screen bg-bg-primary flex items-center justify-center relative overflow-hidden">
      <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_50%_40%,rgba(212,165,116,0.06)_0%,transparent_60%)]" />

      <div className="absolute top-8 left-1/2 -translate-x-1/2 font-[family-name:var(--font-playfair)] text-[22px] text-accent-bright tracking-[2px]">
        CINÉMA
      </div>

      <div className="relative w-[380px] p-10 bg-glass-bg border border-glass-border rounded-2xl backdrop-blur-xl z-10">
        <h1 className="font-[family-name:var(--font-playfair)] text-2xl text-text-primary mb-2">
          Reset Password
        </h1>
        <p className="text-sm text-text-muted mb-7">
          Enter your email and we&apos;ll send a reset link
        </p>

        {sent ? (
          <div className="text-center">
            <p className="text-sm text-text-secondary mb-4">
              If that email exists, a reset link has been sent.
            </p>
            <Link href="/login" className="text-accent hover:text-accent-bright text-sm">
              Back to login
            </Link>
          </div>
        ) : (
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
            <Input
              label="Email"
              type="email"
              placeholder="user@example.com"
              error={errors.email?.message}
              {...register("email", { required: "Email is required" })}
            />
            <Button type="submit" size="lg" className="w-full" disabled={isSubmitting}>
              {isSubmitting ? "Sending..." : "Send Reset Link"}
            </Button>
            <p className="text-center text-sm text-text-muted">
              <Link href="/login" className="text-accent hover:text-accent-bright">
                Back to login
              </Link>
            </p>
          </form>
        )}
      </div>
    </div>
  );
}
