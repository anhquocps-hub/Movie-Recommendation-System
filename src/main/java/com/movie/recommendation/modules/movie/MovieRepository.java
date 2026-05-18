package com.movie.recommendation.modules.movie;

import com.movie.recommendation.modules.movie.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    Optional<Movie> findByIdAndIsActiveTrue(Long id);

    Page<Movie> findAllByIsActiveTrue(Pageable pageable);

    boolean existsBySlug(String slug);

    @Query(value = "SELECT * FROM movies WHERE is_active = TRUE AND title ILIKE '%' || :query || '%' " +
            "ORDER BY similarity(title, :query) DESC",
            countQuery = "SELECT COUNT(*) FROM movies WHERE is_active = TRUE AND title ILIKE '%' || :query || '%'",
            nativeQuery = true)
    Page<Movie> searchByTitle(@Param("query") String query, Pageable pageable);

    @Query("SELECT m FROM Movie m WHERE m.isActive = true ORDER BY m.avgRating DESC, m.voteCount DESC")
    Page<Movie> findTrending(Pageable pageable);

    @Modifying
    @Query("UPDATE Movie m SET m.avgRating = :avgRating, m.voteCount = :voteCount WHERE m.id = :movieId")
    void updateRatingStats(@Param("movieId") Long movieId,
                           @Param("avgRating") BigDecimal avgRating,
                           @Param("voteCount") int voteCount);
}
