"use client";

import { ReactNode, useEffect, useRef } from "react";

interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  title: string;
  children: ReactNode;
}

export function Modal({ isOpen, onClose, title, children }: ModalProps) {
  const dialogRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!isOpen) return;

    const handleEscape = (e: KeyboardEvent) => {
      if (e.key === "Escape") onClose();
    };

    document.addEventListener("keydown", handleEscape);
    document.body.style.overflow = "hidden";

    return () => {
      document.removeEventListener("keydown", handleEscape);
      document.body.style.overflow = "";
    };
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div
        data-testid="modal-backdrop"
        className="absolute inset-0 bg-black/60 backdrop-blur-sm"
        onClick={onClose}
        aria-hidden="true"
      />
      <div
        ref={dialogRef}
        role="dialog"
        aria-modal="true"
        aria-labelledby="modal-title"
        className="relative flex w-full max-w-md max-h-[min(90vh,720px)] flex-col bg-bg-elevated border border-glass-border rounded-xl backdrop-blur-xl z-10"
      >
        <div className="shrink-0 border-b border-glass-border px-8 py-6">
          <h2 id="modal-title" className="font-[family-name:var(--font-playfair)] text-xl text-text-primary">
            {title}
          </h2>
        </div>
        <div className="min-h-0 flex-1 overflow-y-auto px-8 py-6">{children}</div>
      </div>
    </div>
  );
}
