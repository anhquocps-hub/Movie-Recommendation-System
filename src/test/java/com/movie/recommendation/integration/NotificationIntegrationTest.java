package com.movie.recommendation.integration;

import com.movie.recommendation.common.dto.ApiResponse;
import com.movie.recommendation.modules.movie.dto.CreateGenreRequest;
import com.movie.recommendation.modules.movie.dto.CreateMovieRequest;
import com.movie.recommendation.modules.notification.NotificationRepository;
import com.movie.recommendation.modules.notification.entity.Notification;
import com.movie.recommendation.modules.review.dto.CreateReplyRequest;
import com.movie.recommendation.modules.review.dto.CreateReviewRequest;
import com.movie.recommendation.modules.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private NotificationRepository notificationRepository;

    private String adminToken;
    private String user1Token;
    private String user2Token;
    private Long movieId;
    private Long reviewId;
    private String uid;
    private User user1;
    private User user2;

    @BeforeEach
    @SuppressWarnings("rawtypes")
    void setUp() {
        uid = UUID.randomUUID().toString().substring(0, 8);
        adminToken = registerAdmin("nadm" + uid + "@test.com", "nadm" + uid);
        user1Token = registerUser("nu1" + uid + "@test.com", "nu1" + uid);
        user2Token = registerUser("nu2" + uid + "@test.com", "nu2" + uid);

        user1 = userRepository.findByEmail("nu1" + uid + "@test.com").orElseThrow();
        user2 = userRepository.findByEmail("nu2" + uid + "@test.com").orElseThrow();

        CreateGenreRequest genreReq = new CreateGenreRequest();
        genreReq.setName("NotifGenre-" + uid);
        HttpEntity<CreateGenreRequest> genreEntity = new HttpEntity<>(genreReq, authenticatedHeaders(adminToken));
        ResponseEntity<ApiResponse> genreResp = restTemplate.exchange(
                "/api/v1/genres", HttpMethod.POST, genreEntity, ApiResponse.class);
        LinkedHashMap<?, ?> genreData = (LinkedHashMap<?, ?>) genreResp.getBody().getData();
        Integer genreId = (Integer) genreData.get("id");

        CreateMovieRequest movieReq = new CreateMovieRequest();
        movieReq.setTitle("Notif Movie " + uid);
        movieReq.setOverview("For notification testing");
        movieReq.setReleaseDate(LocalDate.of(2024, 3, 1));
        movieReq.setRuntimeMinutes(90);
        movieReq.setGenreIds(Set.of(genreId));

        HttpEntity<CreateMovieRequest> movieEntity = new HttpEntity<>(movieReq, authenticatedHeaders(adminToken));
        ResponseEntity<ApiResponse> movieResp = restTemplate.exchange(
                "/api/v1/movies", HttpMethod.POST, movieEntity, ApiResponse.class);
        LinkedHashMap<?, ?> movieData = (LinkedHashMap<?, ?>) movieResp.getBody().getData();
        movieId = ((Number) movieData.get("id")).longValue();

        CreateReviewRequest reviewReq = new CreateReviewRequest();
        reviewReq.setRating((short) 4);
        reviewReq.setContent("A review for notifications");
        HttpEntity<CreateReviewRequest> reviewEntity = new HttpEntity<>(reviewReq, authenticatedHeaders(user1Token));
        ResponseEntity<ApiResponse> reviewResp = restTemplate.exchange(
                "/api/v1/movies/" + movieId + "/reviews", HttpMethod.POST, reviewEntity, ApiResponse.class);
        LinkedHashMap<?, ?> reviewData = (LinkedHashMap<?, ?>) reviewResp.getBody().getData();
        reviewId = ((Number) reviewData.get("id")).longValue();
    }

    @Test
    @SuppressWarnings("rawtypes")
    void getNotifications_authenticated_returns200() {
        HttpEntity<Void> entity = new HttpEntity<>(authenticatedHeaders(user1Token));
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                "/api/v1/notifications", HttpMethod.GET, entity, ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @SuppressWarnings("rawtypes")
    void getNotifications_unauthenticated_returns401() {
        ResponseEntity<ApiResponse> response = restTemplate.getForEntity(
                "/api/v1/notifications", ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @SuppressWarnings("rawtypes")
    void getUnreadCount_returns200() {
        HttpEntity<Void> entity = new HttpEntity<>(authenticatedHeaders(user1Token));
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                "/api/v1/notifications/unread-count", HttpMethod.GET, entity, ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @SuppressWarnings("rawtypes")
    void markAllAsRead_returns200() {
        HttpEntity<Void> entity = new HttpEntity<>(authenticatedHeaders(user1Token));
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                "/api/v1/notifications/read-all", HttpMethod.PATCH, entity, ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @SuppressWarnings("rawtypes")
    void likeCreatesNotification_forOtherUser() throws InterruptedException {
        HttpEntity<Void> likeEntity = new HttpEntity<>(authenticatedHeaders(user2Token));
        restTemplate.exchange(
                "/api/v1/reviews/" + reviewId + "/like", HttpMethod.POST, likeEntity, ApiResponse.class);

        Thread.sleep(500);

        List<Notification> notifications = notificationRepository.findAll().stream()
                .filter(n -> n.getRecipient().getId().equals(user1.getId())
                        && n.getType() == Notification.NotificationType.REVIEW_LIKE)
                .toList();

        assertThat(notifications).isNotEmpty();
        assertThat(notifications.get(0).getMessage()).contains("liked your review");
    }

    @Test
    @SuppressWarnings("rawtypes")
    void selfLike_doesNotCreateNotification() throws InterruptedException {
        long countBefore = notificationRepository.countByRecipientIdAndIsReadFalse(user1.getId());

        HttpEntity<Void> selfLikeEntity = new HttpEntity<>(authenticatedHeaders(user1Token));
        restTemplate.exchange(
                "/api/v1/reviews/" + reviewId + "/like", HttpMethod.POST, selfLikeEntity, ApiResponse.class);

        Thread.sleep(500);

        long countAfter = notificationRepository.countByRecipientIdAndIsReadFalse(user1.getId());
        assertThat(countAfter).isEqualTo(countBefore);
    }

    @Test
    @SuppressWarnings("rawtypes")
    void replyCreatesNotification_forOtherUser() throws InterruptedException {
        CreateReplyRequest replyReq = new CreateReplyRequest();
        replyReq.setContent("Great review!");

        HttpEntity<CreateReplyRequest> replyEntity = new HttpEntity<>(replyReq, authenticatedHeaders(user2Token));
        restTemplate.exchange(
                "/api/v1/reviews/" + reviewId + "/replies", HttpMethod.POST, replyEntity, ApiResponse.class);

        Thread.sleep(500);

        List<Notification> notifications = notificationRepository.findAll().stream()
                .filter(n -> n.getRecipient().getId().equals(user1.getId())
                        && n.getType() == Notification.NotificationType.REVIEW_REPLY)
                .toList();

        assertThat(notifications).isNotEmpty();
        assertThat(notifications.get(0).getMessage()).contains("replied to your review");
    }
}
