"use client";

import { useState } from "react";
import { useGenres } from "@/lib/hooks/use-genres";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { Button, Modal, Input } from "@/components/ui";
import * as genresApi from "@/lib/api/genres";

export default function AdminGenresPage() {
  const { data: genres, isLoading } = useGenres();
  const queryClient = useQueryClient();
  const [createOpen, setCreateOpen] = useState(false);
  const [editGenre, setEditGenre] = useState<{ id: number; name: string } | null>(null);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [newName, setNewName] = useState("");
  const [editName, setEditName] = useState("");

  const createMutation = useMutation({
    mutationFn: () => genresApi.createGenre({ name: newName }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["genres"] });
      setCreateOpen(false);
      setNewName("");
    },
  });

  const updateMutation = useMutation({
    mutationFn: () => genresApi.updateGenre(editGenre!.id, { name: editName }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["genres"] });
      setEditGenre(null);
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => genresApi.deleteGenre(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["genres"] });
      setDeleteId(null);
    },
  });

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="font-[family-name:var(--font-playfair)] text-2xl text-text-primary">Genre Management</h1>
        <Button onClick={() => setCreateOpen(true)}>Add Genre</Button>
      </div>

      <div className="bg-bg-surface border border-border rounded-lg overflow-hidden">
        <table className="w-full">
          <thead>
            <tr className="border-b border-border">
              <th className="text-left px-4 py-3 text-[10px] uppercase tracking-wider text-text-dim">ID</th>
              <th className="text-left px-4 py-3 text-[10px] uppercase tracking-wider text-text-dim">Name</th>
              <th className="text-left px-4 py-3 text-[10px] uppercase tracking-wider text-text-dim">Slug</th>
              <th className="text-right px-4 py-3 text-[10px] uppercase tracking-wider text-text-dim">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-border">
            {isLoading ? (
              <tr><td colSpan={4} className="px-4 py-8 text-center text-text-muted">Loading...</td></tr>
            ) : !genres?.length ? (
              <tr><td colSpan={4} className="px-4 py-8 text-center text-text-muted">No genres found</td></tr>
            ) : (
              genres.map((genre) => (
                <tr key={genre.id} className="hover:bg-glass-bg transition-colors">
                  <td className="px-4 py-3 text-sm text-text-dim">{genre.id}</td>
                  <td className="px-4 py-3 text-sm text-text-secondary">{genre.name}</td>
                  <td className="px-4 py-3 text-sm text-text-muted">{genre.slug}</td>
                  <td className="px-4 py-3 text-right space-x-2">
                    <Button variant="secondary" size="sm" onClick={() => { setEditGenre(genre); setEditName(genre.name); }}>Edit</Button>
                    <Button variant="danger" size="sm" onClick={() => setDeleteId(genre.id)}>Delete</Button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      <Modal isOpen={createOpen} onClose={() => setCreateOpen(false)} title="Add Genre">
        <form onSubmit={(e) => { e.preventDefault(); createMutation.mutate(); }} className="space-y-4">
          <Input label="Genre Name" value={newName} onChange={(e) => setNewName(e.target.value)} required />
          <Button type="submit" disabled={createMutation.isPending}>
            {createMutation.isPending ? "Creating..." : "Create"}
          </Button>
        </form>
      </Modal>

      <Modal isOpen={editGenre !== null} onClose={() => setEditGenre(null)} title="Edit Genre">
        <form onSubmit={(e) => { e.preventDefault(); updateMutation.mutate(); }} className="space-y-4">
          <Input label="Genre Name" value={editName} onChange={(e) => setEditName(e.target.value)} required />
          <div className="flex gap-3">
            <Button type="submit" disabled={updateMutation.isPending}>
              {updateMutation.isPending ? "Saving..." : "Save"}
            </Button>
            <Button variant="ghost" onClick={() => setEditGenre(null)}>Cancel</Button>
          </div>
        </form>
      </Modal>

      <Modal isOpen={deleteId !== null} onClose={() => setDeleteId(null)} title="Delete Genre">
        <p className="text-sm text-text-secondary mb-6">Are you sure? Movies using this genre will lose the association.</p>
        <div className="flex gap-3">
          <Button variant="danger" onClick={() => deleteId && deleteMutation.mutate(deleteId)} disabled={deleteMutation.isPending}>
            {deleteMutation.isPending ? "Deleting..." : "Delete"}
          </Button>
          <Button variant="ghost" onClick={() => setDeleteId(null)}>Cancel</Button>
        </div>
      </Modal>
    </div>
  );
}
