"use client";

import { use, useState } from "react";
import Image from "next/image";
import { useMovie } from "@/lib/hooks/use-movies";
import { useMovieReviews } from "@/lib/hooks/use-reviews";
import { useAuthStore } from "@/stores/auth.store";
import { ReviewCard, ReviewForm } from "@/components/reviews";
import { Button, Skeleton } from "@/components/ui";
import { MovieDetailSidebar } from "./sidebar";

export default function MovieDetailPage({ params }: { params: Promise<{ slug: string }> }) {
  const { slug } = use(params);
  const movieId = Number(slug);
  const { data: movie, isLoading } = useMovie(movieId);
  const [reviewPage, setReviewPage] = useState(0);
  const { data: reviewsData } = useMovieReviews(movieId, reviewPage);
  const { isAuthenticated } = useAuthStore();

  if (isLoading) {
    return (
      <div>
        <Skeleton className="w-full h-[50vh] rounded-xl" />
        <div className="mt-8 grid grid-cols-1 lg:grid-cols-[1fr_300px] gap-8">
          <Skeleton className="h-96 rounded-lg" />
          <Skeleton className="h-64 rounded-lg" />
        </div>
      </div>
    );
  }

  if (!movie) {
    return (
      <div className="text-center py-20">
        <h1 className="text-2xl text-text-primary font-[family-name:var(--font-playfair)]">Movie not found</h1>
        <p className="text-text-muted mt-2">The movie you&apos;re looking for doesn&apos;t exist.</p>
      </div>
    );
  }

  return (
    <div className="-mx-6 -mt-8">
      <div className="relative h-[50vh] overflow-hidden">
        {movie.backdropUrl ? (
          <Image src={movie.backdropUrl} alt={movie.title} fill className="object-cover" priority />
        ) : (
          <div className="absolute inset-0 bg-bg-elevated" />
        )}
        <div className="absolute inset-0 bg-gradient-to-t from-bg-primary via-bg-primary/50 to-transparent" />

        <div className="absolute bottom-0 left-0 right-0 p-8 max-w-7xl mx-auto">
          <div className="flex flex-wrap gap-2 mb-3">
            {movie.genres.map((g) => (
              <span key={g.id} className="px-2.5 py-1 text-[10px] uppercase tracking-wider text-accent border border-border-accent rounded-full">
                {g.name}
              </span>
            ))}
          </div>
          <h1 className="font-[family-name:var(--font-playfair)] text-4xl lg:text-5xl text-text-primary mb-3">
            {movie.title}
          </h1>
          <div className="flex items-center gap-4 text-sm text-text-muted">
            {movie.avgRating && <span className="text-accent-bright font-medium">★ {movie.avgRating.toFixed(1)}</span>}
            <span>{new Date(movie.releaseDate).getFullYear()}</span>
            <span>{movie.runtimeMinutes} min</span>
            <span>{movie.voteCount} reviews</span>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-6 py-8 grid grid-cols-1 lg:grid-cols-[1fr_300px] gap-8">
        <div className="space-y-8">
          <section>
            <h2 className="font-[family-name:var(--font-playfair)] text-xl text-text-primary mb-3">Overview</h2>
            <p className="text-sm text-text-secondary leading-relaxed">{movie.overview}</p>
          </section>

          {isAuthenticated() && (
            <ReviewForm movieId={movie.id} />
          )}

          <section>
            <h2 className="font-[family-name:var(--font-playfair)] text-xl text-text-primary mb-4">
              Reviews ({reviewsData?.totalElements || 0})
            </h2>
            <div className="space-y-4">
              {reviewsData?.content.map((review) => (
                <ReviewCard key={review.id} review={review} movieId={movie.id} />
              ))}
            </div>
            {reviewsData && reviewsData.totalPages > 1 && (
              <div className="flex items-center justify-center gap-4 mt-6">
                <Button variant="secondary" size="sm" disabled={reviewPage === 0} onClick={() => setReviewPage((p) => p - 1)}>
                  Previous
                </Button>
                <span className="text-xs text-text-muted">Page {reviewPage + 1} of {reviewsData.totalPages}</span>
                <Button variant="secondary" size="sm" disabled={reviewsData.last} onClick={() => setReviewPage((p) => p + 1)}>
                  Next
                </Button>
              </div>
            )}
          </section>
        </div>

        <MovieDetailSidebar movie={movie} />
      </div>
    </div>
  );
}
