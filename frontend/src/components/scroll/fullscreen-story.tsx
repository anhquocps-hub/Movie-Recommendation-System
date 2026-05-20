"use client";

import { useEffect, useRef } from "react";
import Link from "next/link";
import Image from "next/image";
import { useTrendingMovies } from "@/lib/hooks/use-movies";
import { useGenres } from "@/lib/hooks/use-genres";
import { MovieCard } from "@/components/movies";

/* ═══════════════════════════════════════════════════════════════
   FullscreenStory
   ═══════════════════════════════════════════════════════════════
   Fullscreen, GSAP-pinned storytelling sections. Each section
   occupies 100vh and uses scrub-linked animations triggered
   by scroll progress. Inspired by Apple product pages and
   award-winning Awwwards scrollytelling.

   Story flow:
     1. Discovery  — "Thousands of films, one that's yours"
     2. Features   — 4 pillars revealed sequentially
     3. Trending   — Horizontal movie showcase
     4. Journey    — Step-by-step guide with counter animation
     5. Community  — Testimonials + stats
   ══════════════════════════════════════════════════════════════ */

/* ────────── Fullscreen Section wrapper ────────── */
interface FullscreenSectionProps {
  id: string;
  children: React.ReactNode;
  className?: string;
}

function FullscreenSection({ id, children, className = "" }: FullscreenSectionProps) {
  return (
    <section
      id={id}
      className={`fs-section relative w-full min-h-screen flex items-center justify-center overflow-hidden ${className}`}
    >
      {children}
    </section>
  );
}

/* ────────── 1. Discovery Section ────────── */
export function DiscoverySection() {
  return (
    <FullscreenSection id="fs-discovery">
      {/* Background glow */}
      <div className="absolute inset-0 bg-[radial-gradient(ellipse_60%_50%_at_50%_50%,rgba(212,165,116,0.06)_0%,transparent_60%)] pointer-events-none" />

      <div className="relative z-10 max-w-4xl mx-auto text-center px-6">
        <p
          className="fs-reveal text-xs uppercase tracking-[4px] text-accent mb-6 opacity-0"
          data-delay="0"
        >
          The Art of Discovery
        </p>
        <h2
          className="fs-reveal font-[family-name:var(--font-playfair)] text-4xl md:text-6xl lg:text-7xl text-text-primary leading-[1.15] mb-8 opacity-0"
          data-delay="0.1"
        >
          Thousands of films.
          <br />
          <span className="text-accent-bright">One that&apos;s yours.</span>
        </h2>
        <p
          className="fs-reveal text-text-muted text-lg md:text-xl max-w-2xl mx-auto leading-relaxed opacity-0"
          data-delay="0.2"
        >
          Our recommendation engine learns what moves you — analyzing your
          ratings, reviews, and watch history to find the films that resonate
          with your unique taste.
        </p>
      </div>

      {/* Decorative film frames floating */}
      <div className="absolute top-[15%] left-[8%] w-20 h-28 border border-glass-border rounded-md opacity-[0.06] rotate-[-8deg] fs-float" />
      <div className="absolute bottom-[20%] right-[10%] w-16 h-24 border border-glass-border rounded-md opacity-[0.05] rotate-[12deg] fs-float" style={{ animationDelay: "2s" }} />
      <div className="absolute top-[60%] left-[5%] w-12 h-18 border border-glass-border rounded-md opacity-[0.04] rotate-[5deg] fs-float" style={{ animationDelay: "4s" }} />
    </FullscreenSection>
  );
}

/* ────────── 2. Features Section (stacked reveal) ────────── */
const features = [
  {
    icon: "🎯",
    title: "Smart Recommendations",
    desc: "AI-powered taste profiling surfaces films you'll love.",
    accent: "rgba(240, 198, 116, 0.08)",
  },
  {
    icon: "🎬",
    title: "Curated Collections",
    desc: "Hand-picked lists from noir gems to world cinema.",
    accent: "rgba(212, 165, 116, 0.06)",
  },
  {
    icon: "💬",
    title: "Community Reviews",
    desc: "Real conversations. Write, reply, discover perspectives.",
    accent: "rgba(180, 140, 90, 0.06)",
  },
  {
    icon: "📋",
    title: "Personal Watchlist",
    desc: "Never lose a recommendation. Plan your movie night.",
    accent: "rgba(160, 130, 80, 0.05)",
  },
];

