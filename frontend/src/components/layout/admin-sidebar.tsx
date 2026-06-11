"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

const adminLinks = [
  { href: "/admin/movies", label: "Movies", icon: "🎬" },
  { href: "/admin/genres", label: "Genres", icon: "🏷" },
  { href: "/admin/users", label: "Users", icon: "👤" },
  { href: "/admin/reviews", label: "Reviews", icon: "💬" },
];

export function AdminSidebar() {
  const pathname = usePathname();

  return (
    <aside className="w-56 flex-shrink-0 border-r border-border min-h-[calc(100vh-64px)] p-4">
      <p className="text-[10px] uppercase tracking-wider text-text-dim mb-4 px-3">Admin Panel</p>
      <nav className="space-y-1">
        {adminLinks.map((link) => (
          <Link
            key={link.href}
            href={link.href}
            className={`flex items-center gap-3 px-3 py-2 rounded-lg text-sm transition-colors ${
              pathname === link.href
                ? "bg-accent/10 text-accent border border-border-accent"
                : "text-text-muted hover:text-text-primary hover:bg-glass-bg"
            }`}
          >
            <span>{link.icon}</span>
            <span>{link.label}</span>
          </Link>
        ))}
      </nav>
      <div className="mt-8 px-3">
        <Link href="/movies" className="text-xs text-text-dim hover:text-text-muted transition-colors">
          ← Back to app
        </Link>
      </div>
    </aside>
  );
}
