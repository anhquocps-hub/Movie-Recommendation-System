import { useQuery } from "@tanstack/react-query";
import * as genresApi from "@/lib/api/genres";

export function useGenres() {
  return useQuery({
    queryKey: ["genres"],
    queryFn: genresApi.getGenres,
    staleTime: 60 * 60 * 1000,
  });
}
