import { apiClient } from "./client";
import type {
  ApiResponse,
  GenreResponse,
  CreateGenreRequest,
  UpdateGenreRequest,
} from "@/lib/types";

export async function getGenres() {
  const res = await apiClient.get<ApiResponse<GenreResponse[]>>("/genres");
  return res.data.data;
}

export async function createGenre(data: CreateGenreRequest) {
  const res = await apiClient.post<ApiResponse<GenreResponse>>("/genres", data);
  return res.data.data;
}

export async function updateGenre(id: number, data: UpdateGenreRequest) {
  const res = await apiClient.put<ApiResponse<GenreResponse>>(`/genres/${id}`, data);
  return res.data.data;
}

export async function deleteGenre(id: number) {
  await apiClient.delete(`/genres/${id}`);
}
