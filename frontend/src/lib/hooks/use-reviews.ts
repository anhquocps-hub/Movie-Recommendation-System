import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import * as reviewsApi from "@/lib/api/reviews";
import type { CreateReviewRequest } from "@/lib/types";

export function useMovieReviews(movieId: number, page = 0) {
  return useQuery({
    queryKey: ["reviews", movieId, page],
    queryFn: () => reviewsApi.getMovieReviews(movieId, page),
    staleTime: 2 * 60 * 1000,
    enabled: movieId > 0,
  });
}

export function useCreateReview(movieId: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateReviewRequest) => reviewsApi.createReview(movieId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["reviews", movieId] });
    },
  });
}

export function useToggleLike() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (reviewId: number) => reviewsApi.toggleLike(reviewId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["reviews"] });
    },
  });
}
