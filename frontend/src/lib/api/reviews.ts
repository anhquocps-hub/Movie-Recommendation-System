import { apiClient } from "./client";
import type {
  ApiResponse,
  PagedResponse,
  ReviewResponse,
  ReplyResponse,
  CreateReviewRequest,
  UpdateReviewRequest,
  CreateReplyRequest,
} from "@/lib/types";

export async function getMovieReviews(movieId: number, page = 0, size = 20) {
  const res = await apiClient.get<ApiResponse<PagedResponse<ReviewResponse>>>(
    `/movies/${movieId}/reviews`,
    { params: { page, size } }
  );
  return res.data.data;
}

export async function createReview(movieId: number, data: CreateReviewRequest) {
  const res = await apiClient.post<ApiResponse<ReviewResponse>>(
    `/movies/${movieId}/reviews`,
    data
  );
  return res.data.data;
}

export async function updateReview(id: number, data: UpdateReviewRequest) {
  const res = await apiClient.put<ApiResponse<ReviewResponse>>(`/reviews/${id}`, data);
  return res.data.data;
}

export async function deleteReview(id: number) {
  await apiClient.delete(`/reviews/${id}`);
}

export async function toggleLike(id: number) {
  const res = await apiClient.post<ApiResponse<boolean>>(`/reviews/${id}/like`);
  return res.data.data;
}

export async function getReplies(reviewId: number, page = 0, size = 20) {
  const res = await apiClient.get<ApiResponse<PagedResponse<ReplyResponse>>>(
    `/reviews/${reviewId}/replies`,
    { params: { page, size } }
  );
  return res.data.data;
}

export async function createReply(reviewId: number, data: CreateReplyRequest) {
  const res = await apiClient.post<ApiResponse<ReplyResponse>>(
    `/reviews/${reviewId}/replies`,
    data
  );
  return res.data.data;
}
