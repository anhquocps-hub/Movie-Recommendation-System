"use client";

import { useState } from "react";
import { useForm } from "react-hook-form";
import { Button } from "@/components/ui";
import { useCreateReview } from "@/lib/hooks/use-reviews";
import type { CreateReviewRequest } from "@/lib/types";

interface ReviewFormProps {
  movieId: number;
  onSuccess?: () => void;
}

export function ReviewForm({ movieId, onSuccess }: ReviewFormProps) {
  const [rating, setRating] = useState(0);
  const [hoverRating, setHoverRating] = useState(0);
  const createReview = useCreateReview(movieId);

  const { register, handleSubmit, reset, formState: { errors } } = useForm<{ content: string; isSpoiler: boolean }>();

  const onSubmit = async (data: { content: string; isSpoiler: boolean }) => {
    if (rating === 0) return;
    await createReview.mutateAsync({ rating, content: data.content, isSpoiler: data.isSpoiler });
    reset();
    setRating(0);
    onSuccess?.();
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="p-5 bg-bg-surface border border-border rounded-lg">
      <h3 className="text-sm font-medium text-text-primary mb-4">Write a Review</h3>

      <div className="flex items-center gap-1 mb-4">
        {Array.from({ length: 5 }).map((_, i) => (
          <button
            key={i}
            type="button"
            onClick={() => setRating(i + 1)}
            onMouseEnter={() => setHoverRating(i + 1)}
            onMouseLeave={() => setHoverRating(0)}
            className={`text-xl transition-colors ${
              i < (hoverRating || rating) ? "text-accent-bright" : "text-text-dim"
            }`}
          >
            ★
          </button>
        ))}
        {rating === 0 && <span className="text-xs text-red-400 ml-2">Select a rating</span>}
      </div>

      <textarea
        {...register("content", { required: "Review content is required", minLength: { value: 10, message: "At least 10 characters" } })}
        placeholder="Share your thoughts about this movie..."
        rows={4}
        className="w-full px-4 py-3 bg-[rgba(255,255,255,0.02)] border border-glass-border rounded-lg text-text-secondary text-sm placeholder:text-text-dim focus:outline-none focus:border-border-accent resize-none"
      />
      {errors.content && <p className="text-xs text-red-400 mt-1">{errors.content.message}</p>}

      <label className="flex items-center gap-2 mt-3 cursor-pointer">
        <input type="checkbox" {...register("isSpoiler")} className="rounded border-border" />
        <span className="text-xs text-text-muted">Contains spoilers</span>
      </label>

      <Button type="submit" size="sm" className="mt-4" disabled={createReview.isPending || rating === 0}>
        {createReview.isPending ? "Posting..." : "Post Review"}
      </Button>
    </form>
  );
}
