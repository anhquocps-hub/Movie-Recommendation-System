"use client";

import { useState } from "react";
import { useTrendingMovies } from "@/lib/hooks/use-movies";
import { MovieGrid } from "@/components/movies";
import { Button } from "@/components/ui";

export default function TrendingPage() {
  const [page, setPage] = useState(0);
  const { data, isLoading } = useTrendingMovies(page);

  return (
    <div>
      <h1 className="font-[family-name:var(--font-playfair)] text-3xl text-text-primary mb-2">
        Trending Now
      </h1>
      <p className="text-text-muted text-sm mb-6">Most popular movies this week</p>

      <MovieGrid movies={data?.content} isLoading={isLoading} />

      {data && data.totalPages > 1 && (
        <div className="flex items-center justify-center gap-4 mt-8">
          <Button variant="secondary" size="sm" disabled={page === 0} onClick={() => setPage((p) => p - 1)}>
            Previous
          </Button>
          <span className="text-sm text-text-muted">Page {page + 1} of {data.totalPages}</span>
          <Button variant="secondary" size="sm" disabled={data.last} onClick={() => setPage((p) => p + 1)}>
            Next
          </Button>
        </div>
      )}
    </div>
  );
}
