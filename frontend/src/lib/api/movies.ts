import { apiClient } from "./client";
import type {
  ApiResponse,
  PagedResponse,
  MovieResponse,
  MovieDetailResponse,
  CreateMovieRequest,
  UpdateMovieRequest,
} from "@/lib/types";

interface MovieFilters {
  page?: number;
  size?: number;
  sortBy?: string;
  sortDir?: string;
  genreId?: number;
  year?: number;
  minRating?: number;
}

export async function getMovies(filters: MovieFilters = {}) {
  const res = await apiClient.get<ApiResponse<PagedResponse<MovieResponse>>>("/movies", {
    params: filters,
  });
  return res.data.data;
}

export async function getMovieById(id: number) {
  const res = await apiClient.get<ApiResponse<MovieDetailResponse>>(`/movies/${id}`);
  return res.data.data;
}

export async function searchMovies(query: string, genreId?: number, page = 0, size = 20) {
  const res = await apiClient.get<ApiResponse<PagedResponse<MovieResponse>>>("/movies/search", {
    params: { query, genreId, page, size },
  });
  return res.data.data;
}

export async function getTrendingMovies(page = 0, size = 20) {
  const res = await apiClient.get<ApiResponse<PagedResponse<MovieResponse>>>("/movies/trending", {
    params: { page, size },
  });
  return res.data.data;
}

export async function createMovie(data: CreateMovieRequest) {
  const res = await apiClient.post<ApiResponse<MovieDetailResponse>>("/movies", data);
  return res.data.data;
}

export async function updateMovie(id: number, data: UpdateMovieRequest) {
  const res = await apiClient.put<ApiResponse<MovieDetailResponse>>(`/movies/${id}`, data);
  return res.data.data;
}

export async function deleteMovie(id: number) {
  await apiClient.delete(`/movies/${id}`);
}

export async function restoreMovie(id: number) {
  const res = await apiClient.patch<ApiResponse<MovieDetailResponse>>(`/movies/${id}/restore`);
  return res.data.data;
}
