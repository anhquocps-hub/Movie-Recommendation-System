"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useAuthStore } from "@/stores/auth.store";
import { useUIStore } from "@/stores/ui.store";
import { NavbarUserMenu } from "./navbar-user-menu";

const navLinks = [
  { href: "/movies", label: "Browse" },
  { href: "/trending", label: "Trending" },
  { href: "/watchlist", label: "Watchlist" },
  { href: "/recommendations", label: "For You" },
];

export function Navbar() {
  const pathname = usePathname();
  const { user, isAuthenticated } = useAuthStore();
  const { openNotifications } = useUIStore();

  return (
    <nav className="sticky top-0 z-40 w-full border-b border-border bg-bg-primary/80 backdrop-blur-xl">
      <div className="max-w-7xl mx-auto px-6 h-16 flex items-center justify-between">
        <Link
          href="/"
          className="font-[family-name:var(--font-playfair)] text-lg text-accent-bright tracking-[2px]"
        >
          CINÉMA
        </Link>

        <div className="hidden md:flex items-center gap-8">
          {navLinks.map((link) => (
            <Link
              key={link.href}
              href={link.href}
              className={`text-sm transition-colors ${
                pathname === link.href
                  ? "text-accent-bright"
                  : "text-text-muted hover:text-text-primary"
              }`}
            >
              {link.label}
            </Link>
          ))}
        </div>

        <div className="flex items-center gap-4">
          {isAuthenticated() ? (
            <>
              <button
                onClick={openNotifications}
                className="relative p-2 text-text-muted hover:text-text-primary transition-colors"
                aria-label="Notifications"
              >
                <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M14.857 17.082a23.848 23.848 0 005.454-1.31A8.967 8.967 0 0118 9.75v-.7V9A6 6 0 006 9v.75a8.967 8.967 0 01-2.312 6.022c1.733.64 3.56 1.085 5.455 1.31m5.714 0a24.255 24.255 0 01-5.714 0m5.714 0a3 3 0 11-5.714 0" />
                </svg>
              </button>
              <NavbarUserMenu username={user?.username || ""} role={user?.role || ""} />
            </>
          ) : (
            <Link
              href="/login"
              className="text-sm text-accent hover:text-accent-bright transition-colors"
            >
              Sign In
            </Link>
          )}
        </div>
      </div>
    </nav>
  );
}
