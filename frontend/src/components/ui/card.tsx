import { HTMLAttributes, forwardRef } from "react";

interface CardProps extends HTMLAttributes<HTMLDivElement> {
  hover?: boolean;
}

export const Card = forwardRef<HTMLDivElement, CardProps>(
  ({ hover = false, className = "", children, ...props }, ref) => {
    return (
      <div
        ref={ref}
        className={`bg-bg-surface border border-border rounded-lg overflow-hidden ${hover ? "transition-all duration-300 hover:-translate-y-1 hover:shadow-[0_12px_40px_rgba(212,165,116,0.15),0_0_0_1px_rgba(212,165,116,0.2)]" : ""} ${className}`}
        {...props}
      >
        {children}
      </div>
    );
  }
);

Card.displayName = "Card";
