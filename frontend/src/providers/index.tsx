"use client";

import { ReactNode } from "react";
import { QueryProvider } from "./query-provider";
import { WebSocketProvider } from "./websocket-provider";
import { LenisProvider } from "./lenis-provider";
import { LazyMotion, domAnimation } from "framer-motion";

export function Providers({ children }: { children: ReactNode }) {
  return (
    <QueryProvider>
      <LazyMotion features={domAnimation}>
        <LenisProvider>
          <WebSocketProvider>{children}</WebSocketProvider>
        </LenisProvider>
      </LazyMotion>
    </QueryProvider>
  );
}
