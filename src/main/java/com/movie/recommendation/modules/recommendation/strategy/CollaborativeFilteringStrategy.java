package com.movie.recommendation.modules.recommendation.strategy;

import com.movie.recommendation.modules.movie.MovieRepository;
import com.movie.recommendation.modules.movie.entity.Movie;
import com.movie.recommendation.modules.recommendation.entity.Recommendation;
import com.movie.recommendation.modules.review.ReviewRepository;
import com.movie.recommendation.modules.review.entity.Review;
import com.movie.recommendation.modules.user.UserRepository;
import com.movie.recommendation.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class CollaborativeFilteringStrategy implements RecommendationStrategy {

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final MovieRepository movieRepository;

    @Override
    public List<Recommendation> recommend(Long userId, int limit) {
        User targetUser = userRepository.findById(userId).orElse(null);
        if (targetUser == null) {
            return Collections.emptyList();
        }

        List<Review> targetUserReviews = reviewRepository.findAll().stream()
                .filter(r -> r.getUser().getId().equals(userId))
                .toList();

        if (targetUserReviews.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Short> targetUserRatings = targetUserReviews.stream()
                .collect(Collectors.toMap(
                        r -> r.getMovie().getId(),
                        Review::getRating
                ));

        List<User> allUsers = userRepository.findAll().stream()
                .filter(u -> !u.getId().equals(userId) && u.getIsActive())
                .toList();

        Map<Long, Double> userSimilarities = new HashMap<>();

        for (User otherUser : allUsers) {
            List<Review> otherUserReviews = reviewRepository.findAll().stream()
                    .filter(r -> r.getUser().getId().equals(otherUser.getId()))
                    .toList();

            if (otherUserReviews.isEmpty()) {
                continue;
            }

            Map<Long, Short> otherUserRatings = otherUserReviews.stream()
                    .collect(Collectors.toMap(
                            r -> r.getMovie().getId(),
                            Review::getRating
                    ));

            double similarity = calculateCosineSimilarity(targetUserRatings, otherUserRatings);
            if (similarity > 0) {
                userSimilarities.put(otherUser.getId(), similarity);
            }
        }

        Map<Long, Double> movieScores = new HashMap<>();
        Map<Long, Double> movieWeights = new HashMap<>();

        for (Map.Entry<Long, Double> entry : userSimilarities.entrySet()) {
            Long similarUserId = entry.getKey();
            Double similarity = entry.getValue();

            List<Review> similarUserReviews = reviewRepository.findAll().stream()
                    .filter(r -> r.getUser().getId().equals(similarUserId))
                    .filter(r -> !targetUserRatings.containsKey(r.getMovie().getId()))
                    .toList();

            for (Review review : similarUserReviews) {
                Long movieId = review.getMovie().getId();
                double weightedRating = review.getRating() * similarity;

                movieScores.merge(movieId, weightedRating, Double::sum);
                movieWeights.merge(movieId, similarity, Double::sum);
            }
        }

        List<Map.Entry<Long, Double>> rankedMovies = movieScores.entrySet().stream()
                .map(entry -> {
                    Long movieId = entry.getKey();
                    Double totalScore = entry.getValue();
                    Double totalWeight = movieWeights.get(movieId);
                    Double normalizedScore = totalScore / totalWeight;
                    return Map.entry(movieId, normalizedScore);
                })
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(limit)
                .toList();

        List<Recommendation> recommendations = new ArrayList<>();
        for (Map.Entry<Long, Double> entry : rankedMovies) {
            Movie movie = movieRepository.findByIdAndIsActiveTrue(entry.getKey()).orElse(null);
            if (movie != null) {
                Recommendation rec = Recommendation.builder()
                        .user(targetUser)
                        .movie(movie)
                        .score(BigDecimal.valueOf(entry.getValue() / 5.0)
                                .setScale(4, RoundingMode.HALF_UP))
                        .strategyType(Recommendation.StrategyType.COLLABORATIVE)
                        .build();
                recommendations.add(rec);
            }
        }

        return recommendations;
    }

    private double calculateCosineSimilarity(Map<Long, Short> ratings1, Map<Long, Short> ratings2) {
        Set<Long> commonMovies = new HashSet<>(ratings1.keySet());
        commonMovies.retainAll(ratings2.keySet());

        if (commonMovies.isEmpty()) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (Long movieId : commonMovies) {
            double r1 = ratings1.get(movieId);
            double r2 = ratings2.get(movieId);
            dotProduct += r1 * r2;
            norm1 += r1 * r1;
            norm2 += r2 * r2;
        }

        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    @Override
    public Recommendation.StrategyType getStrategyType() {
        return Recommendation.StrategyType.COLLABORATIVE;
    }
}
