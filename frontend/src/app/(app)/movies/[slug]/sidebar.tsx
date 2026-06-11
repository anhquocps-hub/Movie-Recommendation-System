"use client";

import Image from "next/image";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { Button } from "@/components/ui";
import { useAuthStore } from "@/stores/auth.store";
import { useUIStore } from "@/stores/ui.store";
import * as watchlistApi from "@/lib/api/watchlist";
import type { MovieDetailResponse } from "@/lib/types";

interface MovieDetailSidebarProps {
  movie: MovieDetailResponse;
}

export function MovieDetailSidebar({ movie }: MovieDetailSidebarProps) {
  const { isAuthenticated } = useAuthStore();
  const { addToast } = useUIStore();
  const queryClient = useQueryClient();

  const addToWatchlist = useMutation({
    mutationFn: () => watchlistApi.addToWatchlist(movie.id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["watchlist"] });
      addToast({
        message: `"${movie.title}" added to your watchlist`,
        type: "success",
      });
    },
  });

  return (
    <aside className="lg:sticky lg:top-24 h-fit space-y-6">
      {movie.posterUrl && (
        <div className="relative aspect-[2/3] rounded-lg overflow-hidden border border-border">
          <Image src={movie.posterUrl} alt={movie.title} fill className="object-cover" />
        </div>
      )}

      {isAuthenticated() && (
        <div className="space-y-3">
          <Button
            size="lg"
            className="w-full"
            onClick={() => addToWatchlist.mutate()}
            disabled={addToWatchlist.isPending}
          >
            {addToWatchlist.isPending ? "Adding..." : "Add to Watchlist"}
          </Button>
        </div>
      )}

      <div className="p-4 bg-bg-surface border border-border rounded-lg space-y-3">
        <div>
          <p className="text-[10px] uppercase tracking-wider text-text-dim">Release Date</p>
          <p className="text-sm text-text-secondary">{new Date(movie.releaseDate).toLocaleDateString()}</p>
        </div>
        <div>
          <p className="text-[10px] uppercase tracking-wider text-text-dim">Runtime</p>
          <p className="text-sm text-text-secondary">{movie.runtimeMinutes} minutes</p>
        </div>
        <div>
          <p className="text-[10px] uppercase tracking-wider text-text-dim">Rating</p>
          <p className="text-sm text-accent-bright">{movie.avgRating ? `★ ${movie.avgRating.toFixed(1)} (${movie.voteCount} votes)` : "No ratings yet"}</p>
        </div>
      </div>
    </aside>
  );
}
