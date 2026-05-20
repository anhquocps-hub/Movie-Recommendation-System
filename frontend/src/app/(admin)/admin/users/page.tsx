"use client";

import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Button, Modal } from "@/components/ui";
import * as usersApi from "@/lib/api/users";

export default function AdminUsersPage() {
  const [page, setPage] = useState(0);
  const queryClient = useQueryClient();
  const [deactivateId, setDeactivateId] = useState<number | null>(null);

  const { data, isLoading } = useQuery({
    queryKey: ["admin", "users", page],
    queryFn: () => usersApi.getAllUsers(page, 15),
  });

  const deactivateMutation = useMutation({
    mutationFn: (id: number) => usersApi.deactivateUser(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "users"] });
      setDeactivateId(null);
    },
  });

  const roleMutation = useMutation({
    mutationFn: ({ id, role }: { id: number; role: string }) => usersApi.changeUserRole(id, role),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "users"] });
    },
  });

  return (
    <div>
      <h1 className="font-[family-name:var(--font-playfair)] text-2xl text-text-primary mb-6">User Management</h1>

      <div className="bg-bg-surface border border-border rounded-lg overflow-hidden">
        <table className="w-full">
          <thead>
            <tr className="border-b border-border">
              <th className="text-left px-4 py-3 text-[10px] uppercase tracking-wider text-text-dim">Username</th>
              <th className="text-left px-4 py-3 text-[10px] uppercase tracking-wider text-text-dim">Email</th>
              <th className="text-left px-4 py-3 text-[10px] uppercase tracking-wider text-text-dim">Role</th>
              <th className="text-left px-4 py-3 text-[10px] uppercase tracking-wider text-text-dim">Status</th>
              <th className="text-left px-4 py-3 text-[10px] uppercase tracking-wider text-text-dim">Joined</th>
              <th className="text-right px-4 py-3 text-[10px] uppercase tracking-wider text-text-dim">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-border">
            {isLoading ? (
              <tr><td colSpan={6} className="px-4 py-8 text-center text-text-muted">Loading...</td></tr>
            ) : !data?.content.length ? (
              <tr><td colSpan={6} className="px-4 py-8 text-center text-text-muted">No users found</td></tr>
            ) : (
              data.content.map((user) => (
                <tr key={user.id} className="hover:bg-glass-bg transition-colors">
                  <td className="px-4 py-3 text-sm text-text-secondary">{user.username}</td>
                  <td className="px-4 py-3 text-sm text-text-muted">{user.email}</td>
                  <td className="px-4 py-3">
                    <select
                      value={user.role}
                      onChange={(e) => roleMutation.mutate({ id: user.id, role: e.target.value })}
                      className="bg-bg-elevated border border-glass-border rounded px-2 py-1 text-xs text-text-secondary"
                    >
                      <option value="USER">USER</option>
                      <option value="ADMIN">ADMIN</option>
                    </select>
                  </td>
                  <td className="px-4 py-3">
                    <span className={`text-[10px] px-2 py-0.5 rounded-full border ${
                      user.isActive
                        ? "bg-green-900/30 text-green-400 border-green-800/50"
                        : "bg-red-900/30 text-red-400 border-red-800/50"
                    }`}>
                      {user.isActive ? "Active" : "Inactive"}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-sm text-text-dim">{new Date(user.createdAt).toLocaleDateString()}</td>
                  <td className="px-4 py-3 text-right">
                    {user.isActive && (
                      <Button variant="danger" size="sm" onClick={() => setDeactivateId(user.id)}>Deactivate</Button>
                    )}
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

      <Modal isOpen={deactivateId !== null} onClose={() => setDeactivateId(null)} title="Deactivate User">
        <p className="text-sm text-text-secondary mb-6">This will prevent the user from logging in. Are you sure?</p>
        <div className="flex gap-3">
          <Button variant="danger" onClick={() => deactivateId && deactivateMutation.mutate(deactivateId)} disabled={deactivateMutation.isPending}>
            {deactivateMutation.isPending ? "Deactivating..." : "Deactivate"}
          </Button>
          <Button variant="ghost" onClick={() => setDeactivateId(null)}>Cancel</Button>
        </div>
      </Modal>
    </div>
  );
}
