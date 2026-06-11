"use client";

import { useState } from "react";
import { Button, Input } from "@/components/ui";
import { PosterUpload } from "@/components/admin/poster-upload";
import { useGenres } from "@/lib/hooks/use-genres";
import type { CreateMovieRequest } from "@/lib/types";

export interface MovieFormValues {
  title: string;
  overview: string;
  releaseDate: string;
  runtimeMinutes: number;
  posterUrl: string | null;
  genreIds: number[];
}

interface MovieFormProps {
  initialValues?: MovieFormValues;
  onSubmit: (data: CreateMovieRequest) => void;
  isPending: boolean;
  submitLabel: string;
  error?: string;
}

export function MovieForm({
  initialValues,
  onSubmit,
  isPending,
  submitLabel,
  error,
}: MovieFormProps) {
  const { data: genres } = useGenres();
  const [title, setTitle] = useState(initialValues?.title ?? "");
  const [overview, setOverview] = useState(initialValues?.overview ?? "");
  const [releaseDate, setReleaseDate] = useState(initialValues?.releaseDate ?? "");
  const [runtime, setRuntime] = useState(
    initialValues?.runtimeMinutes ? String(initialValues.runtimeMinutes) : ""
  );
  const [posterUrl, setPosterUrl] = useState<string | null>(initialValues?.posterUrl ?? null);
  const [selectedGenreIds, setSelectedGenreIds] = useState<number[]>(
    initialValues?.genreIds ?? []
  );
  const [localError, setLocalError] = useState("");

  const toggleGenre = (genreId: number) => {
    setSelectedGenreIds((current) =>
      current.includes(genreId)
        ? current.filter((id) => id !== genreId)
        : [...current, genreId]
    );
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setLocalError("");

    if (selectedGenreIds.length === 0) {
      setLocalError("Select at least one genre");
      return;
    }

    onSubmit({
      title,
      overview,
      posterUrl: posterUrl || undefined,
      releaseDate,
      runtimeMinutes: Number(runtime),
      genreIds: selectedGenreIds,
    });
  };

  const displayError = localError || error;

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <Input label="Title" value={title} onChange={(e) => setTitle(e.target.value)} required />
      <Input
        label="Release Date"
        type="date"
        value={releaseDate}
        onChange={(e) => setReleaseDate(e.target.value)}
        required
      />
      <Input
        label="Runtime (min)"
        type="number"
        value={runtime}
        onChange={(e) => setRuntime(e.target.value)}
        required
        min={1}
      />
      <PosterUpload value={posterUrl} onChange={setPosterUrl} />

      <div>
        <p className="text-[11px] uppercase tracking-wider text-text-muted mb-2">Genres</p>
        <div className="flex flex-wrap gap-2">
          {genres?.map((genre) => {
            const selected = selectedGenreIds.includes(genre.id);
            return (
              <button
                key={genre.id}
                type="button"
                onClick={() => toggleGenre(genre.id)}
                className={`px-3 py-1.5 rounded-full text-xs border transition-colors ${
                  selected
                    ? "border-accent-bright text-accent-bright bg-accent/10"
                    : "border-glass-border text-text-muted hover:border-border-accent"
                }`}
              >
                {genre.name}
              </button>
            );
          })}
        </div>
      </div>

      <textarea
        placeholder="Overview..."
        value={overview}
        onChange={(e) => setOverview(e.target.value)}
        rows={3}
        className="w-full px-4 py-3 bg-[rgba(255,255,255,0.02)] border border-glass-border rounded-lg text-text-secondary text-sm placeholder:text-text-dim focus:outline-none focus:border-border-accent resize-none"
        required
      />

      {displayError && <p className="text-xs text-red-400 text-center">{displayError}</p>}

      <Button type="submit" disabled={isPending}>
        {isPending ? "Saving..." : submitLabel}
      </Button>
    </form>
  );
}
