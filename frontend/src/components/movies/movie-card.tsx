"use client";

import Image from "next/image";
import Link from "next/link";
import type { MovieResponse } from "@/lib/types";

interface MovieCardProps {
  movie: MovieResponse;
  showAddedAt?: string;
  onRemove?: () => void;
}

export function MovieCard({ movie, showAddedAt, onRemove }: MovieCardProps) {
  const year = movie.releaseDate ? new Date(movie.releaseDate).getFullYear() : "";

  return (
    <Link href={`/movies/${movie.id}`} className="group block">
      <div className="relative aspect-[2/3] rounded-lg overflow-hidden bg-bg-surface border border-border transition-all duration-300 group-hover:-translate-y-1 group-hover:shadow-[0_12px_40px_rgba(212,165,116,0.15),0_0_0_1px_rgba(212,165,116,0.2)]">
        {movie.posterUrl ? (
          <Image
            src={movie.posterUrl}
            alt={movie.title}
            fill
            className="object-cover"
            sizes="(max-width: 640px) 50vw, (max-width: 1024px) 33vw, 20vw"
          />
        ) : (
          <div className="absolute inset-0 flex items-center justify-center bg-bg-elevated">
            <span className="text-text-dim text-sm">No Poster</span>
          </div>
        )}

        <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-transparent to-transparent" />

        {onRemove && (
          <button
            onClick={(e) => { e.preventDefault(); onRemove(); }}
            className="absolute top-2 right-2 w-7 h-7 rounded-full bg-black/60 flex items-center justify-center text-text-muted hover:text-red-400 transition-colors z-10"
            aria-label="Remove"
          >
            ✕
          </button>
        )}

        <div className="absolute bottom-0 left-0 right-0 p-3">
          <h3 className="text-sm font-medium text-text-primary truncate">{movie.title}</h3>
          <div className="flex items-center gap-2 mt-1">
            {movie.avgRating && (
              <span className="text-xs text-accent-bright font-medium">{movie.avgRating.toFixed(1)}</span>
            )}
            {movie.avgRating && year && <span className="text-text-dim text-[10px]">·</span>}
            {year && <span className="text-xs text-text-muted">{year}</span>}
          </div>
          {showAddedAt && (
            <p className="text-[10px] text-text-dim mt-1">Added {showAddedAt}</p>
          )}
        </div>
      </div>
    </Link>
  );
}
