"use client";

import { useState } from "react";
import { useMovies } from "@/lib/hooks/use-movies";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { Button, Modal, Input } from "@/components/ui";
import * as moviesApi from "@/lib/api/movies";
import type { CreateMovieRequest } from "@/lib/types";

export default function AdminMoviesPage() {
  const [page, setPage] = useState(0);
  const { data, isLoading } = useMovies({ page, size: 15 });
  const queryClient = useQueryClient();
  const [createOpen, setCreateOpen] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);

  const deleteMutation = useMutation({
    mutationFn: (id: number) => moviesApi.deleteMovie(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["movies"] });
      setDeleteId(null);
    },
  });

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="font-[family-name:var(--font-playfair)] text-2xl text-text-primary">Movie Management</h1>
        <Button onClick={() => setCreateOpen(true)}>Add Movie</Button>
      </div>

      <div className="bg-bg-surface border border-border rounded-lg overflow-hidden">
        <table className="w-full">
          <thead>
            <tr className="border-b border-border">
              <th className="text-left px-4 py-3 text-[10px] uppercase tracking-wider text-text-dim">Title</th>
              <th className="text-left px-4 py-3 text-[10px] uppercase tracking-wider text-text-dim">Release</th>
              <th className="text-left px-4 py-3 text-[10px] uppercase tracking-wider text-text-dim">Rating</th>
              <th className="text-left px-4 py-3 text-[10px] uppercase tracking-wider text-text-dim">Status</th>
              <th className="text-right px-4 py-3 text-[10px] uppercase tracking-wider text-text-dim">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-border">
            {isLoading ? (
              <tr><td colSpan={5} className="px-4 py-8 text-center text-text-muted">Loading...</td></tr>
            ) : !data?.content.length ? (
              <tr><td colSpan={5} className="px-4 py-8 text-center text-text-muted">No movies found</td></tr>
            ) : (
              data.content.map((movie) => (
                <tr key={movie.id} className="hover:bg-glass-bg transition-colors">
                  <td className="px-4 py-3 text-sm text-text-secondary">{movie.title}</td>
                  <td className="px-4 py-3 text-sm text-text-muted">{movie.releaseDate ? new Date(movie.releaseDate).getFullYear() : "—"}</td>
                  <td className="px-4 py-3 text-sm text-accent-bright">{movie.avgRating?.toFixed(1) || "—"}</td>
                  <td className="px-4 py-3">
                    <span className="text-[10px] px-2 py-0.5 rounded-full bg-green-900/30 text-green-400 border border-green-800/50">Active</span>
                  </td>
                  <td className="px-4 py-3 text-right">
                    <Button variant="danger" size="sm" onClick={() => setDeleteId(movie.id)}>Delete</Button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {data && data.totalPages > 1 && (
        <div className="flex items-center justify-center gap-4 mt-6">
          <Button variant="secondary" size="sm" disabled={page === 0} onClick={() => setPage((p) => p - 1)}>Previous</Button>
          <span className="text-sm text-text-muted">Page {page + 1} of {data.totalPages}</span>
          <Button variant="secondary" size="sm" disabled={data.last} onClick={() => setPage((p) => p + 1)}>Next</Button>
        </div>
      )}

      <Modal isOpen={deleteId !== null} onClose={() => setDeleteId(null)} title="Delete Movie">
        <p className="text-sm text-text-secondary mb-6">Are you sure you want to delete this movie? This action cannot be undone.</p>
        <div className="flex gap-3">
          <Button variant="danger" onClick={() => deleteId && deleteMutation.mutate(deleteId)} disabled={deleteMutation.isPending}>
            {deleteMutation.isPending ? "Deleting..." : "Delete"}
          </Button>
          <Button variant="ghost" onClick={() => setDeleteId(null)}>Cancel</Button>
        </div>
      </Modal>

      <Modal isOpen={createOpen} onClose={() => setCreateOpen(false)} title="Add Movie">
        <p className="text-sm text-text-muted mb-4">Create a new movie entry</p>
        <CreateMovieForm onSuccess={() => { setCreateOpen(false); queryClient.invalidateQueries({ queryKey: ["movies"] }); }} />
      </Modal>
    </div>
  );
}

function CreateMovieForm({ onSuccess }: { onSuccess: () => void }) {
  const [title, setTitle] = useState("");
  const [overview, setOverview] = useState("");
  const [releaseDate, setReleaseDate] = useState("");
  const [runtime, setRuntime] = useState("");

  const createMutation = useMutation({
    mutationFn: (data: CreateMovieRequest) => moviesApi.createMovie(data),
    onSuccess,
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    createMutation.mutate({
      title,
      overview,
      releaseDate,
      runtimeMinutes: Number(runtime),
      genreIds: [],
    });
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <Input label="Title" value={title} onChange={(e) => setTitle(e.target.value)} required />
      <Input label="Release Date" type="date" value={releaseDate} onChange={(e) => setReleaseDate(e.target.value)} required />
      <Input label="Runtime (min)" type="number" value={runtime} onChange={(e) => setRuntime(e.target.value)} required />
      <textarea
        placeholder="Overview..."
        value={overview}
        onChange={(e) => setOverview(e.target.value)}
        rows={3}
        className="w-full px-4 py-3 bg-[rgba(255,255,255,0.02)] border border-glass-border rounded-lg text-text-secondary text-sm placeholder:text-text-dim focus:outline-none focus:border-border-accent resize-none"
        required
      />
      <Button type="submit" disabled={createMutation.isPending}>
        {createMutation.isPending ? "Creating..." : "Create Movie"}
      </Button>
    </form>
  );
}
