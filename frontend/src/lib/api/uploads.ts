import { apiClient } from "./client";
import type { ApiResponse } from "@/lib/types";

interface UploadResponse {
  url: string;
}

export async function uploadPoster(file: File) {
  const formData = new FormData();
  formData.append("file", file);

  const res = await apiClient.post<ApiResponse<UploadResponse>>(
    "/admin/uploads/poster",
    formData
  );
  return res.data.data.url;
}
