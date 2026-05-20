"use client";

import Link from "next/link";
import Image from "next/image";
import { useTrendingMovies } from "@/lib/hooks/use-movies";
import { useGenres } from "@/lib/hooks/use-genres";
import { MovieCard } from "@/components/movies";
import { ScrollReveal } from "./scroll-reveal";

/* ───────────────────── Trending Now ───────────────────── */
export function TrendingSection() {
  const { data } = useTrendingMovies(0);

  if (!data?.content.length) return null;

  return (
    <ScrollReveal className="py-20 px-6 max-w-7xl mx-auto">
      <div className="flex items-center justify-between mb-8">
        <div>
          <p className="text-xs uppercase tracking-[3px] text-accent mb-2">
            What&apos;s Hot
          </p>
          <h2 className="font-[family-name:var(--font-playfair)] text-3xl md:text-4xl text-text-primary">
            Trending Now
          </h2>
        </div>
        <Link
          href="/trending"
          className="text-sm text-accent hover:text-accent-bright transition-colors group flex items-center gap-1"
        >
          View all{" "}
          <span className="inline-block transition-transform group-hover:translate-x-1">
            →
          </span>
        </Link>
      </div>
      <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-5">
        {data.content.slice(0, 5).map((movie) => (
          <MovieCard key={movie.id} movie={movie} />
        ))}
      </div>
    </ScrollReveal>
  );
}

/* ───────────────────── Feature Highlights ───────────────────── */
const features = [
  {
    icon: "🎯",
    title: "Smart Recommendations",
    desc: "Our AI studies your taste profile to surface films you'll actually love — not just what's popular.",
  },
  {
    icon: "🎬",
    title: "Curated Collections",
    desc: "Hand-picked lists from cinephiles: from hidden noir gems to boundary-pushing world cinema.",
  },
  {
    icon: "💬",
    title: "Community Reviews",
    desc: "Real conversations about film. Write reviews, reply to critics, discover new perspectives.",
  },
  {
    icon: "📋",
    title: "Personal Watchlist",
    desc: "Never lose track of a recommendation again. Organize, sort, and plan your next movie night.",
  },
];

export function FeaturesSection() {
  return (
    <section className="relative py-24 px-6 overflow-hidden">
      {/* Decorative glow */}
      <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[600px] bg-[radial-gradient(circle,rgba(212,165,116,0.06)_0%,transparent_70%)] pointer-events-none" />

      <div className="max-w-6xl mx-auto relative">
        <ScrollReveal>
          <p className="text-xs uppercase tracking-[3px] text-accent mb-2 text-center">
            Why Cinéma
          </p>
          <h2 className="font-[family-name:var(--font-playfair)] text-3xl md:text-4xl text-text-primary text-center mb-16">
            Your film journey, elevated
          </h2>
        </ScrollReveal>

        <div className="grid md:grid-cols-2 gap-8">
          {features.map((f, i) => (
            <ScrollReveal key={f.title} delay={i * 0.1}>
              <div className="group relative p-8 rounded-2xl border border-glass-border bg-glass-bg hover:border-border-accent transition-all duration-500 hover:bg-[rgba(212,165,116,0.03)]">
                <div className="text-3xl mb-4">{f.icon}</div>
                <h3 className="font-[family-name:var(--font-playfair)] text-xl text-text-primary mb-2">
                  {f.title}
                </h3>
                <p className="text-text-muted text-sm leading-relaxed">
                  {f.desc}
                </p>
                {/* Hover glow */}
                <div className="absolute inset-0 rounded-2xl opacity-0 group-hover:opacity-100 transition-opacity duration-500 bg-[radial-gradient(ellipse_at_center,rgba(212,165,116,0.04)_0%,transparent_70%)] pointer-events-none" />
              </div>
            </ScrollReveal>
          ))}
        </div>
      </div>
    </section>
  );
}

/* ───────────────────── How It Works ───────────────────── */
const steps = [
  {
    num: "01",
    title: "Create Your Profile",
    desc: "Sign up in seconds and tell us your favorite genres.",
  },
  {
    num: "02",
    title: "Rate & Review",
    desc: "Rate movies you've seen. The more you rate, the smarter we get.",
  },
  {
    num: "03",
    title: "Get Matched",
    desc: "Receive personalized recommendations tailored to your unique taste.",
  },
  {
    num: "04",
    title: "Discover & Repeat",
    desc: "Explore new films, join discussions, and build your perfect watchlist.",
  },
];

