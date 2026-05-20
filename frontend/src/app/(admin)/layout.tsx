"use client";

import { Navbar } from "@/components/layout/navbar";
import { AdminSidebar } from "@/components/layout/admin-sidebar";
import { AuthGuard } from "@/components/auth/auth-guard";
import { ToastContainer } from "@/components/notifications";

export default function AdminLayout({ children }: { children: React.ReactNode }) {
  return (
    <AuthGuard requiredRole="ADMIN">
      <Navbar />
      <div className="flex">
        <AdminSidebar />
        <main className="flex-1 p-8">{children}</main>
      </div>
      <ToastContainer />
    </AuthGuard>
  );
}
