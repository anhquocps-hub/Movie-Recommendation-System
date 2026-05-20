import { apiClient } from "./client";
import type { ApiResponse, PagedResponse, WatchlistResponse } from "@/lib/types";

export async function getWatchlist(page = 0, size = 20) {
  const res = await apiClient.get<ApiResponse<PagedResponse<WatchlistResponse>>>("/watchlist", {
    params: { page, size },
  });
  return res.data.data;
}

export async function addToWatchlist(movieId: number) {
  const res = await apiClient.post<ApiResponse<WatchlistResponse>>(`/watchlist/${movieId}`);
  return res.data.data;
}

export async function removeFromWatchlist(movieId: number) {
  await apiClient.delete(`/watchlist/${movieId}`);
}
