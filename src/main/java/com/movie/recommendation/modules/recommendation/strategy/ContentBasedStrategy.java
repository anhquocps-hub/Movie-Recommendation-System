package com.movie.recommendation.modules.recommendation.strategy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie.recommendation.modules.movie.MovieRepository;
import com.movie.recommendation.modules.movie.entity.Genre;
import com.movie.recommendation.modules.movie.entity.Movie;
import com.movie.recommendation.modules.recommendation.entity.Recommendation;
import com.movie.recommendation.modules.review.ReviewRepository;
import com.movie.recommendation.modules.review.entity.Review;
import com.movie.recommendation.modules.user.UserRepository;
import com.movie.recommendation.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ContentBasedStrategy implements RecommendationStrategy {

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final MovieRepository movieRepository;
    private final ObjectMapper objectMapper;

    @Override
    public List<Recommendation> recommend(Long userId, int limit) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return Collections.emptyList();
        }

        Set<String> preferredGenres = parsePreferences(user.getPreferences());
        Set<String> reviewedGenres = getGenresFromReviewedMovies(userId);

        Set<String> allPreferredGenres = new HashSet<>(preferredGenres);
        allPreferredGenres.addAll(reviewedGenres);

        if (allPreferredGenres.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> reviewedMovieIds = reviewRepository.findAll().stream()
                .filter(r -> r.getUser().getId().equals(userId))
                .map(r -> r.getMovie().getId())
                .collect(Collectors.toSet());

        List<Movie> candidateMovies = movieRepository
                .findAllByIsActiveTrue(PageRequest.of(0, 500))
                .getContent().stream()
                .filter(m -> !reviewedMovieIds.contains(m.getId()))
                .toList();

        List<ScoredMovie> scoredMovies = candidateMovies.stream()
                .map(movie -> {
                    double genreScore = calculateGenreOverlap(movie, allPreferredGenres);
                    double ratingBoost = movie.getAvgRating().doubleValue() / 5.0 * 0.2;
                    double popularityBoost = Math.min(movie.getVoteCount() / 1000.0, 0.1);
                    return new ScoredMovie(movie, genreScore + ratingBoost + popularityBoost);
                })
                .filter(sm -> sm.score > 0)
                .sorted(Comparator.comparingDouble(ScoredMovie::score).reversed())
                .limit(limit)
                .toList();

        double maxScore = scoredMovies.isEmpty() ? 1.0
                : scoredMovies.getFirst().score;

        List<Recommendation> recommendations = new ArrayList<>();
        for (ScoredMovie sm : scoredMovies) {
            double normalizedScore = maxScore > 0 ? sm.score / maxScore : 0;
            Recommendation rec = Recommendation.builder()
                    .user(user)
                    .movie(sm.movie)
                    .score(BigDecimal.valueOf(normalizedScore)
                            .setScale(4, RoundingMode.HALF_UP))
                    .strategyType(Recommendation.StrategyType.CONTENT_BASED)
                    .build();
            recommendations.add(rec);
        }

        return recommendations;
    }

    private Set<String> parsePreferences(String preferencesJson) {
        if (preferencesJson == null || preferencesJson.isBlank()) {
            return Collections.emptySet();
        }
        try {
            List<String> prefs = objectMapper.readValue(preferencesJson, new TypeReference<>() {});
            return prefs.stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.warn("Failed to parse user preferences: {}", e.getMessage());
            return Collections.emptySet();
        }
    }

    private Set<String> getGenresFromReviewedMovies(Long userId) {
        return reviewRepository.findAll().stream()
                .filter(r -> r.getUser().getId().equals(userId))
                .filter(r -> r.getRating() >= 4)
                .map(Review::getMovie)
                .flatMap(m -> m.getGenres().stream())
                .map(g -> g.getName().toLowerCase())
                .collect(Collectors.toSet());
    }

    private double calculateGenreOverlap(Movie movie, Set<String> preferredGenres) {
        Set<String> movieGenres = movie.getGenres().stream()
                .map(Genre::getName)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        if (movieGenres.isEmpty() || preferredGenres.isEmpty()) {
            return 0.0;
        }

        long overlap = movieGenres.stream()
                .filter(preferredGenres::contains)
                .count();

        return (double) overlap / movieGenres.size();
    }

    @Override
    public Recommendation.StrategyType getStrategyType() {
        return Recommendation.StrategyType.CONTENT_BASED;
    }

    private record ScoredMovie(Movie movie, double score) {}
}
