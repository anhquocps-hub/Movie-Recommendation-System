package com.movie.recommendation.modules.recommendation;

import com.movie.recommendation.common.constants.AppConstants;
import com.movie.recommendation.common.dto.PagedResponse;
import com.movie.recommendation.modules.movie.entity.Genre;
import com.movie.recommendation.modules.recommendation.dto.RecommendationResponse;
import com.movie.recommendation.modules.recommendation.entity.Recommendation;
import com.movie.recommendation.modules.recommendation.strategy.CollaborativeFilteringStrategy;
import com.movie.recommendation.modules.recommendation.strategy.ContentBasedStrategy;
import com.movie.recommendation.modules.review.ReviewRepository;
import com.movie.recommendation.modules.user.UserRepository;
import com.movie.recommendation.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    private static final int MIN_REVIEWS_FOR_COLLABORATIVE = 5;
    private static final int RECOMMENDATIONS_PER_USER = 20;

    private final RecommendationRepository recommendationRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final CollaborativeFilteringStrategy collaborativeStrategy;
    private final ContentBasedStrategy contentBasedStrategy;

    @Cacheable(value = AppConstants.RECOMMENDATION_CACHE, key = "#userId + ':' + #page + ':' + #size")
    public PagedResponse<RecommendationResponse> getUserRecommendations(Long userId, int page, int size) {
        size = Math.min(size, AppConstants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, size);
        Page<RecommendationResponse> recommendations = recommendationRepository
                .findByUserIdOrderByScoreDesc(userId, pageable)
                .map(this::toResponse);
        return PagedResponse.from(recommendations);
    }

    @Transactional
    @CacheEvict(value = AppConstants.RECOMMENDATION_CACHE, allEntries = true)
    public void generateRecommendationsForAllUsers() {
        long startTime = System.currentTimeMillis();
        log.info("Starting recommendation generation job...");

        List<User> activeUsers = userRepository.findAll().stream()
                .filter(User::getIsActive)
                .toList();

        int processedCount = 0;
        int collaborativeCount = 0;
        int contentBasedCount = 0;

        for (User user : activeUsers) {
            try {
                long reviewCount = reviewRepository.findAll().stream()
                        .filter(r -> r.getUser().getId().equals(user.getId()))
                        .count();

                List<Recommendation> recommendations;
                if (reviewCount >= MIN_REVIEWS_FOR_COLLABORATIVE) {
                    recommendations = collaborativeStrategy.recommend(user.getId(), RECOMMENDATIONS_PER_USER);
                    collaborativeCount++;
                } else {
                    recommendations = contentBasedStrategy.recommend(user.getId(), RECOMMENDATIONS_PER_USER);
                    contentBasedCount++;
                }

                if (!recommendations.isEmpty()) {
                    recommendationRepository.deleteAllByUserId(user.getId());
                    recommendationRepository.saveAll(recommendations);
                    processedCount++;
                }
            } catch (Exception e) {
                log.error("Failed to generate recommendations for user {}: {}", user.getId(), e.getMessage());
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("Recommendation job completed in {}ms. Processed: {}, Collaborative: {}, Content-based: {}",
                duration, processedCount, collaborativeCount, contentBasedCount);
    }

    @Transactional
    @CacheEvict(value = AppConstants.RECOMMENDATION_CACHE, allEntries = true)
    public void generateRecommendationsForUser(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || !user.getIsActive()) {
            return;
        }

        long reviewCount = reviewRepository.findAll().stream()
                .filter(r -> r.getUser().getId().equals(userId))
                .count();

        List<Recommendation> recommendations;
        if (reviewCount >= MIN_REVIEWS_FOR_COLLABORATIVE) {
            recommendations = collaborativeStrategy.recommend(userId, RECOMMENDATIONS_PER_USER);
        } else {
            recommendations = contentBasedStrategy.recommend(userId, RECOMMENDATIONS_PER_USER);
        }

        if (!recommendations.isEmpty()) {
            recommendationRepository.deleteAllByUserId(userId);
            recommendationRepository.saveAll(recommendations);
        }
    }

    private RecommendationResponse toResponse(Recommendation rec) {
        return RecommendationResponse.builder()
                .id(rec.getId())
                .score(rec.getScore())
                .strategyType(rec.getStrategyType().name())
                .generatedAt(rec.getGeneratedAt())
                .movie(RecommendationResponse.MovieSummary.builder()
                        .id(rec.getMovie().getId())
                        .title(rec.getMovie().getTitle())
                        .slug(rec.getMovie().getSlug())
                        .posterUrl(rec.getMovie().getPosterUrl())
                        .releaseDate(rec.getMovie().getReleaseDate())
                        .avgRating(rec.getMovie().getAvgRating())
                        .voteCount(rec.getMovie().getVoteCount())
                        .genres(rec.getMovie().getGenres().stream()
                                .map(Genre::getName)
                                .toList())
                        .build())
                .build();
    }
}
