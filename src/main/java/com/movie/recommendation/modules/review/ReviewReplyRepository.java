package com.movie.recommendation.modules.review;

import com.movie.recommendation.modules.review.entity.ReviewReply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewReplyRepository extends JpaRepository<ReviewReply, Long> {

    Page<ReviewReply> findAllByReviewId(Long reviewId, Pageable pageable);

    List<ReviewReply> findAllByReviewIdOrderByCreatedAtAsc(Long reviewId);

    long countByReviewId(Long reviewId);
}
