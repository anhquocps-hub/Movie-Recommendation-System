import { create } from "zustand";

export interface Toast {
  id: string;
  message: string;
  type: "success" | "error" | "info";
  duration?: number;
}

interface UIState {
  isNotificationPanelOpen: boolean;
  toasts: Toast[];
  openNotifications: () => void;
  closeNotifications: () => void;
  addToast: (toast: Omit<Toast, "id">) => void;
  removeToast: (id: string) => void;
}

export const useUIStore = create<UIState>((set) => ({
  isNotificationPanelOpen: false,
  toasts: [],
  openNotifications: () => set({ isNotificationPanelOpen: true }),
  closeNotifications: () => set({ isNotificationPanelOpen: false }),
  addToast: (toast) =>
    set((state) => ({
      toasts: [
        ...state.toasts.slice(-2),
        { ...toast, id: crypto.randomUUID() },
      ],
    })),
  removeToast: (id) =>
    set((state) => ({
      toasts: state.toasts.filter((t) => t.id !== id),
    })),
}));
