package com.movie.recommendation.modules.review;

import com.movie.recommendation.common.constants.AppConstants;
import com.movie.recommendation.common.dto.PagedResponse;
import com.movie.recommendation.exception.DuplicateResourceException;
import com.movie.recommendation.exception.ResourceNotFoundException;
import com.movie.recommendation.exception.UnauthorizedException;
import com.movie.recommendation.modules.movie.MovieRepository;
import com.movie.recommendation.modules.movie.entity.Movie;
import com.movie.recommendation.modules.review.dto.*;
import com.movie.recommendation.modules.review.entity.Review;
import com.movie.recommendation.modules.review.entity.ReviewLike;
import com.movie.recommendation.modules.review.entity.ReviewReply;
import com.movie.recommendation.modules.user.UserRepository;
import com.movie.recommendation.modules.user.entity.User;
import com.movie.recommendation.modules.notification.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final ReviewReplyRepository reviewReplyRepository;
    private final MovieRepository movieRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public PagedResponse<ReviewResponse> getMovieReviews(Long movieId, Long currentUserId, int page, int size) {
        size = Math.min(size, AppConstants.MAX_PAGE_SIZE);
        movieRepository.findByIdAndIsActiveTrue(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", movieId));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ReviewResponse> reviews = reviewRepository.findAllByMovieId(movieId, pageable)
                .map(review -> toReviewResponse(review, currentUserId));
        return PagedResponse.from(reviews);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = AppConstants.MOVIE_DETAIL_CACHE, key = "#movieId"),
            @CacheEvict(value = AppConstants.TRENDING_CACHE, allEntries = true)
    })
    public ReviewResponse createReview(Long movieId, Long userId, CreateReviewRequest request) {
        Movie movie = movieRepository.findByIdAndIsActiveTrue(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", movieId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (reviewRepository.existsByUserIdAndMovieId(userId, movieId)) {
            throw new DuplicateResourceException("Review", "movieId", movieId);
        }

        Review review = Review.builder()
                .user(user)
                .movie(movie)
                .rating(request.getRating())
                .content(request.getContent())
                .isSpoiler(request.getIsSpoiler() != null ? request.getIsSpoiler() : false)
                .build();

        review = reviewRepository.save(review);
        recalculateMovieRating(movieId);
        return toReviewResponse(review, userId);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = AppConstants.TRENDING_CACHE, allEntries = true)
    })
    public ReviewResponse updateReview(Long reviewId, Long userId, UpdateReviewRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        if (!review.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You can only edit your own reviews");
        }

        boolean ratingChanged = false;
        if (request.getRating() != null) {
            ratingChanged = !request.getRating().equals(review.getRating());
            review.setRating(request.getRating());
        }
        if (request.getContent() != null) {
            review.setContent(request.getContent());
        }
        if (request.getIsSpoiler() != null) {
            review.setIsSpoiler(request.getIsSpoiler());
        }

        review = reviewRepository.save(review);

        if (ratingChanged) {
            recalculateMovieRating(review.getMovie().getId());
        }

        return toReviewResponse(review, userId);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = AppConstants.TRENDING_CACHE, allEntries = true)
    })
    public void deleteReview(Long reviewId, Long userId, String userRole) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        if (!review.getUser().getId().equals(userId) && !"ADMIN".equals(userRole)) {
            throw new UnauthorizedException("You can only delete your own reviews");
        }

        if (Boolean.TRUE.equals(review.getIsDeleted())) {
            return;
        }

        Long movieId = review.getMovie().getId();
        review.setIsDeleted(true);
        reviewRepository.save(review);
        recalculateMovieRating(movieId);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = AppConstants.MOVIE_DETAIL_CACHE, allEntries = true),
            @CacheEvict(value = AppConstants.TRENDING_CACHE, allEntries = true)
    })
    public void hideReview(Long reviewId) {
        Review review = findReview(reviewId);
        review.setIsHidden(true);
        reviewRepository.save(review);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = AppConstants.MOVIE_DETAIL_CACHE, allEntries = true),
            @CacheEvict(value = AppConstants.TRENDING_CACHE, allEntries = true)
    })
    public void unhideReview(Long reviewId) {
        Review review = findReview(reviewId);
        review.setIsHidden(false);
        reviewRepository.save(review);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = AppConstants.MOVIE_DETAIL_CACHE, allEntries = true),
            @CacheEvict(value = AppConstants.TRENDING_CACHE, allEntries = true)
    })
    public void adminDeleteReview(Long reviewId) {
        Review review = findReview(reviewId);
        if (Boolean.TRUE.equals(review.getIsDeleted())) {
            return;
        }
        Long movieId = review.getMovie().getId();
        review.setIsDeleted(true);
        reviewRepository.save(review);
        recalculateMovieRating(movieId);
    }

    @Transactional
    public void hideReply(Long replyId) {
        ReviewReply reply = findReply(replyId);
        reply.setIsHidden(true);
        reviewReplyRepository.save(reply);
    }

    @Transactional
    public void unhideReply(Long replyId) {
        ReviewReply reply = findReply(replyId);
        reply.setIsHidden(false);
        reviewReplyRepository.save(reply);
    }

    @Transactional
    public void adminDeleteReply(Long replyId) {
        ReviewReply reply = findReply(replyId);
        reply.setIsDeleted(true);
        reviewReplyRepository.save(reply);
    }

    public java.util.List<AdminReviewResponse> getAdminMovieReviews(Long movieId) {
        movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", movieId));

        Pageable pageable = PageRequest.of(0, AppConstants.MAX_PAGE_SIZE, Sort.by("createdAt").descending());
        return reviewRepository.findAllByMovieId(movieId, pageable)
                .map(this::toAdminReviewResponse)
                .getContent();
    }

    @Transactional
    public boolean toggleLike(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Optional<ReviewLike> existingLike = reviewLikeRepository.findByReviewIdAndUserId(reviewId, userId);
        if (existingLike.isPresent()) {
            reviewLikeRepository.delete(existingLike.get());
            return false;
        } else {
            ReviewLike like = ReviewLike.builder()
                    .review(review)
                    .user(user)
                    .build();
            reviewLikeRepository.save(like);

            if (!review.getUser().getId().equals(userId)) {
                eventPublisher.publishEvent(new NotificationEvent.ReviewLikedEvent(
                        reviewId,
                        review.getUser().getId(),
                        userId,
                        user.getUsername(),
                        review.getMovie().getTitle()
                ));
            }
            return true;
        }
    }

    @Transactional
    public ReplyResponse createReply(Long reviewId, Long userId, CreateReplyRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        ReviewReply reply = ReviewReply.builder()
                .review(review)
                .user(user)
                .content(request.getContent())
                .build();

        reply = reviewReplyRepository.save(reply);

        if (!review.getUser().getId().equals(userId)) {
            String replyPreview = request.getContent().length() > 50
                    ? request.getContent().substring(0, 50) + "..."
                    : request.getContent();
            eventPublisher.publishEvent(new NotificationEvent.ReviewRepliedEvent(
                    reviewId,
                    review.getUser().getId(),
                    userId,
                    user.getUsername(),
                    review.getMovie().getTitle(),
                    replyPreview
            ));
        }

        return toReplyResponse(reply);
    }

    public PagedResponse<ReplyResponse> getReplies(Long reviewId, int page, int size) {
        size = Math.min(size, AppConstants.MAX_PAGE_SIZE);
        reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        Page<ReplyResponse> replies = reviewReplyRepository.findAllByReviewId(reviewId, pageable)
                .map(reply -> toReplyResponse(reply));
        return PagedResponse.from(replies);
    }

    private Review findReview(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));
    }

    private ReviewReply findReply(Long replyId) {
        return reviewReplyRepository.findById(replyId)
                .orElseThrow(() -> new ResourceNotFoundException("Reply", "id", replyId));
    }

    private void recalculateMovieRating(Long movieId) {
        Double avg = reviewRepository.calculateAverageRating(movieId);
        int count = reviewRepository.countByMovieId(movieId);
        BigDecimal avgRating = BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP);
        movieRepository.updateRatingStats(movieId, avgRating, count);
    }

    private ReviewResponse toReviewResponse(Review review, Long currentUserId) {
        long likeCount = reviewLikeRepository.countByReviewId(review.getId());
        long replyCount = reviewReplyRepository.countByReviewId(review.getId());
        boolean likedByCurrentUser = currentUserId != null
                && reviewLikeRepository.existsByReviewIdAndUserId(review.getId(), currentUserId);
        boolean hidden = Boolean.TRUE.equals(review.getIsHidden());
        boolean deleted = Boolean.TRUE.equals(review.getIsDeleted());

        return ReviewResponse.builder()
                .id(review.getId())
                .userId(review.getUser().getId())
                .username(review.getUser().getUsername())
                .movieId(review.getMovie().getId())
                .movieTitle(review.getMovie().getTitle())
                .rating(deleted ? null : review.getRating())
                .content(hidden || deleted ? null : review.getContent())
                .isSpoiler(hidden || deleted ? false : review.getIsSpoiler())
                .likeCount(likeCount)
                .replyCount(replyCount)
                .likedByCurrentUser(likedByCurrentUser)
                .hidden(hidden)
                .deleted(deleted)
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }

    private ReplyResponse toReplyResponse(ReviewReply reply) {
        boolean hidden = Boolean.TRUE.equals(reply.getIsHidden());
        boolean deleted = Boolean.TRUE.equals(reply.getIsDeleted());

        return ReplyResponse.builder()
                .id(reply.getId())
                .userId(reply.getUser().getId())
                .username(reply.getUser().getUsername())
                .content(hidden || deleted ? null : reply.getContent())
                .hidden(hidden)
                .deleted(deleted)
                .createdAt(reply.getCreatedAt())
                .updatedAt(reply.getUpdatedAt())
                .build();
    }

    private AdminReviewResponse toAdminReviewResponse(Review review) {
        long likeCount = reviewLikeRepository.countByReviewId(review.getId());
        long replyCount = reviewReplyRepository.countByReviewId(review.getId());
        java.util.List<AdminReplyResponse> replies = reviewReplyRepository
                .findAllByReviewIdOrderByCreatedAtAsc(review.getId())
                .stream()
                .map(this::toAdminReplyResponse)
                .toList();

        return AdminReviewResponse.builder()
                .id(review.getId())
                .userId(review.getUser().getId())
                .username(review.getUser().getUsername())
                .movieId(review.getMovie().getId())
                .movieTitle(review.getMovie().getTitle())
                .rating(review.getRating())
                .content(review.getContent())
                .isSpoiler(review.getIsSpoiler())
                .likeCount(likeCount)
                .replyCount(replyCount)
                .hidden(Boolean.TRUE.equals(review.getIsHidden()))
                .deleted(Boolean.TRUE.equals(review.getIsDeleted()))
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .replies(replies)
                .build();
    }

    private AdminReplyResponse toAdminReplyResponse(ReviewReply reply) {
        return AdminReplyResponse.builder()
                .id(reply.getId())
                .userId(reply.getUser().getId())
                .username(reply.getUser().getUsername())
                .content(reply.getContent())
                .hidden(Boolean.TRUE.equals(reply.getIsHidden()))
                .deleted(Boolean.TRUE.equals(reply.getIsDeleted()))
                .createdAt(reply.getCreatedAt())
                .updatedAt(reply.getUpdatedAt())
                .build();
    }
}
