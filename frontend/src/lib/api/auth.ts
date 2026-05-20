import { apiClient } from "./client";
import type {
  ApiResponse,
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  ForgotPasswordRequest,
  ResetPasswordRequest,
} from "@/lib/types";

export async function login(data: LoginRequest) {
  const res = await apiClient.post<ApiResponse<AuthResponse>>("/auth/login", data);
  return res.data.data;
}

export async function register(data: RegisterRequest) {
  const res = await apiClient.post<ApiResponse<AuthResponse>>("/auth/register", data);
  return res.data.data;
}

export async function refreshToken() {
  const res = await apiClient.post<ApiResponse<AuthResponse>>("/auth/refresh");
  return res.data.data;
}

export async function logout() {
  await apiClient.post("/auth/logout");
}

export async function forgotPassword(data: ForgotPasswordRequest) {
  await apiClient.post("/auth/forgot-password", data);
}

export async function resetPassword(data: ResetPasswordRequest) {
  await apiClient.post("/auth/reset-password", data);
}