export function FeaturesFullscreen() {
  return (
    <FullscreenSection id="fs-features">
      <div className="absolute inset-0 bg-[linear-gradient(180deg,rgba(10,10,10,0)_0%,rgba(212,165,116,0.03)_50%,rgba(10,10,10,0)_100%)] pointer-events-none" />

      <div className="relative z-10 max-w-6xl mx-auto px-6 w-full">
        <div className="text-center mb-16">
          <p className="fs-reveal text-xs uppercase tracking-[4px] text-accent mb-3 opacity-0">
            Why Cinéma
          </p>
          <h2 className="fs-reveal font-[family-name:var(--font-playfair)] text-3xl md:text-5xl text-text-primary opacity-0" data-delay="0.1">
            Your film journey, elevated
          </h2>
        </div>

        <div className="grid md:grid-cols-2 gap-6">
          {features.map((f, i) => (
            <div
              key={f.title}
              className="fs-reveal opacity-0 group relative p-8 md:p-10 rounded-2xl border border-glass-border bg-glass-bg hover:border-border-accent transition-all duration-500"
              data-delay={String(0.15 + i * 0.1)}
              style={{ willChange: "transform, opacity" }}
            >
              <div className="text-4xl mb-5">{f.icon}</div>
              <h3 className="font-[family-name:var(--font-playfair)] text-xl md:text-2xl text-text-primary mb-3">
                {f.title}
              </h3>
              <p className="text-text-muted text-sm leading-relaxed">
                {f.desc}
              </p>
              <div
                className="absolute inset-0 rounded-2xl opacity-0 group-hover:opacity-100 transition-opacity duration-500 pointer-events-none"
                style={{
                  background: `radial-gradient(ellipse at center, ${f.accent} 0%, transparent 70%)`,
                }}
              />
            </div>
          ))}
        </div>
      </div>
    </FullscreenSection>
  );
}

/* ────────── 3. Trending Showcase (horizontal scroll within pinned section) ────────── */
export function TrendingFullscreen() {
  const { data } = useTrendingMovies(0);
  const movies = data?.content?.slice(0, 10) ?? [];

  if (!movies.length) return null;

  return (
    <FullscreenSection id="fs-trending">
      <div className="relative z-10 w-full">
        <div className="px-6 md:px-12 mb-10">
          <p className="fs-reveal text-xs uppercase tracking-[4px] text-accent mb-3 opacity-0">
            What&apos;s Hot
          </p>
          <div className="flex items-end justify-between">
            <h2 className="fs-reveal font-[family-name:var(--font-playfair)] text-3xl md:text-5xl text-text-primary opacity-0" data-delay="0.1">
              Trending Now
            </h2>
            <Link
              href="/trending"
              className="fs-reveal opacity-0 text-sm text-accent hover:text-accent-bright transition-colors group flex items-center gap-1"
              data-delay="0.2"
            >
              View all{" "}
              <span className="inline-block transition-transform group-hover:translate-x-1">→</span>
            </Link>
          </div>
        </div>

        {/* Horizontal scrolling movie row */}
        <div className="fs-horizontal-track flex gap-5 px-6 md:px-12 pb-4 overflow-x-auto scrollbar-hide">
          {movies.map((movie, i) => (
            <div
              key={movie.id}
              className="fs-reveal opacity-0 flex-shrink-0 w-[180px] md:w-[220px]"
              data-delay={String(0.1 + i * 0.05)}
            >
              <MovieCard movie={movie} />
            </div>
          ))}
        </div>
      </div>
    </FullscreenSection>
  );
}

/* ────────── 4. Journey Section (numbered steps, cinematic) ────────── */
const journeySteps = [
  { num: "01", title: "Create Your Profile", desc: "Sign up and tell us your favorite genres. It takes 30 seconds." },
  { num: "02", title: "Rate & Review", desc: "Rate films you've seen. Every rating makes your feed smarter." },
  { num: "03", title: "Get Matched", desc: "Receive personalized picks tailored to your unique cinematic taste." },
  { num: "04", title: "Discover & Repeat", desc: "Explore, discuss, and build the perfect watchlist — endlessly." },
];

