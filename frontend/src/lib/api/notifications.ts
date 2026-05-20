import { apiClient } from "./client";
import type { ApiResponse, PagedResponse, NotificationResponse } from "@/lib/types";

export async function getNotifications(page = 0, size = 20) {
  const res = await apiClient.get<ApiResponse<PagedResponse<NotificationResponse>>>(
    "/notifications",
    { params: { page, size } }
  );
  return res.data.data;
}

export async function getUnreadCount() {
  const res = await apiClient.get<ApiResponse<number>>("/notifications/unread-count");
  return res.data.data;
}

export async function markAsRead(id: number) {
  await apiClient.patch(`/notifications/${id}/read`);
}

export async function markAllAsRead() {
  await apiClient.patch("/notifications/read-all");
}
