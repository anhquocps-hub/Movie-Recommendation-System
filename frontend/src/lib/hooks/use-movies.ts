import { useQuery } from "@tanstack/react-query";
import * as moviesApi from "@/lib/api/movies";

interface UseMoviesOptions {
  page?: number;
  size?: number;
  sortBy?: string;
  sortDir?: string;
  genreId?: number;
  year?: number;
  minRating?: number;
}

export function useMovies(options: UseMoviesOptions = {}) {
  return useQuery({
    queryKey: ["movies", options],
    queryFn: () => moviesApi.getMovies(options),
    staleTime: 5 * 60 * 1000,
  });
}

export function useMovie(id: number) {
  return useQuery({
    queryKey: ["movie", id],
    queryFn: () => moviesApi.getMovieById(id),
    staleTime: 10 * 60 * 1000,
    enabled: id > 0,
  });
}

export function useSearchMovies(query: string, genreId?: number, page = 0) {
  return useQuery({
    queryKey: ["movies", "search", query, genreId, page],
    queryFn: () => moviesApi.searchMovies(query, genreId, page),
    staleTime: 2 * 60 * 1000,
    enabled: query.length >= 2,
  });
}

export function useTrendingMovies(page = 0) {
  return useQuery({
    queryKey: ["movies", "trending", page],
    queryFn: () => moviesApi.getTrendingMovies(page),
    staleTime: 15 * 60 * 1000,
  });
}
