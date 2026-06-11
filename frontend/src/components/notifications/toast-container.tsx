"use client";

import { useEffect } from "react";
import { m, AnimatePresence } from "framer-motion";
import { useUIStore } from "@/stores/ui.store";

export function ToastContainer() {
  const { toasts, removeToast } = useUIStore();

  return (
    <div className="fixed bottom-6 right-6 z-[60] flex flex-col gap-2">
      <AnimatePresence>
        {toasts.map((toast) => (
          <ToastItem key={toast.id} toast={toast} onDismiss={() => removeToast(toast.id)} />
        ))}
      </AnimatePresence>
    </div>
  );
}

function ToastItem({ toast, onDismiss }: { toast: { id: string; message: string; type: string; duration?: number }; onDismiss: () => void }) {
  useEffect(() => {
    const timer = setTimeout(onDismiss, toast.duration || 4000);
    return () => clearTimeout(timer);
  }, [toast.duration, onDismiss]);

  const borderColor =
    toast.type === "error"
      ? "border-red-800"
      : toast.type === "success"
        ? "border-accent-bright"
        : "border-border-accent";

  return (
    <m.div
      initial={{ opacity: 0, y: 20, scale: 0.95 }}
      animate={{ opacity: 1, y: 0, scale: 1 }}
      exit={{ opacity: 0, y: 10, scale: 0.95 }}
      className={`px-4 py-3 bg-bg-elevated border ${borderColor} rounded-lg shadow-xl max-w-sm`}
    >
      <p className="text-sm text-text-secondary">{toast.message}</p>
    </m.div>
  );
}
