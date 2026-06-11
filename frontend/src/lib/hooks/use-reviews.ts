import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import * as reviewsApi from "@/lib/api/reviews";
import type { CreateReviewRequest, CreateReplyRequest } from "@/lib/types";

export function useMovieReviews(movieId: number, page = 0) {
  return useQuery({
    queryKey: ["reviews", movieId, page],
    queryFn: () => reviewsApi.getMovieReviews(movieId, page),
    staleTime: 2 * 60 * 1000,
    enabled: movieId > 0,
  });
}

export function useReviewReplies(reviewId: number, enabled = true) {
  return useQuery({
    queryKey: ["replies", reviewId],
    queryFn: () => reviewsApi.getReplies(reviewId),
    staleTime: 60 * 1000,
    enabled: reviewId > 0 && enabled,
  });
}

export function useCreateReview(movieId: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateReviewRequest) => reviewsApi.createReview(movieId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["reviews", movieId] });
      queryClient.invalidateQueries({ queryKey: ["movie"] });
    },
  });
}

export function useCreateReply(reviewId: number, movieId: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateReplyRequest) => reviewsApi.createReply(reviewId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["replies", reviewId] });
      queryClient.invalidateQueries({ queryKey: ["reviews", movieId] });
    },
  });
}

export function useToggleLike(movieId: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (reviewId: number) => reviewsApi.toggleLike(reviewId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["reviews", movieId] });
    },
  });
}

export function useAdminMovieReviews(movieId: number, enabled = false) {
  return useQuery({
    queryKey: ["admin-reviews", movieId],
    queryFn: () => reviewsApi.getAdminMovieReviews(movieId),
    enabled: movieId > 0 && enabled,
  });
}
