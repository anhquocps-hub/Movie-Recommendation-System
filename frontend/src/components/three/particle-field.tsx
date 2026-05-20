"use client";

import { useRef, useMemo } from "react";
import { useFrame } from "@react-three/fiber";
import * as THREE from "three";

/**
 * Cinema dust motes — tiny round particles drifting gently,
 * simulating dust caught in a warm projector beam.
 * Uses a procedural circular texture so they appear as
 * soft glowing dots rather than harsh squares.
 */

const DUST_COUNT = 80;

/** Creates a small circular gradient texture for soft round particles */
function createDustTexture(): THREE.Texture {
  const size = 64;
  const canvas = document.createElement("canvas");
  canvas.width = size;
  canvas.height = size;
  const ctx = canvas.getContext("2d")!;

  const gradient = ctx.createRadialGradient(
    size / 2, size / 2, 0,
    size / 2, size / 2, size / 2
  );
  gradient.addColorStop(0, "rgba(255, 225, 170, 1)");
  gradient.addColorStop(0.3, "rgba(255, 210, 140, 0.6)");
  gradient.addColorStop(0.7, "rgba(212, 165, 116, 0.15)");
  gradient.addColorStop(1, "rgba(212, 165, 116, 0)");

  ctx.fillStyle = gradient;
  ctx.fillRect(0, 0, size, size);

  const texture = new THREE.CanvasTexture(canvas);
  texture.needsUpdate = true;
  return texture;
}

export function ParticleField() {
  const dustRef = useRef<THREE.Points>(null);
  const dustTexture = useMemo(() => createDustTexture(), []);

  const [positions, basePositions, sizes] = useMemo(() => {
    const pos = new Float32Array(DUST_COUNT * 3);
    const base = new Float32Array(DUST_COUNT * 3);
    const sz = new Float32Array(DUST_COUNT);

    for (let i = 0; i < DUST_COUNT; i++) {
      const i3 = i * 3;

      // Sparse distribution — most particles near center
      const r = Math.pow(Math.random(), 0.6) * 3.5;
      const angle = Math.random() * Math.PI * 2;

      const x = Math.cos(angle) * r;
      const y = (Math.random() - 0.5) * 5;
      const z = Math.sin(angle) * r * 0.5 - 1;

      pos[i3] = x;
      pos[i3 + 1] = y;
      pos[i3 + 2] = z;

      base[i3] = x;
      base[i3 + 1] = y;
      base[i3 + 2] = z;

      sz[i] = 0.08 + Math.random() * 0.15;
    }

    return [pos, base, sz];
  }, []);

  useFrame((state) => {
    if (!dustRef.current) return;
    const time = state.clock.elapsedTime;

    const posArr = dustRef.current.geometry.attributes.position.array as Float32Array;
    const sizeArr = dustRef.current.geometry.attributes.size.array as Float32Array;

    for (let i = 0; i < DUST_COUNT; i++) {
      const i3 = i * 3;
      const seed = i * 1.37;

      // Gentle lazy drift
      posArr[i3]     = basePositions[i3]     + Math.sin(time * 0.08 + seed) * 0.3;
      posArr[i3 + 1] = basePositions[i3 + 1] + Math.cos(time * 0.06 + seed * 0.7) * 0.2
                        + time * 0.008 * ((i % 3) - 1);
      posArr[i3 + 2] = basePositions[i3 + 2] + Math.sin(time * 0.07 + seed * 0.5) * 0.12;

      // Wrap vertically
      if (posArr[i3 + 1] > 3)  posArr[i3 + 1] -= 6;
      if (posArr[i3 + 1] < -3) posArr[i3 + 1] += 6;

      // Twinkle — gentle opacity-like size change
      const baseSz = sizes[i];
      sizeArr[i] = baseSz * (0.5 + 0.5 * Math.abs(Math.sin(time * 0.6 + seed * 2)));
    }

    dustRef.current.geometry.attributes.position.needsUpdate = true;
    dustRef.current.geometry.attributes.size.needsUpdate = true;

    // Extremely slow rotation
    dustRef.current.rotation.y = Math.sin(time * 0.03) * 0.05;
  });

  return (
    <points ref={dustRef}>
      <bufferGeometry>
        <bufferAttribute attach="attributes-position" args={[positions, 3]} />
        <bufferAttribute attach="attributes-size" args={[sizes, 1]} />
      </bufferGeometry>
      <pointsMaterial
        map={dustTexture}
        size={0.15}
        transparent
        opacity={0.5}
        sizeAttenuation
        depthWrite={false}
        blending={THREE.AdditiveBlending}
        color="#ffe0aa"
      />
    </points>
  );
}