export function JourneyFullscreen() {
  return (
    <FullscreenSection id="fs-journey">
      <div className="absolute inset-0 bg-[radial-gradient(ellipse_50%_50%_at_50%_50%,rgba(212,165,116,0.04)_0%,transparent_60%)] pointer-events-none" />

      <div className="relative z-10 max-w-5xl mx-auto px-6 w-full">
        <div className="text-center mb-16">
          <p className="fs-reveal text-xs uppercase tracking-[4px] text-accent mb-3 opacity-0">
            Getting Started
          </p>
          <h2 className="fs-reveal font-[family-name:var(--font-playfair)] text-3xl md:text-5xl text-text-primary opacity-0" data-delay="0.1">
            Four steps to movie bliss
          </h2>
        </div>

        <div className="grid md:grid-cols-4 gap-8 relative">
          {/* Connecting line */}
          <div className="hidden md:block absolute top-10 left-[12.5%] right-[12.5%] h-px bg-gradient-to-r from-transparent via-border-accent to-transparent" />

          {journeySteps.map((s, i) => (
            <div
              key={s.num}
              className="fs-reveal opacity-0 text-center relative"
              data-delay={String(0.15 + i * 0.12)}
            >
              <div className="w-20 h-20 mx-auto rounded-full border border-border-accent bg-bg-surface flex items-center justify-center mb-6 relative z-10 transition-all duration-500 hover:border-accent-bright hover:shadow-[0_0_30px_rgba(212,165,116,0.15)]">
                <span className="font-[family-name:var(--font-playfair)] text-2xl text-accent-bright">
                  {s.num}
                </span>
              </div>
              <h3 className="text-text-primary font-medium mb-2">{s.title}</h3>
              <p className="text-text-muted text-sm leading-relaxed">{s.desc}</p>
            </div>
          ))}
        </div>
      </div>
    </FullscreenSection>
  );
}

/* ────────── 5. Genres Showcase ────────── */
export function GenresFullscreen() {
  const { data: genres } = useGenres();
  if (!genres?.length) return null;

  return (
    <FullscreenSection id="fs-genres">
      <div className="relative z-10 max-w-6xl mx-auto px-6 w-full">
        <div className="mb-12">
          <p className="fs-reveal text-xs uppercase tracking-[4px] text-accent mb-3 opacity-0">
            Browse by Mood
          </p>
          <h2 className="fs-reveal font-[family-name:var(--font-playfair)] text-3xl md:text-5xl text-text-primary opacity-0" data-delay="0.1">
            Explore Genres
          </h2>
        </div>

        <div className="flex flex-wrap gap-3">
          {genres.map((genre, i) => (
            <Link
              key={genre.id}
              href={`/movies?genreId=${genre.id}`}
              className="fs-reveal opacity-0 px-6 py-3 text-sm text-text-secondary border border-border rounded-full hover:border-accent hover:text-accent hover:bg-[rgba(212,165,116,0.04)] transition-all duration-300 hover:scale-105"
              data-delay={String(0.1 + i * 0.02)}
            >
              {genre.name}
            </Link>
          ))}
        </div>
      </div>
    </FullscreenSection>
  );
}

/* ────────── 6. Community / Stats Section ────────── */
const stats = [
  { value: "10K+", label: "Movies" },
  { value: "50K+", label: "Reviews" },
  { value: "25K+", label: "Members" },
  { value: "20+", label: "Genres" },
];

const testimonials = [
  { quote: "Cinéma helped me rediscover why I fell in love with movies.", author: "Film Buff" },
  { quote: "I just check my Cinéma feed and hit play. No more decision fatigue.", author: "Weekend Viewer" },
  { quote: "The community discussions add so much depth to every film.", author: "Cinephile" },
];

