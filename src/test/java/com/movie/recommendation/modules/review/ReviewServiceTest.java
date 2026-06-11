package com.movie.recommendation.modules.review;

import com.movie.recommendation.exception.DuplicateResourceException;
import com.movie.recommendation.exception.ResourceNotFoundException;
import com.movie.recommendation.exception.UnauthorizedException;
import com.movie.recommendation.modules.movie.MovieRepository;
import com.movie.recommendation.modules.movie.entity.Movie;
import com.movie.recommendation.modules.review.dto.CreateReplyRequest;
import com.movie.recommendation.modules.review.dto.CreateReviewRequest;
import com.movie.recommendation.modules.review.dto.ReplyResponse;
import com.movie.recommendation.modules.review.dto.ReviewResponse;
import com.movie.recommendation.modules.review.dto.UpdateReviewRequest;
import com.movie.recommendation.modules.review.entity.Review;
import com.movie.recommendation.modules.review.entity.ReviewLike;
import com.movie.recommendation.modules.review.entity.ReviewReply;
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

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock private ReviewRepository reviewRepository;
    @Mock private ReviewLikeRepository reviewLikeRepository;
    @Mock private ReviewReplyRepository reviewReplyRepository;
    @Mock private MovieRepository movieRepository;
    @Mock private UserRepository userRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ReviewService reviewService;

    private User buildUser(Long id) {
        return User.builder().id(id).email("user@test.com").username("testuser")
                .passwordHash("hash").role(Role.USER).isActive(true).build();
    }

    private Movie buildMovie(Long id) {
        return Movie.builder().id(id).title("Test Movie").slug("test-movie")
                .avgRating(BigDecimal.ZERO).voteCount(0).isActive(true)
                .genres(new HashSet<>()).build();
    }

    private Review buildReview(Long id, User user, Movie movie) {
        return Review.builder().id(id).user(user).movie(movie)
                .rating((short) 4).content("Great movie").isSpoiler(false).build();
    }

    @Test
    void createReview_success() {
        User user = buildUser(1L);
        Movie movie = buildMovie(1L);
        CreateReviewRequest request = new CreateReviewRequest();
        request.setRating((short) 5);
        request.setContent("Excellent");

        when(movieRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(movie));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(reviewRepository.existsByUserIdAndMovieId(1L, 1L)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> {
            Review r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });
        when(reviewRepository.calculateAverageRating(1L)).thenReturn(5.0);
        when(reviewRepository.countByMovieId(1L)).thenReturn(1);
        when(reviewLikeRepository.countByReviewId(anyLong())).thenReturn(0L);
        when(reviewReplyRepository.countByReviewId(anyLong())).thenReturn(0L);
        when(reviewLikeRepository.existsByReviewIdAndUserId(anyLong(), anyLong())).thenReturn(false);

        ReviewResponse result = reviewService.createReview(1L, 1L, request);

        assertThat(result.getRating()).isEqualTo((short) 5);
        verify(movieRepository).updateRatingStats(eq(1L), any(BigDecimal.class), eq(1));
    }

    @Test
    void createReview_duplicate_throws() {
        when(movieRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(buildMovie(1L)));
        when(userRepository.findById(1L)).thenReturn(Optional.of(buildUser(1L)));
        when(reviewRepository.existsByUserIdAndMovieId(1L, 1L)).thenReturn(true);

        CreateReviewRequest request = new CreateReviewRequest();
        request.setRating((short) 3);

        assertThatThrownBy(() -> reviewService.createReview(1L, 1L, request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void createReview_movieNotFound_throws() {
        when(movieRepository.findByIdAndIsActiveTrue(99L)).thenReturn(Optional.empty());

        CreateReviewRequest request = new CreateReviewRequest();
        request.setRating((short) 3);

        assertThatThrownBy(() -> reviewService.createReview(99L, 1L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateReview_ownerSuccess() {
        User user = buildUser(1L);
        Movie movie = buildMovie(1L);
        Review review = buildReview(1L, user, movie);

        UpdateReviewRequest request = new UpdateReviewRequest();
        request.setRating((short) 3);
        request.setContent("Changed my mind");

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));
        when(reviewRepository.calculateAverageRating(1L)).thenReturn(3.0);
        when(reviewRepository.countByMovieId(1L)).thenReturn(1);
        when(reviewLikeRepository.countByReviewId(1L)).thenReturn(0L);
        when(reviewReplyRepository.countByReviewId(1L)).thenReturn(0L);
        when(reviewLikeRepository.existsByReviewIdAndUserId(1L, 1L)).thenReturn(false);

        ReviewResponse result = reviewService.updateReview(1L, 1L, request);

        assertThat(result.getRating()).isEqualTo((short) 3);
    }

    @Test
    void updateReview_notOwner_throws() {
        User owner = buildUser(1L);
        Review review = buildReview(1L, owner, buildMovie(1L));

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        UpdateReviewRequest request = new UpdateReviewRequest();
        request.setContent("Hacked");

        assertThatThrownBy(() -> reviewService.updateReview(1L, 99L, request))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void deleteReview_ownerSuccess() {
        User user = buildUser(1L);
        Movie movie = buildMovie(1L);
        Review review = buildReview(1L, user, movie);

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.calculateAverageRating(1L)).thenReturn(0.0);
        when(reviewRepository.countByMovieId(1L)).thenReturn(0);

        reviewService.deleteReview(1L, 1L, "USER");

        assertThat(review.getIsDeleted()).isTrue();
        verify(reviewRepository).save(review);
        verify(reviewRepository, never()).delete(review);
    }

    @Test
    void deleteReview_adminBypass() {
        User owner = buildUser(1L);
        Movie movie = buildMovie(1L);
        Review review = buildReview(1L, owner, movie);

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.calculateAverageRating(1L)).thenReturn(0.0);
        when(reviewRepository.countByMovieId(1L)).thenReturn(0);

        reviewService.deleteReview(1L, 99L, "ADMIN");

        assertThat(review.getIsDeleted()).isTrue();
        verify(reviewRepository).save(review);
        verify(reviewRepository, never()).delete(review);
    }

    @Test
    void deleteReview_notOwnerNotAdmin_throws() {
        User owner = buildUser(1L);
        Review review = buildReview(1L, owner, buildMovie(1L));

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        assertThatThrownBy(() -> reviewService.deleteReview(1L, 99L, "USER"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void toggleLike_like() {
        User user = buildUser(1L);
        User owner = buildUser(2L);
        owner.setUsername("owner");
        Review review = buildReview(1L, owner, buildMovie(1L));

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(reviewLikeRepository.findByReviewIdAndUserId(1L, 1L)).thenReturn(Optional.empty());
        when(reviewLikeRepository.save(any(ReviewLike.class))).thenAnswer(inv -> inv.getArgument(0));

        boolean result = reviewService.toggleLike(1L, 1L);

        assertThat(result).isTrue();
        verify(reviewLikeRepository).save(any(ReviewLike.class));
        verify(eventPublisher).publishEvent(any(NotificationEvent.ReviewLikedEvent.class));
    }

    @Test
    void toggleLike_unlike() {
        ReviewLike existingLike = ReviewLike.builder().id(1L).build();
        Review review = buildReview(1L, buildUser(2L), buildMovie(1L));

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(userRepository.findById(1L)).thenReturn(Optional.of(buildUser(1L)));
        when(reviewLikeRepository.findByReviewIdAndUserId(1L, 1L)).thenReturn(Optional.of(existingLike));

        boolean result = reviewService.toggleLike(1L, 1L);

        assertThat(result).isFalse();
        verify(reviewLikeRepository).delete(existingLike);
    }

    @Test
    void toggleLike_selfLike_noEventPublished() {
        User user = buildUser(1L);
        Review review = buildReview(1L, user, buildMovie(1L));

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(reviewLikeRepository.findByReviewIdAndUserId(1L, 1L)).thenReturn(Optional.empty());
        when(reviewLikeRepository.save(any(ReviewLike.class))).thenAnswer(inv -> inv.getArgument(0));

        boolean result = reviewService.toggleLike(1L, 1L);

        assertThat(result).isTrue();
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void createReply_success() {
        User user = buildUser(1L);
        User owner = buildUser(2L);
        owner.setUsername("owner");
        Review review = buildReview(1L, owner, buildMovie(1L));
        CreateReplyRequest request = new CreateReplyRequest();
        request.setContent("Great review!");

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(reviewReplyRepository.save(any(ReviewReply.class))).thenAnswer(inv -> {
            ReviewReply r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        ReplyResponse result = reviewService.createReply(1L, 1L, request);

        assertThat(result.getContent()).isEqualTo("Great review!");
        verify(eventPublisher).publishEvent(any(NotificationEvent.ReviewRepliedEvent.class));
    }

    @Test
    void createReply_selfReply_noEventPublished() {
        User user = buildUser(1L);
        Review review = buildReview(1L, user, buildMovie(1L));
        CreateReplyRequest request = new CreateReplyRequest();
        request.setContent("My own reply");

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(reviewReplyRepository.save(any(ReviewReply.class))).thenAnswer(inv -> {
            ReviewReply r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        ReplyResponse result = reviewService.createReply(1L, 1L, request);

        assertThat(result.getContent()).isEqualTo("My own reply");
        verify(eventPublisher, never()).publishEvent(any());
    }
}
