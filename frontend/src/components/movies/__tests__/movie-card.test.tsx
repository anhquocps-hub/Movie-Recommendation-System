import { render, screen } from "@testing-library/react";
import { describe, it, expect } from "vitest";
import { MovieCard } from "../movie-card";

const mockMovie = {
  id: 1,
  title: "Inception",
  slug: "inception",
  posterUrl: "https://image.tmdb.org/t/p/w500/poster.jpg",
  releaseDate: "2010-07-16",
  avgRating: 8.8,
  voteCount: 1200,
  genres: ["Sci-Fi", "Action"],
};

describe("MovieCard", () => {
  it("renders movie title", () => {
    render(<MovieCard movie={mockMovie} />);
    expect(screen.getByText("Inception")).toBeInTheDocument();
  });

  it("renders rating", () => {
    render(<MovieCard movie={mockMovie} />);
    expect(screen.getByText("8.8")).toBeInTheDocument();
  });

  it("renders year from releaseDate", () => {
    render(<MovieCard movie={mockMovie} />);
    expect(screen.getByText("2010")).toBeInTheDocument();
  });
});
