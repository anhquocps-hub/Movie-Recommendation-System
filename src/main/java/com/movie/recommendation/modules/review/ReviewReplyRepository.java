package com.movie.recommendation.modules.review;

import com.movie.recommendation.modules.review.entity.ReviewReply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewReplyRepository extends JpaRepository<ReviewReply, Long> {

    Page<ReviewReply> findAllByReviewId(Long reviewId, Pageable pageable);

    long countByReviewId(Long reviewId);
}
