"use client";

import Image from "next/image";
import { useScrollAnimation } from "@/lib/hooks/use-scroll-animation";

interface ParallaxBackdropProps {
  src: string;
  alt: string;
}

export function ParallaxBackdrop({ src, alt }: ParallaxBackdropProps) {
  const ref = useScrollAnimation();

  return (
    <div ref={ref} className="absolute inset-0 will-change-transform">
      <Image src={src} alt={alt} fill className="object-cover" priority />
    </div>
  );
}