export function HowItWorksSection() {
  return (
    <section className="py-24 px-6 relative">
      <div className="absolute inset-0 bg-[linear-gradient(180deg,transparent_0%,rgba(212,165,116,0.02)_50%,transparent_100%)] pointer-events-none" />

      <div className="max-w-5xl mx-auto relative">
        <ScrollReveal>
          <p className="text-xs uppercase tracking-[3px] text-accent mb-2 text-center">
            Getting Started
          </p>
          <h2 className="font-[family-name:var(--font-playfair)] text-3xl md:text-4xl text-text-primary text-center mb-16">
            Four steps to movie bliss
          </h2>
        </ScrollReveal>

        <div className="grid md:grid-cols-4 gap-0 relative">
          {/* Connecting line */}
          <div className="hidden md:block absolute top-8 left-[12.5%] right-[12.5%] h-px bg-gradient-to-r from-transparent via-border-accent to-transparent" />

          {steps.map((s, i) => (
            <ScrollReveal key={s.num} delay={i * 0.12}>
              <div className="text-center px-4 relative">
                <div className="w-16 h-16 mx-auto rounded-full border border-border-accent bg-bg-surface flex items-center justify-center mb-5 relative z-10">
                  <span className="font-[family-name:var(--font-playfair)] text-lg text-accent-bright">
                    {s.num}
                  </span>
                </div>
                <h3 className="text-text-primary font-medium mb-2 text-sm">
                  {s.title}
                </h3>
                <p className="text-text-muted text-xs leading-relaxed">
                  {s.desc}
                </p>
              </div>
            </ScrollReveal>
          ))}
        </div>
      </div>
    </section>
  );
}

/* ───────────────────── Explore Genres ───────────────────── */
export function GenreSection() {
  const { data: genres } = useGenres();

  if (!genres?.length) return null;

  return (
    <ScrollReveal className="py-20 px-6 max-w-7xl mx-auto">
      <p className="text-xs uppercase tracking-[3px] text-accent mb-2">
        Browse by Mood
      </p>
      <h2 className="font-[family-name:var(--font-playfair)] text-3xl md:text-4xl text-text-primary mb-8">
        Explore Genres
      </h2>
      <div className="flex flex-wrap gap-3">
        {genres.map((genre, i) => (
          <ScrollReveal key={genre.id} delay={i * 0.03}>
            <Link
              href={`/movies?genreId=${genre.id}`}
              className="px-5 py-2.5 text-sm text-text-secondary border border-border rounded-full hover:border-border-accent hover:text-accent hover:bg-[rgba(212,165,116,0.04)] transition-all duration-300"
            >
              {genre.name}
            </Link>
          </ScrollReveal>
        ))}
      </div>
    </ScrollReveal>
  );
}

/* ───────────────────── Stats / Social Proof ───────────────────── */
const stats = [
  { label: "Movies", value: "10K+", suffix: "" },
  { label: "Reviews", value: "50K+", suffix: "" },
  { label: "Community Members", value: "25K+", suffix: "" },
  { label: "Genres", value: "20+", suffix: "" },
];

export function StatsSection() {
  return (
    <section className="py-20 px-6 border-y border-border">
      <div className="max-w-5xl mx-auto grid grid-cols-2 md:grid-cols-4 gap-8">
        {stats.map((s, i) => (
          <ScrollReveal key={s.label} delay={i * 0.08}>
            <div className="text-center">
              <p className="font-[family-name:var(--font-playfair)] text-4xl md:text-5xl text-accent-bright mb-1">
                {s.value}
              </p>
              <p className="text-xs uppercase tracking-[2px] text-text-muted">
                {s.label}
              </p>
            </div>
          </ScrollReveal>
        ))}
      </div>
    </section>
  );
}

/* ───────────────────── Testimonials ───────────────────── */
const testimonials = [
  {
    quote:
      "Cinéma helped me rediscover why I fell in love with movies. The recommendations are uncannily good.",
    author: "Film Buff",
    role: "Watched 300+ movies",
  },
  {
    quote:
      "I used to spend 30 minutes deciding what to watch. Now I just check my Cinéma feed and hit play.",
    author: "Weekend Viewer",
    role: "Saved 200+ to watchlist",
  },
  {
    quote:
      "The community discussions add so much depth. It's like having a film club in your pocket.",
    author: "Cinephile",
    role: "Written 150+ reviews",
  },
];

