package com.movie.recommendation.modules.recommendation;

import com.movie.recommendation.modules.recommendation.entity.Recommendation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

    @Query("SELECT r FROM Recommendation r WHERE r.user.id = :userId ORDER BY r.score DESC")
    Page<Recommendation> findByUserIdOrderByScoreDesc(@Param("userId") Long userId, Pageable pageable);

    @Modifying
    @Query("DELETE FROM Recommendation r WHERE r.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM Recommendation r WHERE r.generatedAt < :cutoffTime")
    void deleteOlderThan(@Param("cutoffTime") java.time.LocalDateTime cutoffTime);

    boolean existsByUserIdAndMovieId(Long userId, Long movieId);
}
