"use client";

import { useEffect } from "react";
import { Button } from "@/components/ui";

export default function Error({
  error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  useEffect(() => {
    console.error(error);
  }, [error]);

  return (
    <div className="min-h-screen bg-bg-primary flex items-center justify-center">
      <div className="text-center px-6">
        <h1 className="font-[family-name:var(--font-playfair)] text-3xl text-text-primary mb-4">Something went wrong</h1>
        <p className="text-sm text-text-muted mb-8">An unexpected error occurred. Please try again.</p>
        <Button onClick={reset}>Try Again</Button>
      </div>
    </div>
  );
}
