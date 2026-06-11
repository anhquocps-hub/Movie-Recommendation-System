"use client";

import { useRef, useState } from "react";
import Image from "next/image";
import { Button } from "@/components/ui";
import { uploadPoster } from "@/lib/api/uploads";

interface PosterUploadProps {
  value: string | null;
  onChange: (url: string | null) => void;
}

export function PosterUpload({ value, onChange }: PosterUploadProps) {
  const inputRef = useRef<HTMLInputElement>(null);
  const [preview, setPreview] = useState<string | null>(null);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState("");

  const displayUrl = value || preview;

  const handleFileChange = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;

    setError("");
    const localPreview = URL.createObjectURL(file);
    setPreview(localPreview);
    setUploading(true);

    try {
      const url = await uploadPoster(file);
      onChange(url);
      setPreview(null);
      URL.revokeObjectURL(localPreview);
    } catch (err: unknown) {
      const message =
        (err as { response?: { data?: { message?: string } } })?.response?.data?.message ||
        "Failed to upload poster";
      setError(message);
      onChange(null);
      setPreview(null);
      URL.revokeObjectURL(localPreview);
    } finally {
      setUploading(false);
      if (inputRef.current) {
        inputRef.current.value = "";
      }
    }
  };

  const handleRemove = () => {
    setError("");
    setPreview(null);
    onChange(null);
    if (inputRef.current) {
      inputRef.current.value = "";
    }
  };

  return (
    <div className="space-y-3">
      <div className="flex items-center justify-between">
        <label className="block text-[11px] uppercase tracking-wider text-text-muted">
          Poster
        </label>
        {displayUrl && (
          <button
            type="button"
            onClick={handleRemove}
            className="text-[11px] text-text-dim hover:text-red-400 transition-colors"
            disabled={uploading}
          >
            Remove
          </button>
        )}
      </div>

      <div className="flex gap-4 items-start">
        <div className="relative w-24 aspect-[2/3] rounded-lg overflow-hidden border border-glass-border bg-bg-elevated shrink-0">
          {displayUrl ? (
            <Image
              src={displayUrl}
              alt="Poster preview"
              fill
              className="object-cover"
              unoptimized={displayUrl.startsWith("blob:")}
            />
          ) : (
            <div className="absolute inset-0 flex items-center justify-center text-[10px] text-text-dim px-2 text-center">
              No poster
            </div>
          )}
        </div>

        <div className="flex-1 space-y-2">
          <input
            ref={inputRef}
            type="file"
            accept="image/jpeg,image/png,image/webp"
            onChange={handleFileChange}
            className="hidden"
          />
          <Button
            type="button"
            variant="secondary"
            size="sm"
            onClick={() => inputRef.current?.click()}
            disabled={uploading}
          >
            {uploading ? "Uploading..." : displayUrl ? "Change Poster" : "Upload Poster"}
          </Button>
          <p className="text-[10px] text-text-dim">
            JPG, PNG, or WebP. Uploaded to Cloudinary and saved as a URL in the database.
          </p>
          {error && <p className="text-xs text-red-400">{error}</p>}
        </div>
      </div>
    </div>
  );
}
