package com.movie.recommendation.modules.recommendation;

import com.movie.recommendation.common.constants.AppConstants;
import com.movie.recommendation.common.dto.PagedResponse;
import com.movie.recommendation.modules.movie.entity.Genre;
import com.movie.recommendation.modules.movie.entity.Movie;
import com.movie.recommendation.modules.recommendation.dto.RecommendationResponse;
import com.movie.recommendation.modules.recommendation.entity.Recommendation;
import com.movie.recommendation.modules.recommendation.strategy.CollaborativeFilteringStrategy;
import com.movie.recommendation.modules.recommendation.strategy.ContentBasedStrategy;
import com.movie.recommendation.modules.review.ReviewRepository;
import com.movie.recommendation.modules.review.entity.Review;
import com.movie.recommendation.modules.notification.event.NotificationEvent;
import com.movie.recommendation.modules.user.UserRepository;
import com.movie.recommendation.modules.user.entity.Role;
import com.movie.recommendation.modules.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock private RecommendationRepository recommendationRepository;
    @Mock private UserRepository userRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private CollaborativeFilteringStrategy collaborativeStrategy;
    @Mock private ContentBasedStrategy contentBasedStrategy;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private RecommendationService recommendationService;

    private User buildUser(Long id) {
        return User.builder()
                .id(id)
                .email("user" + id + "@test.com")
                .username("user" + id)
                .passwordHash("hash")
                .role(Role.USER)
                .isActive(true)
                .preferences("[\"action\"]")
                .build();
    }

    private Movie buildMovie(Long id, String title) {
        Genre genre = Genre.builder().id(1).name("Action").build();
        return Movie.builder()
                .id(id)
                .title(title)
                .slug(title.toLowerCase().replace(" ", "-"))
                .overview("Test overview")
                .releaseDate(LocalDate.of(2024, 1, 1))
                .runtimeMinutes(120)
                .avgRating(BigDecimal.valueOf(4.0))
                .voteCount(100)
                .isActive(true)
                .genres(Set.of(genre))
                .build();
    }

    private Recommendation buildRecommendation(Long id, User user, Movie movie, double score, Recommendation.StrategyType strategy) {
        return Recommendation.builder()
                .id(id)
                .user(user)
                .movie(movie)
                .score(BigDecimal.valueOf(score))
                .strategyType(strategy)
                .build();
    }

    private Review buildReview(Long id, User user, Movie movie, short rating) {
        return Review.builder()
                .id(id)
                .user(user)
                .movie(movie)
                .rating(rating)
                .content("Test review")
                .isSpoiler(false)
                .build();
    }

    @Test
    void getUserRecommendations_success() {
        User user = buildUser(1L);
        Movie movie = buildMovie(1L, "Test Movie");
        Recommendation rec = buildRecommendation(1L, user, movie, 0.95, Recommendation.StrategyType.COLLABORATIVE);

        Pageable pageable = PageRequest.of(0, 20);
        Page<Recommendation> page = new PageImpl<>(List.of(rec), pageable, 1);

        when(recommendationRepository.findByUserIdOrderByScoreDesc(1L, pageable)).thenReturn(page);

        PagedResponse<RecommendationResponse> result = recommendationService.getUserRecommendations(1L, 0, 20);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getMovie().getTitle()).isEqualTo("Test Movie");
        assertThat(result.getContent().get(0).getScore()).isEqualByComparingTo(BigDecimal.valueOf(0.95));
        verify(recommendationRepository).findByUserIdOrderByScoreDesc(1L, pageable);
    }

    @Test
    void getUserRecommendations_enforcesMaxPageSize() {
        Pageable expectedPageable = PageRequest.of(0, AppConstants.MAX_PAGE_SIZE);
        when(recommendationRepository.findByUserIdOrderByScoreDesc(eq(1L), any(Pageable.class)))
                .thenReturn(Page.empty(expectedPageable));

        recommendationService.getUserRecommendations(1L, 0, 100);

        verify(recommendationRepository).findByUserIdOrderByScoreDesc(eq(1L), argThat(p -> p.getPageSize() == AppConstants.MAX_PAGE_SIZE));
    }

    @Test
    void generateRecommendationsForAllUsers_usesCollaborativeForUsersWithEnoughReviews() {
        User user1 = buildUser(1L);
        User user2 = buildUser(2L);
        Movie movie = buildMovie(1L, "Test Movie");

        Review review1 = buildReview(1L, user1, movie, (short) 5);
        Review review2 = buildReview(2L, user1, movie, (short) 4);
        Review review3 = buildReview(3L, user1, movie, (short) 5);
        Review review4 = buildReview(4L, user1, movie, (short) 3);
        Review review5 = buildReview(5L, user1, movie, (short) 5);

        Recommendation rec = buildRecommendation(1L, user1, movie, 0.9, Recommendation.StrategyType.COLLABORATIVE);

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));
        when(reviewRepository.findAll()).thenReturn(List.of(review1, review2, review3, review4, review5));
        when(collaborativeStrategy.recommend(1L, 20)).thenReturn(List.of(rec));
        when(contentBasedStrategy.recommend(2L, 20)).thenReturn(List.of());

        recommendationService.generateRecommendationsForAllUsers();

        verify(collaborativeStrategy).recommend(1L, 20);
        verify(contentBasedStrategy).recommend(2L, 20);
        verify(recommendationRepository).deleteAllByUserId(1L);
        verify(recommendationRepository).saveAll(List.of(rec));
    }

    @Test
    void generateRecommendationsForAllUsers_usesContentBasedForUsersWithFewReviews() {
        User user = buildUser(1L);
        Movie movie = buildMovie(1L, "Test Movie");

        Review review1 = buildReview(1L, user, movie, (short) 4);
        Review review2 = buildReview(2L, user, movie, (short) 3);

        Recommendation rec = buildRecommendation(1L, user, movie, 0.8, Recommendation.StrategyType.CONTENT_BASED);

        when(userRepository.findAll()).thenReturn(List.of(user));
        when(reviewRepository.findAll()).thenReturn(List.of(review1, review2));
        when(contentBasedStrategy.recommend(1L, 20)).thenReturn(List.of(rec));

        recommendationService.generateRecommendationsForAllUsers();

        verify(contentBasedStrategy).recommend(1L, 20);
        verify(collaborativeStrategy, never()).recommend(anyLong(), anyInt());
        verify(recommendationRepository).deleteAllByUserId(1L);
        verify(recommendationRepository).saveAll(List.of(rec));
    }

    @Test
    void generateRecommendationsForAllUsers_skipsInactiveUsers() {
        User activeUser = buildUser(1L);
        User inactiveUser = buildUser(2L);
        inactiveUser.setIsActive(false);

        when(userRepository.findAll()).thenReturn(List.of(activeUser, inactiveUser));
        when(reviewRepository.findAll()).thenReturn(List.of());
        when(contentBasedStrategy.recommend(1L, 20)).thenReturn(List.of());

        recommendationService.generateRecommendationsForAllUsers();

        verify(contentBasedStrategy).recommend(1L, 20);
        verify(contentBasedStrategy, never()).recommend(2L, 20);
        verify(collaborativeStrategy, never()).recommend(2L, 20);
    }

    @Test
    void generateRecommendationsForAllUsers_skipsUsersWithNoRecommendations() {
        User user = buildUser(1L);

        when(userRepository.findAll()).thenReturn(List.of(user));
        when(reviewRepository.findAll()).thenReturn(List.of());
        when(contentBasedStrategy.recommend(1L, 20)).thenReturn(List.of());

        recommendationService.generateRecommendationsForAllUsers();

        verify(contentBasedStrategy).recommend(1L, 20);
        verify(recommendationRepository, never()).deleteAllByUserId(anyLong());
        verify(recommendationRepository, never()).saveAll(anyList());
    }

    @Test
    void generateRecommendationsForUser_usesCollaborativeWhenEnoughReviews() {
        User user = buildUser(1L);
        Movie movie = buildMovie(1L, "Test Movie");

        Review review1 = buildReview(1L, user, movie, (short) 5);
        Review review2 = buildReview(2L, user, movie, (short) 4);
        Review review3 = buildReview(3L, user, movie, (short) 5);
        Review review4 = buildReview(4L, user, movie, (short) 3);
        Review review5 = buildReview(5L, user, movie, (short) 5);

        Recommendation rec = buildRecommendation(1L, user, movie, 0.9, Recommendation.StrategyType.COLLABORATIVE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(reviewRepository.findAll()).thenReturn(List.of(review1, review2, review3, review4, review5));
        when(collaborativeStrategy.recommend(1L, 20)).thenReturn(List.of(rec));

        recommendationService.generateRecommendationsForUser(1L);

        verify(collaborativeStrategy).recommend(1L, 20);
        verify(contentBasedStrategy, never()).recommend(anyLong(), anyInt());
        verify(recommendationRepository).deleteAllByUserId(1L);
        verify(recommendationRepository).saveAll(List.of(rec));
    }

    @Test
    void generateRecommendationsForUser_usesContentBasedWhenFewReviews() {
        User user = buildUser(1L);
        Movie movie = buildMovie(1L, "Test Movie");

        Review review = buildReview(1L, user, movie, (short) 4);
        Recommendation rec = buildRecommendation(1L, user, movie, 0.8, Recommendation.StrategyType.CONTENT_BASED);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(reviewRepository.findAll()).thenReturn(List.of(review));
        when(contentBasedStrategy.recommend(1L, 20)).thenReturn(List.of(rec));

        recommendationService.generateRecommendationsForUser(1L);

        verify(contentBasedStrategy).recommend(1L, 20);
        verify(collaborativeStrategy, never()).recommend(anyLong(), anyInt());
        verify(recommendationRepository).deleteAllByUserId(1L);
        verify(recommendationRepository).saveAll(List.of(rec));
    }

    @Test
    void generateRecommendationsForUser_skipsIfUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        recommendationService.generateRecommendationsForUser(99L);

        verify(reviewRepository, never()).findAll();
        verify(collaborativeStrategy, never()).recommend(anyLong(), anyInt());
        verify(contentBasedStrategy, never()).recommend(anyLong(), anyInt());
        verify(recommendationRepository, never()).deleteAllByUserId(anyLong());
    }

    @Test
    void generateRecommendationsForUser_skipsIfUserInactive() {
        User user = buildUser(1L);
        user.setIsActive(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        recommendationService.generateRecommendationsForUser(1L);

        verify(reviewRepository, never()).findAll();
        verify(collaborativeStrategy, never()).recommend(anyLong(), anyInt());
        verify(contentBasedStrategy, never()).recommend(anyLong(), anyInt());
        verify(recommendationRepository, never()).deleteAllByUserId(anyLong());
    }
}
