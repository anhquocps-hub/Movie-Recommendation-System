"use client";

import { useState } from "react";
import Link from "next/link";
import { useRecommendations } from "@/lib/hooks/use-recommendations";
import { MovieCard } from "@/components/movies";
import { Button, Skeleton } from "@/components/ui";

export default function RecommendationsPage() {
  const [page, setPage] = useState(0);
  const { data, isLoading } = useRecommendations(page);

  if (isLoading) {
    return (
      <div>
        <h1 className="font-[family-name:var(--font-playfair)] text-3xl text-text-primary mb-6">For You</h1>
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
      <h1 className="font-[family-name:var(--font-playfair)] text-3xl text-text-primary mb-2">For You</h1>
      <p className="text-sm text-text-muted mb-6">Personalized picks based on your taste</p>

      {!data?.content.length ? (
        <div className="text-center py-20">
          <p className="text-text-muted text-lg mb-2">No recommendations yet</p>
          <p className="text-text-dim text-sm mb-6">Rate some movies to get personalized picks</p>
          <Link href="/movies">
            <Button variant="primary">Browse Movies</Button>
          </Link>
        </div>
      ) : (
        <>
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
            {data.content.map((rec) => (
              <MovieCard
                key={rec.id}
                movie={{
                  id: rec.movie.id,
                  title: rec.movie.title,
                  slug: rec.movie.slug,
                  posterUrl: rec.movie.posterUrl,
                  releaseDate: rec.movie.releaseDate,
                  avgRating: rec.movie.avgRating,
                  voteCount: rec.movie.voteCount,
                  genres: rec.movie.genres,
                }}
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
