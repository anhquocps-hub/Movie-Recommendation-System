"use client";

import { useCallback, useState, useEffect } from "react";
import { useGenres } from "@/lib/hooks/use-genres";

interface FilterBarProps {
  onFiltersChange: (filters: {
    query?: string;
    genreId?: number;
    year?: number;
    minRating?: number;
    sortBy?: string;
    sortDir?: string;
  }) => void;
  initialFilters?: Record<string, string>;
}

export function FilterBar({ onFiltersChange, initialFilters = {} }: FilterBarProps) {
  const { data: genres } = useGenres();
  const [query, setQuery] = useState(initialFilters.query || "");

  useEffect(() => {
    const timer = setTimeout(() => {
      onFiltersChange({ query: query || undefined });
    }, 300);
    return () => clearTimeout(timer);
  }, [query]);

  const handleGenreChange = useCallback((e: React.ChangeEvent<HTMLSelectElement>) => {
    const val = e.target.value;
    onFiltersChange({ genreId: val ? Number(val) : undefined });
  }, [onFiltersChange]);

  const handleSortChange = useCallback((e: React.ChangeEvent<HTMLSelectElement>) => {
    const [sortBy, sortDir] = e.target.value.split(":");
    onFiltersChange({ sortBy, sortDir });
  }, [onFiltersChange]);

  const selectClass = "bg-bg-elevated border border-glass-border rounded-lg px-3 py-2 text-sm text-text-secondary focus:outline-none focus:border-border-accent";

  return (
    <div className="flex flex-wrap items-center gap-3 mb-6">
      <input
        type="text"
        placeholder="Search movies..."
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        className="flex-1 min-w-[200px] bg-bg-elevated border border-glass-border rounded-lg px-4 py-2 text-sm text-text-secondary placeholder:text-text-dim focus:outline-none focus:border-border-accent"
      />

      <select onChange={handleGenreChange} className={selectClass} defaultValue="">
        <option value="">All Genres</option>
        {genres?.map((g) => (
          <option key={g.id} value={g.id}>{g.name}</option>
        ))}
      </select>

      <select onChange={handleSortChange} className={selectClass} defaultValue="avgRating:desc">
        <option value="avgRating:desc">Top Rated</option>
        <option value="releaseDate:desc">Newest</option>
        <option value="releaseDate:asc">Oldest</option>
        <option value="title:asc">A-Z</option>
        <option value="voteCount:desc">Most Reviewed</option>
      </select>
    </div>
  );
}
