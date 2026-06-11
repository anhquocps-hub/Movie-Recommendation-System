"use client";

import { useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useMovies } from "@/lib/hooks/use-movies";
import { useAdminMovieReviews } from "@/lib/hooks/use-reviews";
import { Button, Skeleton } from "@/components/ui";
import { ModerationMessage } from "@/components/reviews/moderation-message";
import { useUIStore } from "@/stores/ui.store";
import * as reviewsApi from "@/lib/api/reviews";
import type { AdminReplyResponse, AdminReviewResponse } from "@/lib/types";

export default function AdminReviewsPage() {
  const [page, setPage] = useState(0);
  const [expandedMovieId, setExpandedMovieId] = useState<number | null>(null);
  const { data, isLoading } = useMovies({ page, size: 15 });

  return (
    <div>
      <div className="mb-6">
        <h1 className="font-[family-name:var(--font-playfair)] text-2xl text-text-primary">Review Moderation</h1>
        <p className="text-sm text-text-muted mt-1">Select a movie to manage its reviews and replies</p>
      </div>

      <div className="space-y-3">
        {isLoading ? (
          <Skeleton className="h-24 rounded-lg" />
        ) : !data?.content.length ? (
          <p className="text-sm text-text-muted">No movies found</p>
        ) : (
          data.content.map((movie) => {
            const expanded = expandedMovieId === movie.id;
            return (
              <div key={movie.id} className="bg-bg-surface border border-border rounded-lg overflow-hidden">
                <button
                  type="button"
                  onClick={() => setExpandedMovieId(expanded ? null : movie.id)}
                  className="w-full flex items-center justify-between px-4 py-4 text-left hover:bg-glass-bg transition-colors"
                >
                  <div>
                    <p className="text-sm text-text-secondary">{movie.title}</p>
                    <p className="text-xs text-text-dim mt-1">
                      {movie.releaseDate ? new Date(movie.releaseDate).getFullYear() : "—"} · {movie.voteCount} reviews
                    </p>
                  </div>
                  <span className="text-xs text-accent">{expanded ? "Collapse" : "Manage"}</span>
                </button>

                {expanded && <MovieReviewPanel movieId={movie.id} movieTitle={movie.title} />}
              </div>
            );
          })
        )}
      </div>

      {data && data.totalPages > 1 && (
        <div className="flex items-center justify-center gap-4 mt-6">
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

function MovieReviewPanel({ movieId, movieTitle }: { movieId: number; movieTitle: string }) {
  const { data, isLoading } = useAdminMovieReviews(movieId, true);

  if (isLoading) {
    return <p className="px-4 pb-4 text-sm text-text-muted">Loading reviews...</p>;
  }

  if (!data?.length) {
    return <p className="px-4 pb-4 text-sm text-text-muted">No reviews for {movieTitle}</p>;
  }

  return (
    <div className="px-4 pb-4 space-y-4 border-t border-border">
      {data.map((review) => (
        <AdminReviewCard key={review.id} review={review} movieId={movieId} />
      ))}
    </div>
  );
}

function AdminReviewCard({ review, movieId }: { review: AdminReviewResponse; movieId: number }) {
  const queryClient = useQueryClient();
  const { addToast } = useUIStore();

  const invalidate = () => {
    queryClient.invalidateQueries({ queryKey: ["admin-reviews", movieId] });
    queryClient.invalidateQueries({ queryKey: ["reviews", movieId] });
    queryClient.invalidateQueries({ queryKey: ["movies"] });
    queryClient.invalidateQueries({ queryKey: ["movie", movieId] });
  };

  const mutation = useMutation({
    mutationFn: async (action: () => Promise<void>) => {
      await action();
    },
    onSuccess: () => {
      invalidate();
      addToast({ message: "Moderation updated", type: "success" });
    },
  });

  return (
    <div className="p-4 bg-bg-elevated border border-glass-border rounded-lg space-y-3">
      <div className="flex items-start justify-between gap-4">
        <div>
          <p className="text-sm text-text-primary">{review.username}</p>
          <p className="text-[10px] text-text-dim">{new Date(review.createdAt).toLocaleString()}</p>
        </div>
        <div className="flex flex-wrap gap-2 justify-end">
          <StatusBadge hidden={review.hidden} deleted={review.deleted} />
          <ModerationActions
            hidden={review.hidden}
            deleted={review.deleted}
            onHide={() => mutation.mutate(() => reviewsApi.hideReview(review.id))}
            onUnhide={() => mutation.mutate(() => reviewsApi.unhideReview(review.id))}
            onDelete={() => mutation.mutate(() => reviewsApi.adminDeleteReview(review.id))}
            disabled={mutation.isPending}
          />
        </div>
      </div>

      {review.deleted || review.hidden ? (
        <div className="space-y-2">
          <ModerationMessage hidden={review.hidden} deleted={review.deleted} />
          <p className="text-xs text-text-dim border border-dashed border-glass-border rounded-lg p-3">
            Original: {review.content}
          </p>
        </div>
      ) : (
        <>
          <div className="flex items-center gap-1">
            {Array.from({ length: 5 }).map((_, i) => (
              <span key={i} className={`text-sm ${i < review.rating ? "text-accent-bright" : "text-text-dim"}`}>★</span>
            ))}
          </div>
          <p className="text-sm text-text-secondary">{review.content}</p>
        </>
      )}

      {review.replies.length > 0 && (
        <div className="space-y-3 pt-3 border-t border-border">
          {review.replies.map((reply) => (
            <AdminReplyCard
              key={reply.id}
              reply={reply}
              movieId={movieId}
              onMutate={(action) => mutation.mutate(action)}
              disabled={mutation.isPending}
            />
          ))}
        </div>
      )}
    </div>
  );
}

function AdminReplyCard({
  reply,
  movieId,
  onMutate,
  disabled,
}: {
  reply: AdminReplyResponse;
  movieId: number;
  onMutate: (action: () => Promise<void>) => void;
  disabled: boolean;
}) {
  const queryClient = useQueryClient();

  const invalidate = () => {
    queryClient.invalidateQueries({ queryKey: ["admin-reviews", movieId] });
    queryClient.invalidateQueries({ queryKey: ["replies"] });
  };

  return (
    <div className="pl-4 border-l border-glass-border">
      <div className="flex items-start justify-between gap-4">
        <div>
          <p className="text-xs text-text-primary">{reply.username}</p>
          <p className="text-[10px] text-text-dim">{new Date(reply.createdAt).toLocaleString()}</p>
        </div>
        <div className="flex flex-wrap gap-2 justify-end">
          <StatusBadge hidden={reply.hidden} deleted={reply.deleted} />
          <ModerationActions
            hidden={reply.hidden}
            deleted={reply.deleted}
            onHide={() => onMutate(async () => { await reviewsApi.hideReply(reply.id); invalidate(); })}
            onUnhide={() => onMutate(async () => { await reviewsApi.unhideReply(reply.id); invalidate(); })}
            onDelete={() => onMutate(async () => { await reviewsApi.adminDeleteReply(reply.id); invalidate(); })}
            disabled={disabled}
            compact
          />
        </div>
      </div>
      {reply.deleted || reply.hidden ? (
        <div className="space-y-2 mt-2">
          <ModerationMessage hidden={reply.hidden} deleted={reply.deleted} type="reply" />
          <p className="text-xs text-text-dim border border-dashed border-glass-border rounded-lg p-3">
            Original: {reply.content}
          </p>
        </div>
      ) : (
        <p className="text-sm text-text-secondary mt-2">{reply.content}</p>
      )}
    </div>
  );
}

function StatusBadge({ hidden, deleted }: { hidden: boolean; deleted: boolean }) {
  if (deleted) {
    return <span className="text-[10px] px-2 py-0.5 rounded-full border border-red-800/50 text-red-400">Deleted</span>;
  }
  if (hidden) {
    return <span className="text-[10px] px-2 py-0.5 rounded-full border border-yellow-800/50 text-yellow-400">Hidden</span>;
  }
  return <span className="text-[10px] px-2 py-0.5 rounded-full border border-green-800/50 text-green-400">Visible</span>;
}

function ModerationActions({
  hidden,
  deleted,
  onHide,
  onUnhide,
  onDelete,
  disabled,
  compact = false,
}: {
  hidden: boolean;
  deleted: boolean;
  onHide: () => void;
  onUnhide: () => void;
  onDelete: () => void;
  disabled: boolean;
  compact?: boolean;
}) {
  const size = compact ? "sm" as const : "sm" as const;

  if (deleted) {
    return null;
  }

  return (
    <>
      {hidden ? (
        <Button variant="secondary" size={size} onClick={onUnhide} disabled={disabled}>Unhide</Button>
      ) : (
        <Button variant="secondary" size={size} onClick={onHide} disabled={disabled}>Hide</Button>
      )}
      <Button variant="danger" size={size} onClick={onDelete} disabled={disabled}>Delete</Button>
    </>
  );
}
