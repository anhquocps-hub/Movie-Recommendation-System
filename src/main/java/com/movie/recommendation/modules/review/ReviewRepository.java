package com.movie.recommendation.modules.review;

import com.movie.recommendation.modules.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findAllByMovieId(Long movieId, Pageable pageable);

    Optional<Review> findByUserIdAndMovieId(Long userId, Long movieId);

    boolean existsByUserIdAndMovieId(Long userId, Long movieId);

    @Query("SELECT COALESCE(AVG(CAST(r.rating AS double)), 0) FROM Review r WHERE r.movie.id = :movieId AND r.isDeleted = false")
    Double calculateAverageRating(@Param("movieId") Long movieId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.movie.id = :movieId AND r.isDeleted = false")
    int countByMovieId(@Param("movieId") Long movieId);
}
