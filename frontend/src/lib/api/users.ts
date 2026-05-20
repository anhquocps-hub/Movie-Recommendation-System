import { apiClient } from "./client";
import type {
  ApiResponse,
  PagedResponse,
  UserProfileResponse,
  UpdateProfileRequest,
  UpdatePreferencesRequest,
} from "@/lib/types";

export async function getCurrentProfile() {
  const res = await apiClient.get<ApiResponse<UserProfileResponse>>("/users/me");
  return res.data.data;
}

export async function updateProfile(data: UpdateProfileRequest) {
  const res = await apiClient.put<ApiResponse<UserProfileResponse>>("/users/me", data);
  return res.data.data;
}

export async function updatePreferences(data: UpdatePreferencesRequest) {
  const res = await apiClient.put<ApiResponse<UserProfileResponse>>(
    "/users/me/preferences",
    data
  );
  return res.data.data;
}

export async function getAllUsers(page = 0, size = 20) {
  const res = await apiClient.get<ApiResponse<PagedResponse<UserProfileResponse>>>("/users", {
    params: { page, size },
  });
  return res.data.data;
}

export async function getUserById(id: number) {
  const res = await apiClient.get<ApiResponse<UserProfileResponse>>(`/users/${id}`);
  return res.data.data;
}

export async function changeUserRole(id: number, role: string) {
  const res = await apiClient.patch<ApiResponse<UserProfileResponse>>(`/users/${id}/role`, {
    role,
  });
  return res.data.data;
}

export async function deactivateUser(id: number) {
  await apiClient.delete(`/users/${id}`);
}
