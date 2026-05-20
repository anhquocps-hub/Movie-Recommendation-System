"use client";

import { useState } from "react";
import Link from "next/link";
import { useWatchlist, useRemoveFromWatchlist } from "@/lib/hooks/use-watchlist";
import { MovieCard } from "@/components/movies";
import { Button, Skeleton } from "@/components/ui";

export default function WatchlistPage() {
  const [page, setPage] = useState(0);
  const { data, isLoading } = useWatchlist(page);
  const removeFromWatchlist = useRemoveFromWatchlist();

  if (isLoading) {
    return (
      <div>
        <h1 className="font-[family-name:var(--font-playfair)] text-3xl text-text-primary mb-6">My Watchlist</h1>
        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
          {Array.from({ length: 10 }).map((_, i) => (
            <Skeleton key={i} className="aspect-[2/3] rounded-lg" />
          ))}
        </div>
      </div>
    );
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="font-[family-name:var(--font-playfair)] text-3xl text-text-primary">My Watchlist</h1>
          <p className="text-sm text-text-muted mt-1">{data?.totalElements || 0} movies saved</p>
        </div>
      </div>

      {!data?.content.length ? (
        <div className="text-center py-20">
          <p className="text-text-muted text-lg mb-2">Your watchlist is empty</p>
          <p className="text-text-dim text-sm mb-6">Start building your collection</p>
          <Link href="/movies">
            <Button variant="primary">Browse Movies</Button>
          </Link>
        </div>
      ) : (
        <>
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
            {data.content.map((item) => (
              <MovieCard
                key={item.id}
                movie={{
                  id: item.movieId,
                  title: item.movieTitle,
                  slug: item.movieSlug,
                  posterUrl: item.posterUrl,
                  releaseDate: "",
                  avgRating: item.avgRating,
                  voteCount: 0,
                  genres: [],
                }}
                showAddedAt={getTimeAgo(item.addedAt)}
                onRemove={() => removeFromWatchlist.mutate(item.movieId)}
              />
            ))}
          </div>

          {data.totalPages > 1 && (
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
        </>
      )}
    </div>
  );
}

function getTimeAgo(dateStr: string): string {
  const diff = Date.now() - new Date(dateStr).getTime();
  const hours = Math.floor(diff / 3600000);
  if (hours < 24) return `${hours}h ago`;
  const days = Math.floor(hours / 24);
  return `${days}d ago`;
}
