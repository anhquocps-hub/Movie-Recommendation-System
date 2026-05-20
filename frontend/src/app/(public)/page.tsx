"use client";

import dynamic from "next/dynamic";
import Link from "next/link";
import { useEffect, useState } from "react";
import { useAuthStore } from "@/stores/auth.store";
import { CinemaHeroBackdrop } from "@/components/three/cinema-hero-backdrop";
import {
  DiscoverySection,
  FeaturesFullscreen,
  TrendingFullscreen,
  JourneyFullscreen,
  GenresFullscreen,
  CommunityFullscreen,
  SpotlightFullscreen,
  useFullscreenScrollInit,
} from "@/components/scroll";

const ParticleHero = dynamic(
  () =>
    import("@/components/three/particle-hero").then((m) => ({
      default: m.ParticleHero,
    })),
  { ssr: false }
);

export default function LandingPage() {
  const [showParticles, setShowParticles] = useState(false);
  const { user, isAuthenticated } = useAuthStore();

  useEffect(() => {
    const prefersReduced = window.matchMedia(
      "(prefers-reduced-motion: reduce)"
    ).matches;
    const isMobile = window.innerWidth < 768;
    setShowParticles(!prefersReduced && !isMobile);
  }, []);

  // Initialize GSAP fullscreen scroll animations
  useFullscreenScrollInit();

  const loggedIn = isAuthenticated();

  return (
    <div className="min-h-screen bg-bg-primary">
      {/* ────────── Scroll Progress Bar ────────── */}
      <div id="fs-progress-bar" className="fs-progress-bar" />

      {/* ────────── Section 1: Hero ────────── */}
      <section className="fs-section relative h-screen flex flex-col items-center justify-center overflow-hidden">
        <CinemaHeroBackdrop />
        {showParticles && <ParticleHero />}

        {/* Landing nav — auth-aware */}
        <nav className="absolute top-0 left-0 right-0 z-10 flex items-center justify-between px-8 py-6">
          <span className="font-[family-name:var(--font-playfair)] text-lg text-accent-bright tracking-[2px]">
            CINÉMA
          </span>
          <div className="flex items-center gap-6">
            <Link
              href="/movies"
              className="text-sm text-text-muted hover:text-text-primary transition-colors"
            >
              Browse
            </Link>
            {loggedIn ? (
              <Link
                href="/movies"
                className="flex items-center gap-2 text-sm text-text-secondary hover:text-text-primary transition-colors"
              >
                <div className="w-8 h-8 rounded-full bg-bg-elevated border border-border-accent flex items-center justify-center text-xs text-accent">
                  {user?.username?.[0]?.toUpperCase() ?? "U"}
                </div>
                <span className="hidden sm:inline">{user?.username}</span>
              </Link>
            ) : (
              <Link
                href="/login"
                className="text-sm text-accent hover:text-accent-bright transition-colors"
              >
                Sign In
              </Link>
            )}
          </div>
        </nav>

        {/* Hero copy */}
        <div className="relative z-10 text-center px-6 max-w-2xl">
          <p className="fs-reveal text-sm uppercase tracking-[3px] text-accent mb-4 opacity-0" data-delay="0.3">
            Discover Your Next Masterpiece
          </p>
          <h1 className="fs-reveal font-[family-name:var(--font-playfair)] text-4xl md:text-6xl lg:text-7xl text-text-primary leading-tight mb-6 opacity-0" data-delay="0.5">
            What story will move you tonight?
          </h1>
          <p className="fs-reveal text-text-muted text-lg md:text-xl mb-8 max-w-lg mx-auto opacity-0" data-delay="0.7">
            Personalized recommendations, curated collections, and a community
            of film lovers.
          </p>
          <div className="fs-reveal flex items-center justify-center gap-4 opacity-0" data-delay="0.9">
            {loggedIn ? (
              <>
                <Link
                  href="/recommendations"
                  className="px-7 py-3.5 bg-gradient-to-br from-accent to-accent-bright text-bg-primary font-medium rounded-lg hover:opacity-90 transition-opacity"
                >
                  My Recommendations
                </Link>
                <Link
                  href="/movies"
                  className="px-7 py-3.5 border border-glass-border text-text-secondary rounded-lg hover:border-border-accent transition-colors"
                >
                  Browse Movies
                </Link>
              </>
            ) : (
              <>
                <Link
                  href="/register"
                  className="px-7 py-3.5 bg-gradient-to-br from-accent to-accent-bright text-bg-primary font-medium rounded-lg hover:opacity-90 transition-opacity"
                >
                  Get Started
                </Link>
                <Link
                  href="/movies"
                  className="px-7 py-3.5 border border-glass-border text-text-secondary rounded-lg hover:border-border-accent transition-colors"
                >
                  Browse Movies
                </Link>
              </>
            )}
          </div>
        </div>

        {/* Scroll indicator */}
        <div className="absolute bottom-8 left-1/2 -translate-x-1/2 flex flex-col items-center gap-2">
          <span className="text-[10px] uppercase tracking-wider text-text-dim animate-pulse">
            Scroll to explore
          </span>
          <div className="w-5 h-8 rounded-full border border-glass-border flex items-start justify-center p-1">
            <div className="w-1 h-2 rounded-full bg-accent animate-bounce" />
          </div>
        </div>
      </section>

      {/* ────────── Section 2: Discovery ────────── */}
      <DiscoverySection />

      {/* ────────── Section 3: Spotlight ────────── */}
      <SpotlightFullscreen />

      {/* ────────── Section 4: Features ────────── */}
      <FeaturesFullscreen />

      {/* ────────── Section 5: Trending ────────── */}
      <TrendingFullscreen />

      {/* ────────── Section 6: Journey ────────── */}
      <JourneyFullscreen />

      {/* ────────── Section 7: Genres ────────── */}
      <GenresFullscreen />

      {/* ────────── Section 8: Community ────────── */}
      <CommunityFullscreen />

      {/* ────────── Final CTA ────────── */}
      <section className="fs-section relative min-h-screen flex flex-col items-center justify-center px-6">
        <div className="absolute inset-0 bg-[radial-gradient(ellipse_50%_50%_at_50%_50%,rgba(212,165,116,0.06)_0%,transparent_60%)] pointer-events-none" />

        <div className="relative z-10 text-center max-w-2xl">
          <p className="fs-reveal text-xs uppercase tracking-[4px] text-accent mb-4 opacity-0">
            Your Journey Begins
          </p>
          <h2 className="fs-reveal font-[family-name:var(--font-playfair)] text-4xl md:text-6xl text-text-primary mb-6 opacity-0" data-delay="0.1">
            Ready to discover?
          </h2>
          <p className="fs-reveal text-text-muted text-lg mb-10 max-w-md mx-auto opacity-0" data-delay="0.2">
            Join thousands of film enthusiasts who&apos;ve already found their
            next favorite movie.
          </p>
          <div className="fs-reveal opacity-0" data-delay="0.3">
            {loggedIn ? (
              <Link
                href="/recommendations"
                className="inline-block px-10 py-5 bg-gradient-to-br from-accent to-accent-bright text-bg-primary font-medium rounded-lg hover:opacity-90 transition-opacity text-lg"
              >
                View My Recommendations
              </Link>
            ) : (
              <Link
                href="/register"
                className="inline-block px-10 py-5 bg-gradient-to-br from-accent to-accent-bright text-bg-primary font-medium rounded-lg hover:opacity-90 transition-opacity text-lg"
              >
                Create Free Account
              </Link>
            )}
          </div>
        </div>
      </section>

      {/* ────────── Footer ────────── */}
      <footer className="border-t border-border py-12 px-6">
        <div className="max-w-7xl mx-auto flex flex-col md:flex-row items-center justify-between gap-6">
          <span className="font-[family-name:var(--font-playfair)] text-sm text-accent-bright tracking-[2px]">
            CINÉMA
          </span>
          <div className="flex items-center gap-6 text-xs text-text-dim">
            <Link href="/movies" className="hover:text-text-muted transition-colors">
              Browse
            </Link>
            <Link href="/trending" className="hover:text-text-muted transition-colors">
              Trending
            </Link>
            <span>© {new Date().getFullYear()} Cinéma</span>
          </div>
        </div>
      </footer>
    </div>
  );
}
