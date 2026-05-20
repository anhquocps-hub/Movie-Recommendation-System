"use client";

import { Canvas } from "@react-three/fiber";
import { Suspense } from "react";
import { ParticleField } from "./particle-field";

/**
 * Thin 3D canvas overlay — renders only the floating
 * dust motes.  All other cinema effects (projector beam,
 * bokeh, film grain, light leaks) live in the CSS-based
 * CinemaHeroBackdrop component for maximum performance.
 */
export function ParticleHero() {
  return (
    <div className="absolute inset-0 z-[2] pointer-events-none">
      <Canvas
        camera={{ position: [0, 0, 4], fov: 50 }}
        dpr={[1, 1.5]}
        gl={{ antialias: false, alpha: true, powerPreference: "high-performance" }}
        style={{ background: "transparent" }}
      >
        <Suspense fallback={null}>
          <ambientLight intensity={0.4} />
          <ParticleField />
        </Suspense>
      </Canvas>
    </div>
  );
}
