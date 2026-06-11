"use client";

import { useState } from "react";
import { useReviewReplies, useCreateReply } from "@/lib/hooks/use-reviews";
import { useAuthStore } from "@/stores/auth.store";
import { Button } from "@/components/ui";
import { ModerationMessage } from "./moderation-message";
import type { ReplyResponse } from "@/lib/types";

interface ReplySectionProps {
  reviewId: number;
  movieId: number;
  replyCount: number;
}

export function ReplySection({ reviewId, movieId, replyCount }: ReplySectionProps) {
  const { isAuthenticated } = useAuthStore();
  const [expanded, setExpanded] = useState(false);
  const [content, setContent] = useState("");
  const { data, isLoading } = useReviewReplies(reviewId, expanded);
  const createReply = useCreateReply(reviewId, movieId);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!content.trim()) return;
    await createReply.mutateAsync({ content: content.trim() });
    setContent("");
    setExpanded(true);
  };

  return (
    <div className="mt-4 pt-3 border-t border-border">
      <button
        type="button"
        onClick={() => setExpanded((value) => !value)}
        className="text-xs text-accent hover:text-accent-bright transition-colors"
      >
        {expanded ? "Hide replies" : `View ${replyCount} ${replyCount === 1 ? "reply" : "replies"}`}
      </button>

      {expanded && (
        <div className="mt-4 space-y-3">
          {isLoading ? (
            <p className="text-xs text-text-dim">Loading replies...</p>
          ) : !data?.content.length ? (
            <p className="text-xs text-text-dim">No replies yet</p>
          ) : (
            data.content.map((reply) => <ReplyItem key={reply.id} reply={reply} />)
          )}

          {isAuthenticated() && (
            <form onSubmit={handleSubmit} className="flex gap-2 pt-2">
              <input
                value={content}
                onChange={(e) => setContent(e.target.value)}
                placeholder="Write a reply..."
                className="flex-1 px-3 py-2 bg-[rgba(255,255,255,0.02)] border border-glass-border rounded-lg text-sm text-text-secondary placeholder:text-text-dim focus:outline-none focus:border-border-accent"
              />
              <Button type="submit" size="sm" disabled={createReply.isPending || !content.trim()}>
                {createReply.isPending ? "..." : "Reply"}
              </Button>
            </form>
          )}
        </div>
      )}
    </div>
  );
}

function ReplyItem({ reply }: { reply: ReplyResponse }) {
  const moderated = reply.hidden || reply.deleted;

  return (
    <div className="pl-4 border-l border-glass-border">
      <div className="flex items-center gap-2 mb-1">
        <div className="w-6 h-6 rounded-full bg-bg-elevated border border-border flex items-center justify-center text-[10px] text-accent">
          {reply.username[0]?.toUpperCase()}
        </div>
        <p className="text-xs text-text-primary">{reply.username}</p>
        <p className="text-[10px] text-text-dim">{new Date(reply.createdAt).toLocaleDateString()}</p>
      </div>
      {moderated ? (
        <ModerationMessage hidden={reply.hidden} deleted={reply.deleted} type="reply" />
      ) : (
        <p className="text-sm text-text-secondary">{reply.content}</p>
      )}
    </div>
  );
}
