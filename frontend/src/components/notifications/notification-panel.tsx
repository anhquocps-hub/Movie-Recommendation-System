"use client";

import { useEffect } from "react";
import { m, AnimatePresence } from "framer-motion";
import { useUIStore } from "@/stores/ui.store";
import { useNotifications, useMarkAllAsRead, useMarkAsRead } from "@/lib/hooks/use-notifications";
import { NotificationItem } from "./notification-item";

export function NotificationPanel() {
  const { isNotificationPanelOpen, closeNotifications } = useUIStore();
  const { data } = useNotifications();
  const markAllAsRead = useMarkAllAsRead();
  const markAsRead = useMarkAsRead();

  useEffect(() => {
    if (!isNotificationPanelOpen) return;
    const handleEscape = (e: KeyboardEvent) => {
      if (e.key === "Escape") closeNotifications();
    };
    document.addEventListener("keydown", handleEscape);
    return () => document.removeEventListener("keydown", handleEscape);
  }, [isNotificationPanelOpen, closeNotifications]);

  return (
    <AnimatePresence>
      {isNotificationPanelOpen && (
        <>
          <m.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 bg-black/40 z-50"
            onClick={closeNotifications}
          />

          <m.div
            initial={{ x: "100%" }}
            animate={{ x: 0 }}
            exit={{ x: "100%" }}
            transition={{ type: "spring", damping: 25, stiffness: 200 }}
            className="fixed top-0 right-0 bottom-0 w-[380px] max-w-full bg-bg-elevated border-l border-glass-border z-50 flex flex-col"
          >
            <div className="flex items-center justify-between px-5 py-4 border-b border-border">
              <h2 className="font-[family-name:var(--font-playfair)] text-lg text-text-primary">Notifications</h2>
              <button
                onClick={() => markAllAsRead.mutate()}
                className="text-xs text-accent hover:text-accent-bright transition-colors"
              >
                Mark all read
              </button>
            </div>

            <div className="flex-1 overflow-y-auto">
              {!data?.content.length ? (
                <div className="text-center py-12">
                  <p className="text-text-muted text-sm">All caught up</p>
                  <p className="text-text-dim text-xs mt-1">No new notifications</p>
                </div>
              ) : (
                <div className="divide-y divide-border">
                  {data.content.map((n) => (
                    <NotificationItem
                      key={n.id}
                      notification={n}
                      onClick={() => {
                        if (!n.read) markAsRead.mutate(n.id);
                        closeNotifications();
                      }}
                    />
                  ))}
                </div>
              )}
            </div>
          </m.div>
        </>
      )}
    </AnimatePresence>
  );
}
