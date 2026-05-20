import { useQuery } from "@tanstack/react-query";
import { apiClient } from "@/lib/api/client";
import type { ApiResponse, PagedResponse, RecommendationResponse } from "@/lib/types";

export function useRecommendations(page = 0) {
  return useQuery({
    queryKey: ["recommendations", page],
    queryFn: async () => {
      const res = await apiClient.get<ApiResponse<PagedResponse<RecommendationResponse>>>("/recommendations", {
        params: { page, size: 20 },
      });
      return res.data.data;
    },
    staleTime: 30 * 60 * 1000,
  });
}
