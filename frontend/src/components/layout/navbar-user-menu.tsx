"use client";

import { useState, useRef, useEffect } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/stores/auth.store";
import * as authApi from "@/lib/api/auth";

interface NavbarUserMenuProps {
  username: string;
  role: string;
}

export function NavbarUserMenu({ username, role }: NavbarUserMenuProps) {
  const [open, setOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);
  const router = useRouter();
  const clearAuth = useAuthStore((s) => s.clearAuth);

  useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false);
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const handleLogout = async () => {
    try { await authApi.logout(); } catch {}
    clearAuth();
    router.push("/");
  };

  return (
    <div ref={ref} className="relative">
      <button
        onClick={() => setOpen(!open)}
        className="flex items-center gap-2 text-sm text-text-secondary hover:text-text-primary transition-colors"
      >
        <div className="w-8 h-8 rounded-full bg-bg-elevated border border-border flex items-center justify-center text-xs text-accent">
          {username[0]?.toUpperCase()}
        </div>
      </button>

      {open && (
        <div className="absolute right-0 top-12 w-48 bg-bg-elevated border border-glass-border rounded-lg shadow-xl py-2 z-50">
          <div className="px-4 py-2 border-b border-border">
            <p className="text-sm text-text-primary">{username}</p>
            <p className="text-[10px] text-text-dim uppercase">{role}</p>
          </div>
          <Link href="/profile" className="block px-4 py-2 text-sm text-text-secondary hover:bg-glass-bg" onClick={() => setOpen(false)}>
            Profile
          </Link>
          {role === "ADMIN" && (
            <Link href="/admin/movies" className="block px-4 py-2 text-sm text-text-secondary hover:bg-glass-bg" onClick={() => setOpen(false)}>
              Admin Panel
            </Link>
          )}
          <button onClick={handleLogout} className="w-full text-left px-4 py-2 text-sm text-red-400 hover:bg-glass-bg">
            Sign Out
          </button>
        </div>
      )}
    </div>
  );
}
