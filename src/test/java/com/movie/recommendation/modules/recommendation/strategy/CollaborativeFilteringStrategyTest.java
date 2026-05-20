package com.movie.recommendation.modules.recommendation.strategy;

import com.movie.recommendation.modules.movie.MovieRepository;
import com.movie.recommendation.modules.movie.entity.Genre;
import com.movie.recommendation.modules.movie.entity.Movie;
import com.movie.recommendation.modules.recommendation.entity.Recommendation;
import com.movie.recommendation.modules.review.ReviewRepository;
import com.movie.recommendation.modules.review.entity.Review;
import com.movie.recommendation.modules.user.UserRepository;
import com.movie.recommendation.modules.user.entity.Role;
import com.movie.recommendation.modules.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CollaborativeFilteringStrategyTest {

    @Mock private UserRepository userRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private MovieRepository movieRepository;

    @InjectMocks private CollaborativeFilteringStrategy strategy;

    private User buildUser(Long id) {
        return User.builder()
                .id(id).email("u" + id + "@test.com").username("user" + id)
                .passwordHash("hash").role(Role.USER).isActive(true).build();
    }

    private Movie buildMovie(Long id, String title) {
        return Movie.builder()
                .id(id).title(title).slug(title.toLowerCase().replace(" ", "-"))
                .overview("Overview").releaseDate(LocalDate.of(2024, 1, 1))
                .runtimeMinutes(120).avgRating(BigDecimal.valueOf(4.0))
                .voteCount(100).isActive(true).genres(Set.of(Genre.builder().id(1).name("Action").build()))
                .build();
    }

    private Review buildReview(Long id, User user, Movie movie, short rating) {
        return Review.builder()
                .id(id).user(user).movie(movie).rating(rating)
                .content("Review").isSpoiler(false).build();
    }

    @Test
    void recommend_nullUser_returnsEmpty() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        List<Recommendation> result = strategy.recommend(99L, 20);

        assertThat(result).isEmpty();
    }

    @Test
    void recommend_noReviews_returnsEmpty() {
        User user = buildUser(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(reviewRepository.findAll()).thenReturn(List.of());

        List<Recommendation> result = strategy.recommend(1L, 20);

        assertThat(result).isEmpty();
    }

    @Test
    void recommend_noSimilarUsers_returnsEmpty() {
        User targetUser = buildUser(1L);
        Movie movie1 = buildMovie(1L, "Movie A");

        Review targetReview = buildReview(1L, targetUser, movie1, (short) 5);

        when(userRepository.findById(1L)).thenReturn(Optional.of(targetUser));
        when(reviewRepository.findAll()).thenReturn(List.of(targetReview));
        when(userRepository.findAll()).thenReturn(List.of(targetUser));

        List<Recommendation> result = strategy.recommend(1L, 20);

        assertThat(result).isEmpty();
    }

    @Test
    void recommend_similarUsersNoOverlap_returnsEmpty() {
        User target = buildUser(1L);
        User other = buildUser(2L);
        Movie movie1 = buildMovie(1L, "Movie A");
        Movie movie2 = buildMovie(2L, "Movie B");

        Review targetReview = buildReview(1L, target, movie1, (short) 5);
        Review otherReview = buildReview(2L, other, movie2, (short) 5);

        when(userRepository.findById(1L)).thenReturn(Optional.of(target));
        when(reviewRepository.findAll()).thenReturn(List.of(targetReview, otherReview));
        when(userRepository.findAll()).thenReturn(List.of(target, other));

        List<Recommendation> result = strategy.recommend(1L, 20);

        assertThat(result).isEmpty();
    }

    @Test
    void recommend_withSimilarUsers_returnsRecommendations() {
        User target = buildUser(1L);
        User similar = buildUser(2L);
        Movie sharedMovie = buildMovie(1L, "Shared Movie");
        Movie recommendableMovie = buildMovie(2L, "Recommend This");

        Review targetReview = buildReview(1L, target, sharedMovie, (short) 5);
        Review similarReview1 = buildReview(2L, similar, sharedMovie, (short) 5);
        Review similarReview2 = buildReview(3L, similar, recommendableMovie, (short) 4);

        when(userRepository.findById(1L)).thenReturn(Optional.of(target));
        when(reviewRepository.findAll()).thenReturn(List.of(targetReview, similarReview1, similarReview2));
        when(userRepository.findAll()).thenReturn(List.of(target, similar));
        when(movieRepository.findByIdAndIsActiveTrue(2L)).thenReturn(Optional.of(recommendableMovie));

        List<Recommendation> result = strategy.recommend(1L, 20);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getMovie().getTitle()).isEqualTo("Recommend This");
        assertThat(result.getFirst().getStrategyType()).isEqualTo(Recommendation.StrategyType.COLLABORATIVE);
    }

    @Test
    void recommend_movieNotActive_excluded() {
        User target = buildUser(1L);
        User similar = buildUser(2L);
        Movie sharedMovie = buildMovie(1L, "Shared");
        Movie inactiveMovie = buildMovie(2L, "Inactive");

        Review targetReview = buildReview(1L, target, sharedMovie, (short) 5);
        Review similarReview1 = buildReview(2L, similar, sharedMovie, (short) 5);
        Review similarReview2 = buildReview(3L, similar, inactiveMovie, (short) 4);

        when(userRepository.findById(1L)).thenReturn(Optional.of(target));
        when(reviewRepository.findAll()).thenReturn(List.of(targetReview, similarReview1, similarReview2));
        when(userRepository.findAll()).thenReturn(List.of(target, similar));
        when(movieRepository.findByIdAndIsActiveTrue(2L)).thenReturn(Optional.empty());

        List<Recommendation> result = strategy.recommend(1L, 20);

        assertThat(result).isEmpty();
    }

    @Test
    void recommend_skipsInactiveUsers() {
        User target = buildUser(1L);
        User inactive = buildUser(2L);
        inactive.setIsActive(false);
        Movie movie = buildMovie(1L, "Movie");

        Review targetReview = buildReview(1L, target, movie, (short) 5);

        when(userRepository.findById(1L)).thenReturn(Optional.of(target));
        when(reviewRepository.findAll()).thenReturn(List.of(targetReview));
        when(userRepository.findAll()).thenReturn(List.of(target, inactive));

        List<Recommendation> result = strategy.recommend(1L, 20);

        assertThat(result).isEmpty();
    }

    @Test
    void recommend_otherUserWithNoReviews_skipped() {
        User target = buildUser(1L);
        User noReviews = buildUser(2L);
        Movie movie = buildMovie(1L, "Movie");

        Review targetReview = buildReview(1L, target, movie, (short) 5);

        when(userRepository.findById(1L)).thenReturn(Optional.of(target));
        when(reviewRepository.findAll()).thenReturn(List.of(targetReview));
        when(userRepository.findAll()).thenReturn(List.of(target, noReviews));

        List<Recommendation> result = strategy.recommend(1L, 20);

        assertThat(result).isEmpty();
    }

    @Test
    void getStrategyType_returnsCollaborative() {
        assertThat(strategy.getStrategyType()).isEqualTo(Recommendation.StrategyType.COLLABORATIVE);
    }
}
