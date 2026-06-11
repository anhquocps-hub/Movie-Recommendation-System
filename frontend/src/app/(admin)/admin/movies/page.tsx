"use client";

import { useState } from "react";
import { useMovies, useMovie } from "@/lib/hooks/use-movies";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { Button, Modal } from "@/components/ui";
import { MovieForm } from "@/components/admin/movie-form";
import { useUIStore } from "@/stores/ui.store";
import * as moviesApi from "@/lib/api/movies";
import type { CreateMovieRequest } from "@/lib/types";

export default function AdminMoviesPage() {
  const [page, setPage] = useState(0);
  const { data, isLoading } = useMovies({ page, size: 15 });
  const queryClient = useQueryClient();
  const [createOpen, setCreateOpen] = useState(false);
  const [editId, setEditId] = useState<number | null>(null);
  const [deleteId, setDeleteId] = useState<number | null>(null);

  const deleteMutation = useMutation({
    mutationFn: (id: number) => moviesApi.deleteMovie(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["movies"] });
      setDeleteId(null);
    },
  });

  const invalidateMovies = () => {
    queryClient.invalidateQueries({ queryKey: ["movies"] });
    if (editId) {
      queryClient.invalidateQueries({ queryKey: ["movie", editId] });
    }
  };

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
                    <div className="flex justify-end gap-2">
                      <Button variant="secondary" size="sm" onClick={() => setEditId(movie.id)}>Edit</Button>
                      <Button variant="danger" size="sm" onClick={() => setDeleteId(movie.id)}>Delete</Button>
                    </div>
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
        <CreateMovieForm onSuccess={() => { setCreateOpen(false); invalidateMovies(); }} />
      </Modal>

      <Modal isOpen={editId !== null} onClose={() => setEditId(null)} title="Edit Movie">
        {editId && (
          <EditMovieForm
            movieId={editId}
            onSuccess={() => { setEditId(null); invalidateMovies(); }}
          />
        )}
      </Modal>
    </div>
  );
}

function CreateMovieForm({ onSuccess }: { onSuccess: () => void }) {
  const { addToast } = useUIStore();
  const [error, setError] = useState("");

  const createMutation = useMutation({
    mutationFn: (data: CreateMovieRequest) => moviesApi.createMovie(data),
    onSuccess: () => {
      addToast({ message: "Movie created successfully", type: "success" });
      onSuccess();
    },
    onError: (err: { response?: { data?: { message?: string } } }) => {
      setError(err.response?.data?.message || "Failed to create movie");
    },
  });

  return (
    <MovieForm
      onSubmit={(data) => createMutation.mutate(data)}
      isPending={createMutation.isPending}
      submitLabel="Create Movie"
      error={error}
    />
  );
}

function EditMovieForm({ movieId, onSuccess }: { movieId: number; onSuccess: () => void }) {
  const { addToast } = useUIStore();
  const { data: movie, isLoading, isError } = useMovie(movieId);
  const [error, setError] = useState("");

  const updateMutation = useMutation({
    mutationFn: (data: CreateMovieRequest) => moviesApi.updateMovie(movieId, data),
    onSuccess: () => {
      addToast({ message: "Movie updated successfully", type: "success" });
      onSuccess();
    },
    onError: (err: { response?: { data?: { message?: string } } }) => {
      setError(err.response?.data?.message || "Failed to update movie");
    },
  });

  if (isLoading) {
    return <p className="text-sm text-text-muted py-4 text-center">Loading movie...</p>;
  }

  if (isError || !movie) {
    return <p className="text-sm text-red-400 py-4 text-center">Failed to load movie details</p>;
  }

  return (
    <>
      <p className="text-sm text-text-muted mb-4">Update movie details and poster</p>
      <MovieForm
        key={movie.id}
        initialValues={{
          title: movie.title,
          overview: movie.overview,
          releaseDate: movie.releaseDate?.slice(0, 10) ?? "",
          runtimeMinutes: movie.runtimeMinutes,
          posterUrl: movie.posterUrl,
          genreIds: movie.genres.map((genre) => genre.id),
        }}
        onSubmit={(data) => updateMutation.mutate(data)}
        isPending={updateMutation.isPending}
        submitLabel="Save Changes"
        error={error}
      />
    </>
  );
}
