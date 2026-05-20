"use client";

import { createContext, useContext, useEffect, useRef, ReactNode } from "react";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { useAuthStore } from "@/stores/auth.store";
import { useQueryClient } from "@tanstack/react-query";
import { useUIStore } from "@/stores/ui.store";

interface WebSocketContextValue {
  connected: boolean;
}

const WebSocketContext = createContext<WebSocketContextValue>({ connected: false });

export function useWebSocket() {
  return useContext(WebSocketContext);
}

export function WebSocketProvider({ children }: { children: ReactNode }) {
  const clientRef = useRef<Client | null>(null);
  const { accessToken, isAuthenticated } = useAuthStore();
  const queryClient = useQueryClient();
  const { addToast } = useUIStore();

  useEffect(() => {
    if (!isAuthenticated() || !accessToken) return;

    const client = new Client({
      webSocketFactory: () => new SockJS(process.env.NEXT_PUBLIC_WS_URL || "http://localhost:8080/ws"),
      connectHeaders: { Authorization: `Bearer ${accessToken}` },
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
    });

    client.onConnect = () => {
      client.subscribe("/user/queue/notifications", (message) => {
        const notification = JSON.parse(message.body);
        queryClient.invalidateQueries({ queryKey: ["notifications"] });
        queryClient.invalidateQueries({ queryKey: ["notifications", "unread"] });
        addToast({ message: notification.message, type: "info", duration: 5000 });
      });
    };

    client.onStompError = (frame) => {
      console.error("STOMP error:", frame.headers["message"]);
    };

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
      clientRef.current = null;
    };
  }, [accessToken, isAuthenticated, queryClient, addToast]);

  return (
    <WebSocketContext.Provider value={{ connected: !!clientRef.current?.connected }}>
      {children}
    </WebSocketContext.Provider>
  );
}
