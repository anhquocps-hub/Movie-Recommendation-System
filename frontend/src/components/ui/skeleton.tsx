interface SkeletonProps {
  className?: string;
}

export function Skeleton({ className = "" }: SkeletonProps) {
  return (
    <div
      className={`animate-pulse bg-gradient-to-r from-bg-surface via-bg-elevated to-bg-surface bg-[length:200%_100%] rounded-lg ${className}`}
      aria-hidden="true"
    />
  );
}
