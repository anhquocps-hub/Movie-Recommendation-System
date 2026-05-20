"use client";

import { Suspense, useState } from "react";
import { useForm } from "react-hook-form";
import { useSearchParams } from "next/navigation";
import Link from "next/link";
import { Button, Input } from "@/components/ui";
import * as authApi from "@/lib/api/auth";

interface ResetForm {
  newPassword: string;
  confirmPassword: string;
}

function ResetPasswordContent() {
  const searchParams = useSearchParams();
  const token = searchParams.get("token") || "";
  const [done, setDone] = useState(false);
  const [error, setError] = useState("");

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<ResetForm>();

  const onSubmit = async (data: ResetForm) => {
    try {
      setError("");
      await authApi.resetPassword({ token, newPassword: data.newPassword });
      setDone(true);
    } catch (err: any) {
      setError(err.response?.data?.message || "Reset failed. Token may be expired.");
    }
  };

  return (
    <div className="relative w-[380px] p-10 bg-glass-bg border border-glass-border rounded-2xl backdrop-blur-xl z-10">
      <h1 className="font-[family-name:var(--font-playfair)] text-2xl text-text-primary mb-2">
        New Password
      </h1>
      <p className="text-sm text-text-muted mb-7">Choose a new password</p>

      {done ? (
        <div className="text-center">
          <p className="text-sm text-text-secondary mb-4">
            Password reset successful!
          </p>
          <Link href="/login" className="text-accent hover:text-accent-bright text-sm">
            Sign in with new password
          </Link>
        </div>
      ) : (
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
          <Input
            label="New Password"
            type="password"
            placeholder="••••••••"
            error={errors.newPassword?.message}
            {...register("newPassword", {
              required: "Password is required",
              minLength: { value: 6, message: "At least 6 characters" },
            })}
          />
          <Input
            label="Confirm Password"
            type="password"
            placeholder="••••••••"
            error={errors.confirmPassword?.message}
            {...register("confirmPassword", {
              required: "Please confirm password",
              validate: (val) => val === watch("newPassword") || "Passwords don't match",
            })}
          />
          {error && <p className="text-xs text-red-400 text-center">{error}</p>}
          <Button type="submit" size="lg" className="w-full" disabled={isSubmitting}>
            {isSubmitting ? "Resetting..." : "Reset Password"}
          </Button>
        </form>
      )}
    </div>
  );
}

export default function ResetPasswordPage() {
  return (
    <div className="min-h-screen bg-bg-primary flex items-center justify-center relative overflow-hidden">
      <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_50%_40%,rgba(212,165,116,0.06)_0%,transparent_60%)]" />

      <div className="absolute top-8 left-1/2 -translate-x-1/2 font-[family-name:var(--font-playfair)] text-[22px] text-accent-bright tracking-[2px]">
        CINÉMA
      </div>

      <Suspense fallback={null}>
        <ResetPasswordContent />
      </Suspense>
    </div>
  );
}