export function CommunityFullscreen() {
  return (
    <FullscreenSection id="fs-community">
      <div className="relative z-10 max-w-6xl mx-auto px-6 w-full">
        {/* Stats row */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-8 mb-20">
          {stats.map((s, i) => (
            <div
              key={s.label}
              className="fs-reveal opacity-0 text-center"
              data-delay={String(i * 0.08)}
            >
              <p className="font-[family-name:var(--font-playfair)] text-5xl md:text-6xl text-accent-bright mb-2">
                {s.value}
              </p>
              <p className="text-xs uppercase tracking-[2px] text-text-muted">
                {s.label}
              </p>
            </div>
          ))}
        </div>

        {/* Testimonials */}
        <div className="grid md:grid-cols-3 gap-6">
          {testimonials.map((t, i) => (
            <div
              key={i}
              className="fs-reveal opacity-0 p-8 rounded-2xl border border-glass-border bg-glass-bg"
              data-delay={String(0.3 + i * 0.1)}
            >
              <svg className="w-8 h-8 text-accent/20 mb-4" fill="currentColor" viewBox="0 0 32 32">
                <path d="M10 8c-3.3 0-6 2.7-6 6v10h10V14H8c0-1.1.9-2 2-2V8zm14 0c-3.3 0-6 2.7-6 6v10h10V14h-6c0-1.1.9-2 2-2V8z" />
              </svg>
              <p className="text-text-secondary text-sm leading-relaxed mb-4">
                &ldquo;{t.quote}&rdquo;
              </p>
              <p className="text-text-dim text-xs uppercase tracking-wider">
                — {t.author}
              </p>
            </div>
          ))}
        </div>
      </div>
    </FullscreenSection>
  );
}

/* ────────── Spotlight (uses real data) ────────── */
export function SpotlightFullscreen() {
  const { data } = useTrendingMovies(0);
  const movie = data?.content?.[0];
  if (!movie) return null;

  return (
    <FullscreenSection id="fs-spotlight">
      {movie.posterUrl && (
        <div className="absolute inset-0 opacity-[0.05]">
          <Image src={movie.posterUrl} alt="" fill className="object-cover blur-3xl scale-110" aria-hidden="true" />
        </div>
      )}

      <div className="relative z-10 max-w-5xl mx-auto px-6 flex flex-col md:flex-row items-center gap-12 w-full">
        <div className="fs-reveal opacity-0 flex-shrink-0" data-delay="0">
          <div className="w-56 md:w-72 aspect-[2/3] relative rounded-xl overflow-hidden shadow-2xl shadow-accent/10 border border-glass-border">
            {movie.posterUrl ? (
              <Image src={movie.posterUrl} alt={movie.title} fill className="object-cover" sizes="288px" />
            ) : (
              <div className="absolute inset-0 flex items-center justify-center bg-bg-elevated text-text-dim">No Poster</div>
            )}
          </div>
        </div>

        <div className="fs-reveal opacity-0 text-center md:text-left" data-delay="0.15">
          <p className="text-xs uppercase tracking-[4px] text-accent mb-4">Editor&apos;s Pick</p>
          <h2 className="font-[family-name:var(--font-playfair)] text-3xl md:text-5xl lg:text-6xl text-text-primary mb-5 leading-tight">
            {movie.title}
          </h2>
          <div className="flex items-center gap-4 justify-center md:justify-start mb-8">
            {movie.avgRating && <span className="text-accent-bright text-xl font-medium">★ {movie.avgRating.toFixed(1)}</span>}
            {movie.releaseDate && <span className="text-text-muted">{new Date(movie.releaseDate).getFullYear()}</span>}
            {movie.genres?.length > 0 && <span className="text-text-dim text-sm">{movie.genres.slice(0, 3).join(" · ")}</span>}
          </div>
          <Link
            href={`/movies/${movie.id}`}
            className="inline-flex items-center gap-2 px-8 py-4 bg-gradient-to-br from-accent to-accent-bright text-bg-primary font-medium rounded-lg hover:opacity-90 transition-opacity text-lg"
          >
            View Details <span>→</span>
          </Link>
        </div>
      </div>
    </FullscreenSection>
  );
}

/* ────────── Scroll Initializer ────────── */
export function useFullscreenScrollInit() {
  const initialized = useRef(false);

  useEffect(() => {
    if (initialized.current) return;
    if (typeof window === "undefined") return;
    if (window.matchMedia("(prefers-reduced-motion: reduce)").matches) {
      // Still show content, just without animation
      document.querySelectorAll(".fs-reveal").forEach((el) => {
        (el as HTMLElement).style.opacity = "1";
      });
      return;
    }

    initialized.current = true;
    let cleanup: (() => void) | undefined;

    (async () => {
      const { gsap } = await import("gsap");
      const { ScrollTrigger } = await import("gsap/ScrollTrigger");
      gsap.registerPlugin(ScrollTrigger);

      // For each fullscreen section, create a scroll-triggered reveal
      const sections = document.querySelectorAll(".fs-section");

      sections.forEach((section) => {
        const reveals = section.querySelectorAll(".fs-reveal");

        reveals.forEach((el) => {
          const delay = parseFloat((el as HTMLElement).dataset.delay || "0");

          gsap.fromTo(
            el,
            { opacity: 0, y: 50 },
            {
              opacity: 1,
              y: 0,
              duration: 1,
              delay,
              ease: "power3.out",
              scrollTrigger: {
                trigger: section,
                start: "top 70%",
                toggleActions: "play none none none",
              },
            }
          );
        });
      });

      // Progress indicator update
      const progressBar = document.getElementById("fs-progress-bar");
      if (progressBar) {
        ScrollTrigger.create({
          trigger: document.body,
          start: "top top",
          end: "bottom bottom",
          onUpdate: (self) => {
            progressBar.style.transform = `scaleX(${self.progress})`;
          },
        });
      }

      cleanup = () => {
        ScrollTrigger.getAll().forEach((t) => t.kill());
      };
    })();

    return () => cleanup?.();
  }, []);
}
