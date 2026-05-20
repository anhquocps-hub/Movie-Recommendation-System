import type { Metadata } from "next";
import { playfair, outfit, jetbrainsMono } from "./fonts";
import { Providers } from "@/providers";
import { SkipLink } from "@/components/ui/skip-link";
import "./globals.css";

export const metadata: Metadata = {
  title: "CINÉMA — Movie Recommendations",
  description: "Discover your next masterpiece",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html
      lang="en"
      className={`${playfair.variable} ${outfit.variable} ${jetbrainsMono.variable}`}
    >
      <body>
        <SkipLink />
        <Providers>
          <div id="main-content">{children}</div>
        </Providers>
      </body>
    </html>
  );
}