export function TestimonialsSection() {
  return (
    <section className="py-24 px-6">
      <div className="max-w-6xl mx-auto">
        <ScrollReveal>
          <p className="text-xs uppercase tracking-[3px] text-accent mb-2 text-center">
            Community Voices
          </p>
          <h2 className="font-[family-name:var(--font-playfair)] text-3xl md:text-4xl text-text-primary text-center mb-16">
            What film lovers say
          </h2>
        </ScrollReveal>

        <div className="grid md:grid-cols-3 gap-6">
          {testimonials.map((t, i) => (
            <ScrollReveal key={i} delay={i * 0.1}>
              <div className="p-8 rounded-2xl border border-glass-border bg-glass-bg h-full flex flex-col">
                <svg
                  className="w-8 h-8 text-accent/30 mb-4 flex-shrink-0"
                  fill="currentColor"
                  viewBox="0 0 32 32"
                >
                  <path d="M10 8c-3.3 0-6 2.7-6 6v10h10V14H8c0-1.1.9-2 2-2V8zm14 0c-3.3 0-6 2.7-6 6v10h10V14h-6c0-1.1.9-2 2-2V8z" />
                </svg>
                <p className="text-text-secondary text-sm leading-relaxed mb-6 flex-1">
                  {t.quote}
                </p>
                <div className="border-t border-border pt-4">
                  <p className="text-text-primary text-sm font-medium">
                    {t.author}
                  </p>
                  <p className="text-text-dim text-xs">{t.role}</p>
                </div>
              </div>
            </ScrollReveal>
          ))}
        </div>
      </div>
    </section>
  );
}

/* ───────────────────── Spotlight Movie ───────────────────── */
export function SpotlightSection() {
  const { data } = useTrendingMovies(0);
  const movie = data?.content?.[0];

  if (!movie) return null;

  return (
    <section className="py-24 px-6 relative overflow-hidden">
      {/* Big blurred poster background */}
      {movie.posterUrl && (
        <div className="absolute inset-0 opacity-[0.06]">
          <Image
            src={movie.posterUrl}
            alt=""
            fill
            className="object-cover blur-3xl scale-110"
            aria-hidden="true"
          />
        </div>
      )}

      <div className="max-w-5xl mx-auto relative flex flex-col md:flex-row items-center gap-12">
        <ScrollReveal className="flex-shrink-0">
          <div className="w-56 md:w-64 aspect-[2/3] relative rounded-xl overflow-hidden shadow-2xl shadow-accent/10 border border-glass-border">
            {movie.posterUrl ? (
              <Image
                src={movie.posterUrl}
                alt={movie.title}
                fill
                className="object-cover"
                sizes="256px"
              />
            ) : (
              <div className="absolute inset-0 flex items-center justify-center bg-bg-elevated text-text-dim text-sm">
                No Poster
              </div>
            )}
          </div>
        </ScrollReveal>

        <ScrollReveal delay={0.15} className="text-center md:text-left">
          <p className="text-xs uppercase tracking-[3px] text-accent mb-3">
            Editor&apos;s Pick
          </p>
          <h2 className="font-[family-name:var(--font-playfair)] text-3xl md:text-5xl text-text-primary mb-4 leading-tight">
            {movie.title}
          </h2>
          <div className="flex items-center gap-4 justify-center md:justify-start mb-6">
            {movie.avgRating && (
              <span className="text-accent-bright text-lg font-medium">
                ★ {movie.avgRating.toFixed(1)}
              </span>
            )}
            {movie.releaseDate && (
              <span className="text-text-muted text-sm">
                {new Date(movie.releaseDate).getFullYear()}
              </span>
            )}
            {movie.genres?.length > 0 && (
              <span className="text-text-dim text-sm">
                {movie.genres.slice(0, 3).join(" · ")}
              </span>
            )}
          </div>
          <Link
            href={`/movies/${movie.id}`}
            className="inline-flex items-center gap-2 px-6 py-3 bg-gradient-to-br from-accent to-accent-bright text-bg-primary font-medium rounded-lg hover:opacity-90 transition-opacity"
          >
            View Details
            <span>→</span>
          </Link>
        </ScrollReveal>
      </div>
    </section>
  );
}
