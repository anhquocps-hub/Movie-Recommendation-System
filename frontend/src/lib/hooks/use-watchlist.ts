import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import * as watchlistApi from "@/lib/api/watchlist";

export function useWatchlist(page = 0) {
  return useQuery({
    queryKey: ["watchlist", page],
    queryFn: () => watchlistApi.getWatchlist(page),
    staleTime: 5 * 60 * 1000,
  });
}

export function useAddToWatchlist() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (movieId: number) => watchlistApi.addToWatchlist(movieId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["watchlist"] });
    },
  });
}

export function useRemoveFromWatchlist() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (movieId: number) => watchlistApi.removeFromWatchlist(movieId),
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: ["watchlist"] });
    },
  });
}
