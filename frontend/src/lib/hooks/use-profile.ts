import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import * as usersApi from "@/lib/api/users";
import type { UpdateProfileRequest, UpdatePreferencesRequest } from "@/lib/types";

export function useProfile() {
  return useQuery({
    queryKey: ["user", "me"],
    queryFn: usersApi.getCurrentProfile,
    staleTime: 10 * 60 * 1000,
  });
}

export function useUpdateProfile() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: UpdateProfileRequest) => usersApi.updateProfile(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["user", "me"] });
    },
  });
}

export function useUpdatePreferences() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: UpdatePreferencesRequest) => usersApi.updatePreferences(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["user", "me"] });
    },
  });
}
