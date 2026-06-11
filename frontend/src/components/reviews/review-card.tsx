"use client";

import { useToggleLike } from "@/lib/hooks/use-reviews";
import { useAuthStore } from "@/stores/auth.store";
import { ModerationMessage } from "./moderation-message";
import { ReplySection } from "./reply-section";
import type { ReviewResponse } from "@/lib/types";

interface ReviewCardProps {
  review: ReviewResponse;
  movieId: number;
}

export function ReviewCard({ review, movieId }: ReviewCardProps) {
  const { isAuthenticated } = useAuthStore();
  const toggleLike = useToggleLike(movieId);
  const moderated = review.hidden || review.deleted;
  const timeAgo = getTimeAgo(review.createdAt);

  return (
    <div className="p-5 bg-bg-surface border border-border rounded-lg">
      <div className="flex items-center justify-between mb-3">
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-full bg-bg-elevated border border-border flex items-center justify-center text-xs text-accent">
            {review.username[0]?.toUpperCase()}
          </div>
          <div>
            <p className="text-sm text-text-primary">{review.username}</p>
            <p className="text-[10px] text-text-dim">{timeAgo}</p>
          </div>
        </div>
        {!moderated && review.rating != null && (
          <div className="flex items-center gap-1">
            {Array.from({ length: 5 }).map((_, i) => (
              <span key={i} className={`text-sm ${i < review.rating! ? "text-accent-bright" : "text-text-dim"}`}>★</span>
            ))}
          </div>
        )}
      </div>

      {moderated ? (
        <ModerationMessage hidden={review.hidden} deleted={review.deleted} />
      ) : (
        <>
          {review.isSpoiler && (
            <p className="text-[10px] uppercase tracking-wider text-red-400 mb-2">⚠ Contains spoilers</p>
          )}
          <p className="text-sm text-text-secondary leading-relaxed">{review.content}</p>
        </>
      )}

      <div className="flex items-center gap-4 mt-4 pt-3 border-t border-border">
        <button
          onClick={() => isAuthenticated() && !moderated && toggleLike.mutate(review.id)}
          disabled={!isAuthenticated() || moderated}
          className={`flex items-center gap-1.5 text-xs transition-colors ${
            review.likedByCurrentUser ? "text-accent-bright" : "text-text-muted hover:text-text-primary"
          } disabled:opacity-50`}
        >
          <span>{review.likedByCurrentUser ? "♥" : "♡"}</span>
          <span>{review.likeCount}</span>
        </button>
        <span className="text-xs text-text-dim">{review.replyCount} replies</span>
      </div>

      <ReplySection reviewId={review.id} movieId={movieId} replyCount={review.replyCount} />
    </div>
  );
}

function getTimeAgo(dateStr: string): string {
  const diff = Date.now() - new Date(dateStr).getTime();
  const minutes = Math.floor(diff / 60000);
  if (minutes < 60) return `${minutes}m ago`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours}h ago`;
  const days = Math.floor(hours / 24);
  if (days < 30) return `${days}d ago`;
  return new Date(dateStr).toLocaleDateString();
}
