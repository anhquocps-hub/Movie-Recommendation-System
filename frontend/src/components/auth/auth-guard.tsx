"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/stores/auth.store";

interface AuthGuardProps {
  children: React.ReactNode;
  requiredRole?: string;
}

export function AuthGuard({ children, requiredRole }: AuthGuardProps) {
  const router = useRouter();
  const { accessToken, user } = useAuthStore();

  useEffect(() => {
    if (!accessToken) {
      router.replace("/login");
      return;
    }
    if (requiredRole && user?.role !== requiredRole) {
      router.replace("/movies");
    }
  }, [accessToken, user, requiredRole, router]);

  if (!accessToken) return null;
  if (requiredRole && user?.role !== requiredRole) return null;

  return <>{children}</>;
}
