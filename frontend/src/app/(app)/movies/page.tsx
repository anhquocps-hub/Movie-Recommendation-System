"use client";

import { useState, useCallback } from "react";
import { useMovies, useSearchMovies } from "@/lib/hooks/use-movies";
import { MovieGrid, FilterBar } from "@/components/movies";
import { Button } from "@/components/ui";

export default function BrowsePage() {
  const [page, setPage] = useState(0);
  const [filters, setFilters] = useState<Record<string, any>>({});
  const [searchQuery, setSearchQuery] = useState("");

  const { data: moviesData, isLoading: moviesLoading } = useMovies({
    page,
    size: 20,
    ...filters,
  });

  const { data: searchData, isLoading: searchLoading } = useSearchMovies(
    searchQuery,
    filters.genreId,
    page
  );

  const isSearching = searchQuery.length >= 2;
  const data = isSearching ? searchData : moviesData;
  const isLoading = isSearching ? searchLoading : moviesLoading;

  const handleFiltersChange = useCallback((newFilters: Record<string, any>) => {
    if (newFilters.query !== undefined) {
      setSearchQuery(newFilters.query || "");
      delete newFilters.query;
    }
    setFilters((prev) => ({ ...prev, ...newFilters }));
    setPage(0);
  }, []);

  return (
    <div>
      <h1 className="font-[family-name:var(--font-playfair)] text-3xl text-text-primary mb-6">
        Browse Movies
      </h1>

      <FilterBar onFiltersChange={handleFiltersChange} />

      <MovieGrid movies={data?.content} isLoading={isLoading} />

      {data && data.totalPages > 1 && (
        <div className="flex items-center justify-center gap-4 mt-8">
          <Button
            variant="secondary"
            size="sm"
            disabled={page === 0}
            onClick={() => setPage((p) => p - 1)}
          >
            Previous
          </Button>
          <span className="text-sm text-text-muted">
            Page {page + 1} of {data.totalPages}
          </span>
          <Button
            variant="secondary"
            size="sm"
            disabled={data.last}
            onClick={() => setPage((p) => p + 1)}
          >
            Next
          </Button>
        </div>
      )}
    </div>
  );
}
