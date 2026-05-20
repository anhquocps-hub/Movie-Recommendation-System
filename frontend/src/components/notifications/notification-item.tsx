"use client";

import type { NotificationResponse } from "@/lib/types";

interface NotificationItemProps {
  notification: NotificationResponse;
  onClick?: () => void;
}

export function NotificationItem({ notification, onClick }: NotificationItemProps) {
  return (
    <button
      onClick={onClick}
      className={`w-full text-left px-4 py-3 flex items-start gap-3 transition-colors hover:bg-glass-bg ${
        !notification.read ? "bg-accent/5" : ""
      }`}
    >
      <div className={`mt-1.5 w-2 h-2 rounded-full flex-shrink-0 ${
        notification.read ? "border border-text-dim" : "bg-accent-bright"
      }`} />
      <div className="flex-1 min-w-0">
        <p className="text-sm text-text-secondary leading-snug">{notification.message}</p>
        <p className="text-[10px] text-text-dim mt-1">{getTimeAgo(notification.createdAt)}</p>
      </div>
    </button>
  );
}

function getTimeAgo(dateStr: string): string {
  const diff = Date.now() - new Date(dateStr).getTime();
  const minutes = Math.floor(diff / 60000);
  if (minutes < 60) return `${minutes}m ago`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours}h ago`;
  const days = Math.floor(hours / 24);
  return `${days}d ago`;
}
