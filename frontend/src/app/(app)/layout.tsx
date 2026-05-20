"use client";

import { Navbar } from "@/components/layout/navbar";
import { AuthGuard } from "@/components/auth/auth-guard";
import { NotificationPanel } from "@/components/notifications";
import { ToastContainer } from "@/components/notifications";

export default function AppLayout({ children }: { children: React.ReactNode }) {
  return (
    <AuthGuard>
      <Navbar />
      <main className="max-w-7xl mx-auto px-6 py-8">{children}</main>
      <NotificationPanel />
      <ToastContainer />
    </AuthGuard>
  );
}
