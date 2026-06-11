"use client";

import { useState } from "react";
import { useProfile, useUpdateProfile, useUpdatePreferences } from "@/lib/hooks/use-profile";
import { useGenres } from "@/lib/hooks/use-genres";
import { Button, Input, Modal, Skeleton } from "@/components/ui";

export default function ProfilePage() {
  const { data: profile, isLoading } = useProfile();
  const { data: genres } = useGenres();
  const updateProfile = useUpdateProfile();
  const updatePreferences = useUpdatePreferences();
  const [editOpen, setEditOpen] = useState(false);
  const [username, setUsername] = useState("");
  const [avatarUrl, setAvatarUrl] = useState("");

  if (isLoading) {
    return (
      <div className="grid grid-cols-1 lg:grid-cols-[300px_1fr] gap-8">
        <Skeleton className="h-80 rounded-lg" />
        <Skeleton className="h-96 rounded-lg" />
      </div>
    );
  }

  if (!profile) return null;

  const openEdit = () => {
    setUsername(profile.username);
    setAvatarUrl(profile.avatarUrl || "");
    setEditOpen(true);
  };

  const handleSaveProfile = async () => {
    await updateProfile.mutateAsync({ username, avatarUrl: avatarUrl || undefined });
    setEditOpen(false);
  };

  const togglePreference = (genreName: string) => {
    const current = profile.preferences || [];
    const updated = current.includes(genreName)
      ? current.filter((g) => g !== genreName)
      : [...current, genreName];
    updatePreferences.mutate({ preferences: updated });
  };

  return (
    <div className="grid grid-cols-1 lg:grid-cols-[300px_1fr] gap-8">
      <aside className="p-6 bg-bg-surface border border-border rounded-lg h-fit">
        <div className="flex flex-col items-center text-center">
          <div className="w-20 h-20 rounded-full bg-bg-elevated border border-border flex items-center justify-center text-2xl text-accent mb-4">
            {profile.username[0]?.toUpperCase()}
          </div>
          <h2 className="text-lg text-text-primary">{profile.username}</h2>
          <p className="text-xs text-text-muted mt-1">{profile.email}</p>
          <p className="text-[10px] uppercase tracking-wider text-text-dim mt-2">{profile.role}</p>
        </div>

        <div className="mt-6 pt-6 border-t border-border space-y-3">
          <div className="flex justify-between text-sm">
            <span className="text-text-muted">Member since</span>
            <span className="text-text-secondary">{new Date(profile.createdAt).toLocaleDateString()}</span>
          </div>
        </div>

        <Button variant="secondary" size="sm" className="w-full mt-6" onClick={openEdit}>
          Edit Profile
        </Button>
      </aside>

      <div className="space-y-8">
        <section>
          <h2 className="font-[family-name:var(--font-playfair)] text-xl text-text-primary mb-4">Genre Preferences</h2>
          <p className="text-sm text-text-muted mb-4">Select genres you enjoy for better recommendations</p>
          <div className="flex flex-wrap gap-2">
            {genres?.map((genre) => {
              const isSelected = profile.preferences?.includes(genre.name);
              return (
                <button
                  key={genre.id}
                  onClick={() => togglePreference(genre.name)}
                  className={`px-3 py-1.5 text-xs rounded-full border transition-all ${
                    isSelected
                      ? "bg-accent/10 border-border-accent text-accent"
                      : "bg-bg-elevated border-border text-text-muted hover:border-border-accent"
                  }`}
                >
                  {genre.name}
                </button>
              );
            })}
          </div>
        </section>
      </div>

      <Modal isOpen={editOpen} onClose={() => setEditOpen(false)} title="Edit Profile">
        <div className="space-y-4">
          <Input label="Username" value={username} onChange={(e) => setUsername(e.target.value)} />
          <Input label="Avatar URL" value={avatarUrl} onChange={(e) => setAvatarUrl(e.target.value)} placeholder="https://..." />
          <div className="flex gap-3 pt-2">
            <Button onClick={handleSaveProfile} disabled={updateProfile.isPending}>
              {updateProfile.isPending ? "Saving..." : "Save"}
            </Button>
            <Button variant="ghost" onClick={() => setEditOpen(false)}>Cancel</Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
