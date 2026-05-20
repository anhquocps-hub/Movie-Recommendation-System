import Link from "next/link";

export default function NotFound() {
  return (
    <div className="min-h-screen bg-bg-primary flex items-center justify-center">
      <div className="text-center px-6">
        <h1 className="font-[family-name:var(--font-playfair)] text-6xl text-text-primary mb-4">404</h1>
        <p className="text-lg text-text-muted mb-2">Page not found</p>
        <p className="text-sm text-text-dim mb-8">The page you&apos;re looking for doesn&apos;t exist or has been moved.</p>
        <Link
          href="/"
          className="inline-block px-6 py-3 bg-gradient-to-br from-accent to-accent-bright text-bg-primary font-medium rounded-lg hover:opacity-90 transition-opacity"
        >
          Go Home
        </Link>
      </div>
    </div>
  );
}
