import { describe, it, expect, beforeEach } from "vitest";
import { useAuthStore } from "../auth.store";

describe("Auth Store", () => {
  beforeEach(() => {
    useAuthStore.getState().clearAuth();
  });

  it("starts with no auth", () => {
    const state = useAuthStore.getState();
    expect(state.accessToken).toBeNull();
    expect(state.user).toBeNull();
    expect(state.isAuthenticated()).toBe(false);
  });

  it("sets auth state", () => {
    useAuthStore.getState().setAuth("token-123", { username: "alice", role: "USER" });
    const state = useAuthStore.getState();
    expect(state.accessToken).toBe("token-123");
    expect(state.user?.username).toBe("alice");
    expect(state.isAuthenticated()).toBe(true);
  });

  it("clears auth state", () => {
    useAuthStore.getState().setAuth("token-123", { username: "alice", role: "USER" });
    useAuthStore.getState().clearAuth();
    const state = useAuthStore.getState();
    expect(state.accessToken).toBeNull();
    expect(state.user).toBeNull();
    expect(state.isAuthenticated()).toBe(false);
  });
});
