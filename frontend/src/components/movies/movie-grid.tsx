"use client";

import { MovieCard } from "./movie-card";
import { Skeleton } from "@/components/ui";
import type { MovieResponse } from "@/lib/types";

interface MovieGridProps {
  movies?: MovieResponse[];
  isLoading?: boolean;
}

export function MovieGrid({ movies, isLoading }: MovieGridProps) {
  if (isLoading) {
    return (
      <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
        {Array.from({ length: 20 }).map((_, i) => (
          <Skeleton key={i} className="aspect-[2/3] rounded-lg" />
        ))}
      </div>
    );
  }

  if (!movies?.length) {
    return (
      <div className="text-center py-20">
        <p className="text-text-muted text-lg">No movies found</p>
        <p className="text-text-dim text-sm mt-2">Try adjusting your filters</p>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
      {movies.map((movie) => (
        <MovieCard key={movie.id} movie={movie} />
      ))}
    </div>
  );
}
